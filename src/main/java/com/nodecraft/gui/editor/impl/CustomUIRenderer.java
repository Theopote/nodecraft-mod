package com.nodecraft.gui.editor.impl;

import java.util.UUID;
import com.nodecraft.nodesystem.api.INode;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import com.nodecraft.core.NodeCraft;

/**
 * 自定义UI渲染器
 * 专门处理节点的自定义UI渲染逻辑
 */
public class CustomUIRenderer {
    
    private final ICanvasEditor editor;
    
    public CustomUIRenderer(ICanvasEditor editor) {
        this.editor = editor;
    }

    /**
     * 自定义UI渲染信息
     */
    public static class CustomUIRenderInfo {
        public final INode node;
        public final ICustomUINode customUINode;
        public final UUID nodeId;
        public final float screenX;
        public final float screenY;
        public final float width;
        public final float height;
        public final float zoom;
        public final boolean supportsDirectDrawing;

        public CustomUIRenderInfo(INode node, ICustomUINode customUINode, UUID nodeId,
                           float screenX, float screenY, float width, float height,
                           float zoom, boolean supportsDirectDrawing) {
            this.node = node;
            this.customUINode = customUINode;
            this.nodeId = nodeId;
            this.screenX = screenX;
            this.screenY = screenY;
            this.width = width;
            this.height = height;
            this.zoom = zoom;
            this.supportsDirectDrawing = supportsDirectDrawing;
        }
    }

    /**
     * 渲染单个节点的自定义UI（使用子窗口）
     */
    public void renderSingleCustomUIWithChildWindow(CustomUIRenderInfo info) {
        boolean childStarted = false;
        try {
            ICustomUINode.ContentBounds bounds = null;
            if (info.customUINode != null) {
                bounds = info.customUINode.getContentBounds(info.zoom);
            }

            // 将逻辑尺寸转换为缩放后的像素尺寸
            float scaledWidth = info.width * info.zoom;
            float scaledHeight = info.height * info.zoom;
            
            float safeWidth = scaledWidth;
            float safeHeight = scaledHeight;

            if (bounds != null) {
                safeWidth = Math.max(scaledWidth, bounds.minWidth);
                safeHeight = Math.max(scaledHeight, bounds.minHeight);

                float maxSafeWidth = scaledWidth * 2.0f;
                float maxSafeHeight = scaledHeight * 2.0f;
                safeWidth = Math.min(safeWidth, maxSafeWidth);
                safeHeight = Math.min(safeHeight, maxSafeHeight);
            }

            // === 画布级别缩放变换 ===
            // 保存原始样式状态
            float originalFontScale = ImGui.getIO().getFontGlobalScale();
            float originalFramePaddingX = ImGui.getStyle().getFramePaddingX();
            float originalFramePaddingY = ImGui.getStyle().getFramePaddingY();
            float originalItemSpacingX = ImGui.getStyle().getItemSpacingX();
            float originalItemSpacingY = ImGui.getStyle().getItemSpacingY();
            float originalIndentSpacing = ImGui.getStyle().getIndentSpacing();
            float originalFrameBorderSize = ImGui.getStyle().getFrameBorderSize();
            float originalFrameRounding = ImGui.getStyle().getFrameRounding();
            float originalGrabRounding = ImGui.getStyle().getGrabRounding();
            float originalScrollbarSize = ImGui.getStyle().getScrollbarSize();
            float originalScrollbarRounding = ImGui.getStyle().getScrollbarRounding();
            float originalGrabMinSize = ImGui.getStyle().getGrabMinSize();
            
            // 应用统一的缩放变换
            // 这样 ImGui 控件的所有部分（边框、内边距、交互区域等）都会正确缩放
            float zoom = info.zoom;
            ImGui.getIO().setFontGlobalScale(zoom);
            ImGui.getStyle().setFramePadding(originalFramePaddingX * zoom, originalFramePaddingY * zoom);
            ImGui.getStyle().setItemSpacing(originalItemSpacingX * zoom, originalItemSpacingY * zoom);
            ImGui.getStyle().setIndentSpacing(originalIndentSpacing * zoom);
            ImGui.getStyle().setFrameBorderSize(originalFrameBorderSize * zoom);
            ImGui.getStyle().setFrameRounding(originalFrameRounding * zoom);
            ImGui.getStyle().setGrabRounding(originalGrabRounding * zoom);
            ImGui.getStyle().setScrollbarSize(originalScrollbarSize * zoom);
            ImGui.getStyle().setScrollbarRounding(originalScrollbarRounding * zoom);
            ImGui.getStyle().setGrabMinSize(originalGrabMinSize * zoom);

            try {
                ImGui.setCursorScreenPos(info.screenX, info.screenY);

                String customUIChildId = "custom_ui_" + info.nodeId;

                int windowFlags = ImGuiWindowFlags.NoScrollbar |
                        ImGuiWindowFlags.NoScrollWithMouse |
                        ImGuiWindowFlags.NoBackground |
                        ImGuiWindowFlags.NoDecoration;

                boolean childVisible = ImGui.beginChild(customUIChildId, safeWidth, safeHeight, false, windowFlags);
                childStarted = true;

                if (childVisible && info.customUINode != null) {
                    try {
                        // 在缩放变换下渲染自定义UI
                        // 传递逻辑尺寸给节点，缩放变换已经在样式中处理
                        info.customUINode.renderCustomUI(info.width, info.height, zoom);
                    } catch (Exception e) {
                        NodeCraft.LOGGER.error("自定义UI渲染失败 (节点: {}): {}", info.nodeId, e.getMessage(), e);
                    }
                }
            } finally {
                // 恢复原始样式状态
                ImGui.getIO().setFontGlobalScale(originalFontScale);
                ImGui.getStyle().setFramePadding(originalFramePaddingX, originalFramePaddingY);
                ImGui.getStyle().setItemSpacing(originalItemSpacingX, originalItemSpacingY);
                ImGui.getStyle().setIndentSpacing(originalIndentSpacing);
                ImGui.getStyle().setFrameBorderSize(originalFrameBorderSize);
                ImGui.getStyle().setFrameRounding(originalFrameRounding);
                ImGui.getStyle().setGrabRounding(originalGrabRounding);
                ImGui.getStyle().setScrollbarSize(originalScrollbarSize);
                ImGui.getStyle().setScrollbarRounding(originalScrollbarRounding);
                ImGui.getStyle().setGrabMinSize(originalGrabMinSize);
            }

        } catch (Exception e) {
            NodeCraft.LOGGER.error("自定义UI子窗口创建失败 (节点: {}): {}", info.nodeId, e.getMessage(), e);
        } finally {
            if (childStarted) {
                ImGui.endChild();
            }
        }
    }

    /**
     * 使用防裁剪模式渲染
     */
    private boolean renderWithAntiClippingMode(CustomUIRenderInfo info,
                                               ICustomUINode.ViewPortRenderInfo renderInfo,
                                               ICustomUINode.ContentBounds bounds) {
        boolean uiInteracted = false;

        try {
            imgui.ImVec2 originalClipMin = new imgui.ImVec2();
            imgui.ImVec2 originalClipMax = new imgui.ImVec2();
            ImGui.getWindowDrawList().getClipRectMin(originalClipMin);
            ImGui.getWindowDrawList().getClipRectMax(originalClipMax);

            float expandedClipMinX = Math.min(originalClipMin.x, info.screenX - bounds.marginLeft);
            float expandedClipMinY = Math.min(originalClipMin.y, info.screenY - bounds.marginTop);
            float expandedClipMaxX = Math.max(originalClipMax.x, info.screenX + info.width + bounds.marginRight);
            float expandedClipMaxY = Math.max(originalClipMax.y, info.screenY + info.height + bounds.marginBottom);

            ImGui.getWindowDrawList().pushClipRect(
                    expandedClipMinX, expandedClipMinY,
                    expandedClipMaxX, expandedClipMaxY,
                    false
            );

            uiInteracted = info.customUINode.renderWithViewPortAwareness(
                    renderInfo, bounds.minWidth, bounds.minHeight);

            ImGui.getWindowDrawList().popClipRect();

        } catch (Exception e) {
            System.err.println("防裁剪渲染失败: " + e.getMessage());
            try {
                uiInteracted = info.customUINode.renderCustomUI(
                        info.width, info.height, info.zoom);
            } catch (Exception fallbackError) {
                System.err.println("回退渲染也失败: " + fallbackError.getMessage());
            }
        }
        return uiInteracted;
    }

    /**
     * 通过反射渲染自定义UI
     */
    private boolean renderCustomUIViaReflection(CustomUIRenderInfo info) {
        try {
            java.lang.reflect.Method renderCustomUIMethod = info.node.getClass().getMethod("renderCustomUI", float.class, float.class, float.class);
            return (Boolean) renderCustomUIMethod.invoke(info.node, info.width, info.height, info.zoom);
        } catch (NoSuchMethodException e1) {
            try {
                java.lang.reflect.Method renderCustomUIMethod = info.node.getClass().getMethod("renderCustomUI", float.class, float.class);
                return (Boolean) renderCustomUIMethod.invoke(info.node, info.width, info.height);
            } catch (NoSuchMethodException e2) {
                try {
                    java.lang.reflect.Method renderCustomUIMethod = info.node.getClass().getMethod("renderCustomUI", float.class);
                    return (Boolean) renderCustomUIMethod.invoke(info.node, info.width);
                } catch (Exception e3) {
                    System.err.println("Failed to invoke renderCustomUI method for node " + info.nodeId + " (single-arg): " + e3.getMessage());
                }
            } catch (Exception e2) {
                System.err.println("Failed to invoke renderCustomUI method for node " + info.nodeId + " (two-arg): " + e2.getMessage());
            }
        } catch (Exception e1) {
            System.err.println("Failed to invoke renderCustomUI method for node " + info.nodeId + " (three-arg): " + e1.getMessage());
        }
        return false;
    }

    /**
     * 检查节点是否有自定义UI
     */
    public boolean hasCustomUI(INode node) {
        if (node instanceof ICustomUINode customUINode) {
            return customUINode.hasCustomUI();
        }

        boolean shouldCheckReflection = NodeDrawingUtils.shouldCheckReflection(node);
        if (shouldCheckReflection) {
            try {
                java.lang.reflect.Method hasCustomUIMethod = node.getClass().getMethod("hasCustomUI");
                return (Boolean) hasCustomUIMethod.invoke(node);
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    /**
     * 获取自定义UI的高度
     */
    public float getCustomUIHeight(INode node) {
        if (node instanceof ICustomUINode customUINode) {
            return customUINode.getCustomUIHeight();
        }

        boolean shouldCheckReflection = NodeDrawingUtils.shouldCheckReflection(node);
        if (shouldCheckReflection) {
            try {
                java.lang.reflect.Method getCustomUIHeightMethod = node.getClass().getMethod("getCustomUIHeight");
                return (Float) getCustomUIHeightMethod.invoke(node);
            } catch (NoSuchMethodException e) {
                return 0;
            } catch (Exception e) {
                return 0;
            }
        }

        return 0;
    }

    /**
     * 获取自定义UI的最小宽度
     */
    public float getMinRequiredUIWidth(INode node) {
        if (node instanceof ICustomUINode customUINode) {
            return customUINode.getMinRequiredUIWidth();
        }

        boolean shouldCheckReflection = NodeDrawingUtils.shouldCheckReflection(node);
        if (shouldCheckReflection) {
            try {
                java.lang.reflect.Method getMinRequiredUIWidthMethod = node.getClass().getMethod("getMinRequiredUIWidth");
                return (Float) getMinRequiredUIWidthMethod.invoke(node);
            } catch (NoSuchMethodException e) {
                return 0;
            } catch (Exception e) {
                return 0;
            }
        }

        return 0;
    }
} 