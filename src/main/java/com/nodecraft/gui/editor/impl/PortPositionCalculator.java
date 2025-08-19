package com.nodecraft.gui.editor.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.nodecraft.nodesystem.api.INode;
import com.nodecraft.nodesystem.api.IPort;
import com.nodecraft.nodesystem.graph.NodeGraph;
import imgui.ImGui;
import imgui.ImVec2;

/**
 * 端口位置计算器
 * 负责计算和管理节点端口的屏幕位置
 */
public class PortPositionCalculator {
    
    private final ICanvasEditor editor;
    private final PortPositionManager portPositionManager;
    private final FlatPortPositionManager flatPortManager;
    private boolean useFlatPortManager = true; // 默认使用新的扁平化管理器

    public PortPositionCalculator(ICanvasEditor editor) {
        this.editor = editor;
        this.portPositionManager = new PortPositionManager();
        this.flatPortManager = new FlatPortPositionManager();
    }

    /**
     * 计算所有节点的端口位置
     */
    public void calculatePortPositions(ImVec2 canvasPos, NodeGraph graph,
                                       Map<UUID, NodePosition> nodePositions,
                                       Map<UUID, Map<String, ImVec2>> portScreenPositions) {
        if (graph == null) return;

        if (useFlatPortManager) {
            flatPortManager.nextFrame();
        } else {
            portPositionManager.nextFrame();
        }

        float canvasZoom = editor.getCanvasZoom();
        float canvasOffsetX = editor.getCanvasOffsetX();
        float canvasOffsetY = editor.getCanvasOffsetY();

        float baseTextLineHeight = ImGui.getTextLineHeight();
        float baseItemSpacingY = ImGui.getStyle().getItemSpacingY();

        for (INode node : graph.getNodes()) {
            updateNodeDimensionsInPosition(node, nodePositions, canvasZoom, baseTextLineHeight, baseItemSpacingY);
        }

        for (INode node : graph.getNodes()) {
            UUID nodeId = node.getId();
            NodePosition pos = nodePositions.get(nodeId);

            if (pos == null) continue;

            float nodeScreenX = canvasPos.x + (pos.x * canvasZoom + canvasOffsetX);
            float nodeScreenY = canvasPos.y + (pos.y * canvasZoom + canvasOffsetY);
            float finalNodeWidthScaled = pos.width * canvasZoom;

            if (!portScreenPositions.containsKey(nodeId)) {
                portScreenPositions.put(nodeId, new HashMap<>());
            }

            float scaledTextLineHeight = baseTextLineHeight * canvasZoom;
            float scaledPortVerticalSpacing = baseItemSpacingY * canvasZoom * 0.8f;

            float nodeHeaderHeightScaled = (baseTextLineHeight + 2 * NodeRenderConstants.NODE_VERTICAL_PADDING) * canvasZoom;
            float portYOffset = nodeScreenY + nodeHeaderHeightScaled + (NodeRenderConstants.NODE_VERTICAL_PADDING / 2) * canvasZoom;

            boolean hasInputPorts = !node.getInputPorts().isEmpty();
            boolean hasOutputPorts = !node.getOutputPorts().isEmpty();

            if (useFlatPortManager) {
                if (hasInputPorts) {
                    int numInputPorts = node.getInputPorts().size();
                    for (int i = 0; i < numInputPorts; i++) {
                        IPort port = node.getInputPorts().get(i);
                        float currentPortY = portYOffset + i * (scaledTextLineHeight + scaledPortVerticalSpacing) + scaledTextLineHeight / 2;
                        flatPortManager.setPortPosition(nodeId, port.getId(), nodeScreenX, currentPortY);
                        portScreenPositions.get(nodeId).put(port.getId(), new ImVec2(nodeScreenX, currentPortY));
                    }
                }

                if (hasOutputPorts) {
                    float outputPortCircleX = nodeScreenX + finalNodeWidthScaled;
                    int numOutputPorts = node.getOutputPorts().size();
                    for (int i = 0; i < numOutputPorts; i++) {
                        IPort port = node.getOutputPorts().get(i);
                        float currentPortY = portYOffset + i * (scaledTextLineHeight + scaledPortVerticalSpacing) + scaledTextLineHeight / 2;
                        flatPortManager.setPortPosition(nodeId, port.getId(), outputPortCircleX, currentPortY);
                        portScreenPositions.get(nodeId).put(port.getId(), new ImVec2(outputPortCircleX, currentPortY));
                    }
                }
            } else {
                PortPositionManager.PortPositionCalculator calculator = (nodeIdParam, nodeParam, positions) -> {
                    float localNodeScreenX = canvasPos.x + (pos.x * canvasZoom + canvasOffsetX);
                    float localFinalNodeWidthScaled = pos.width * canvasZoom;

                    if (hasInputPorts) {
                        int numInputPorts = nodeParam.getInputPorts().size();
                        for (int i = 0; i < numInputPorts; i++) {
                            IPort port = nodeParam.getInputPorts().get(i);
                            float currentPortY = portYOffset + i * (scaledTextLineHeight + scaledPortVerticalSpacing) + scaledTextLineHeight / 2;
                            positions.setPosition(port.getId(), localNodeScreenX, currentPortY);
                            portScreenPositions.get(nodeId).put(port.getId(), new ImVec2(localNodeScreenX, currentPortY));
                        }
                    }

                    if (hasOutputPorts) {
                        float outputPortCircleX = localNodeScreenX + localFinalNodeWidthScaled;
                        int numOutputPorts = nodeParam.getOutputPorts().size();
                        for (int i = 0; i < numOutputPorts; i++) {
                            IPort port = nodeParam.getOutputPorts().get(i);
                            float currentPortY = portYOffset + i * (scaledTextLineHeight + scaledPortVerticalSpacing) + scaledTextLineHeight / 2;
                            positions.setPosition(port.getId(), outputPortCircleX, currentPortY);
                            portScreenPositions.get(nodeId).put(port.getId(), new ImVec2(outputPortCircleX, currentPortY));
                        }
                    }
                };

                portPositionManager.updateNodePortPositions(nodeId, pos, canvasZoom, canvasOffsetX, canvasOffsetY, node, calculator);
            }
        }
    }

    /**
     * 获取端口屏幕位置
     */
    public ImVec2 getPortScreenPosition(UUID nodeId, String portId, Map<UUID, Map<String, ImVec2>> portScreenPositions) {
        if (useFlatPortManager) {
            return flatPortManager.getPortPosition(nodeId, portId);
        }

        Map<String, ImVec2> ports = portScreenPositions.get(nodeId);
        return (ports != null) ? ports.get(portId) : null;
    }

    /**
     * 更新节点尺寸在位置信息中
     */
    private void updateNodeDimensionsInPosition(INode node, Map<UUID, NodePosition> nodePositions, float canvasZoom,
                                                float baseTextLineHeight, float baseItemSpacingY) {
        UUID nodeId = node.getId();
        NodePosition pos = nodePositions.computeIfAbsent(nodeId, id -> new NodePosition(100, 100));

        String nodeDisplayName = node.getDisplayName();
        float unscaledTitleTextWidth = getCachedTextWidth(nodeDisplayName);

        boolean hasInputPorts = !node.getInputPorts().isEmpty();
        boolean hasOutputPorts = !node.getOutputPorts().isEmpty();
        boolean hasAnyPorts = hasInputPorts || hasOutputPorts;

        float maxInputTextWidthUnscaled = 0;
        float maxOutputTextWidthUnscaled = 0;
        if (hasAnyPorts) {
            if (hasInputPorts) {
                for (IPort p : node.getInputPorts()) {
                    maxInputTextWidthUnscaled = Math.max(maxInputTextWidthUnscaled, getCachedTextWidth(p.getDisplayName()));
                }
            }
            if (hasOutputPorts) {
                for (IPort p : node.getOutputPorts()) {
                    maxOutputTextWidthUnscaled = Math.max(maxOutputTextWidthUnscaled, getCachedTextWidth(p.getDisplayName()));
                }
            }
        }

        float unscaledPortsContentWidth = 0;
        if (hasAnyPorts) {
            if (hasInputPorts) {
                unscaledPortsContentWidth += NodeRenderConstants.PORT_RADIUS_UNSCALED * 2 + NodeRenderConstants.PORT_CIRCLE_TO_TEXT_PADDING + maxInputTextWidthUnscaled;
            }
            if (hasOutputPorts) {
                if (hasInputPorts) {
                    unscaledPortsContentWidth += NodeRenderConstants.INTER_PORT_COLUMN_PADDING;
                }
                unscaledPortsContentWidth += maxOutputTextWidthUnscaled + NodeRenderConstants.PORT_CIRCLE_TO_TEXT_PADDING + NodeRenderConstants.PORT_RADIUS_UNSCALED * 2;
            }
        }

        float unscaledEffectiveContentWidth = Math.max(unscaledTitleTextWidth, unscaledPortsContentWidth);

        float customUIUnscaledHeight = 0;
        float customUIUnscaledRequiredWidth = 0;
        boolean hasCustomUI = false;
        ICustomUINode customUINode;

        if (node instanceof ICustomUINode) {
            customUINode = (ICustomUINode) node;
            hasCustomUI = customUINode.hasCustomUI();
            if (hasCustomUI) {
                customUIUnscaledHeight = customUINode.getCustomUIHeight();
                customUIUnscaledRequiredWidth = customUINode.getMinRequiredUIWidth();
            }
        } else {
            boolean shouldCheckReflection = NodeDrawingUtils.shouldCheckReflection(node);
            if (shouldCheckReflection) {
                try {
                    java.lang.reflect.Method hasCustomUIMethod = node.getClass().getMethod("hasCustomUI");
                    hasCustomUI = (Boolean) hasCustomUIMethod.invoke(node);
                    if (hasCustomUI) {
                        try {
                            java.lang.reflect.Method getCustomUIHeightMethod = node.getClass().getMethod("getCustomUIHeight");
                            customUIUnscaledHeight = (Float) getCustomUIHeightMethod.invoke(node);
                        } catch (NoSuchMethodException e) {
                            customUIUnscaledHeight = 0;
                        }
                        try {
                            java.lang.reflect.Method getMinRequiredUIWidthMethod = node.getClass().getMethod("getMinRequiredUIWidth");
                            customUIUnscaledRequiredWidth = (Float) getMinRequiredUIWidthMethod.invoke(node);
                        } catch (NoSuchMethodException e) {
                            customUIUnscaledRequiredWidth = 0;
                        }
                    }
                } catch (Exception e) {
                    hasCustomUI = false;
                    customUIUnscaledHeight = 0;
                    customUIUnscaledRequiredWidth = 0;
                }
            }
        }

        float finalUnscaledNodeWidth = Math.max(NodeRenderConstants.MIN_NODE_WIDTH_UNSCALED, 
                unscaledEffectiveContentWidth + 2 * NodeRenderConstants.NODE_HORIZONTAL_PADDING);
        if (hasCustomUI && customUIUnscaledRequiredWidth > 0) {
            finalUnscaledNodeWidth = Math.max(finalUnscaledNodeWidth, 
                    customUIUnscaledRequiredWidth + 2 * NodeRenderConstants.NODE_HORIZONTAL_PADDING);
        }

        float unscaledNodeHeaderHeight = baseTextLineHeight + 2 * NodeRenderConstants.NODE_VERTICAL_PADDING;

        float unscaledPortsRegionHeight = 0;
        if (hasAnyPorts) {
            int numInputPorts = node.getInputPorts().size();
            int numOutputPorts = node.getOutputPorts().size();
            float inputPortsVisualHeight = (numInputPorts > 0) ? (numInputPorts * baseTextLineHeight + Math.max(0, numInputPorts - 1) * baseItemSpacingY * 0.8f) : 0;
            float outputPortsVisualHeight = (numOutputPorts > 0) ? (numOutputPorts * baseTextLineHeight + Math.max(0, numOutputPorts - 1) * baseItemSpacingY * 0.8f) : 0;
            unscaledPortsRegionHeight = Math.max(inputPortsVisualHeight, outputPortsVisualHeight);
        }

        float finalUnscaledNodeHeight = unscaledNodeHeaderHeight;
        if (unscaledPortsRegionHeight > 0) {
            finalUnscaledNodeHeight += unscaledPortsRegionHeight + NodeRenderConstants.NODE_VERTICAL_PADDING;
        }
        if (hasCustomUI && customUIUnscaledHeight > 0) {
            finalUnscaledNodeHeight += customUIUnscaledHeight + NodeRenderConstants.NODE_VERTICAL_PADDING;
        }

        pos.setSize(finalUnscaledNodeWidth, finalUnscaledNodeHeight);
    }

    private float getCachedTextWidth(String text) {
        if (text == null || text.isEmpty()) return 0f;
        return ImGui.calcTextSize(text).x;
    }

    /**
     * 预计算端口文本大小
     */
    public void precalculatePortTextSizes(INode node) {
        if (node.getInputPorts() != null) {
            for (IPort port : node.getInputPorts()) {
                getCachedTextWidth(port.getDisplayName());
            }
        }
        if (node.getOutputPorts() != null) {
            for (IPort port : node.getOutputPorts()) {
                getCachedTextWidth(port.getDisplayName());
            }
        }
    }
} 