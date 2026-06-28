package com.nodecraft.gui.preset;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.gui.editor.impl.ImGuiNodeEditor;
import com.nodecraft.nodesystem.api.INode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GraphPresetApplier {

    private static final float MIN_NODE_HORIZONTAL_GAP = 260.0f;
    private static final float MIN_NODE_VERTICAL_GAP = 130.0f;
    private static final int MAX_LAYOUT_ADJUSTMENTS_PER_NODE = 64;

    public record ApplyResult(boolean success, String message, List<UUID> createdNodeIds) {
        public static ApplyResult failure(String message) {
            return new ApplyResult(false, message, List.of());
        }

        public static ApplyResult success(String message, List<UUID> createdNodeIds) {
            return new ApplyResult(true, message, List.copyOf(createdNodeIds));
        }
    }

    private GraphPresetApplier() {
    }

    public static ApplyResult apply(GraphPresetRules.GraphPresetDefinition preset, float originX, float originY) {
        if (preset == null) {
            return ApplyResult.failure("Preset is missing");
        }
        if ("placeholder".equalsIgnoreCase(preset.kind)) {
            return ApplyResult.failure("该预设仍在筹备中");
        }
        if (!"composite".equalsIgnoreCase(preset.kind)) {
            return ApplyResult.failure("Unsupported preset kind: " + preset.kind);
        }
        if (preset.nodes == null || preset.nodes.isEmpty()) {
            return ApplyResult.failure("Preset has no nodes");
        }

        ImGuiNodeEditor editor = ImGuiNodeEditor.getInstance();
        if (editor == null || editor.getCurrentGraph() == null) {
            return ApplyResult.failure("Editor is not ready");
        }

        Map<String, UUID> refToNodeId = new HashMap<>();
        List<UUID> createdNodeIds = new ArrayList<>();
        Map<String, LayoutPosition> layoutPositions = resolveLayoutPositions(preset.nodes);

        for (GraphPresetRules.PresetNode presetNode : preset.nodes) {
            if (presetNode == null || presetNode.ref == null || presetNode.typeId == null) {
                continue;
            }
            LayoutPosition position = layoutPositions.getOrDefault(
                    presetNode.ref,
                    new LayoutPosition(presetNode.x, presetNode.y));
            INode created = editor.addNode(
                    presetNode.typeId,
                    originX + position.x,
                    originY + position.y);
            if (created == null) {
                rollback(editor, createdNodeIds);
                return ApplyResult.failure("Failed to create node: " + presetNode.typeId);
            }
            refToNodeId.put(presetNode.ref, created.getId());
            createdNodeIds.add(created.getId());
        }

        if (preset.connections != null) {
            for (GraphPresetRules.PresetConnection connection : preset.connections) {
                if (connection == null) {
                    continue;
                }
                UUID sourceNodeId = refToNodeId.get(connection.fromRef);
                UUID targetNodeId = refToNodeId.get(connection.toRef);
                if (sourceNodeId == null || targetNodeId == null) {
                    NodeCraft.LOGGER.warn(
                            "Skipping preset connection with unknown node reference in {}: {} -> {}",
                            preset.id,
                            connection.fromRef,
                            connection.toRef);
                    continue;
                }

                boolean connected = editor.connectPorts(
                        sourceNodeId,
                        connection.fromPort,
                        targetNodeId,
                        connection.toPort);
                if (!connected) {
                    NodeCraft.LOGGER.warn(
                            "Skipping invalid preset connection in {}: {}.{} -> {}.{}",
                            preset.id,
                            connection.fromRef,
                            connection.fromPort,
                            connection.toRef,
                            connection.toPort);
                }
            }
        }

        editor.clearSelectedNodes();
        editor.getSelectedNodeIds().addAll(createdNodeIds);
        if (!createdNodeIds.isEmpty()) {
            editor.setSelectedNodeId(createdNodeIds.get(0));
        }

        NodeCraft.LOGGER.info("Applied graph preset {} ({} nodes)", preset.displayName, createdNodeIds.size());
        return ApplyResult.success("已添加预设: " + preset.displayName, createdNodeIds);
    }

    private static void rollback(ImGuiNodeEditor editor, List<UUID> createdNodeIds) {
        Set<UUID> ids = new HashSet<>(createdNodeIds);
        editor.getSelectedNodeIds().clear();
        editor.getSelectedNodeIds().addAll(ids);
        if (!ids.isEmpty()) {
            editor.setSelectedNodeId(ids.iterator().next());
            editor.deleteSelectedNodes();
        }
    }

    private static Map<String, LayoutPosition> resolveLayoutPositions(List<GraphPresetRules.PresetNode> presetNodes) {
        Map<String, LayoutPosition> positionsByRef = new HashMap<>();
        List<LayoutPosition> placedPositions = new ArrayList<>();

        for (GraphPresetRules.PresetNode presetNode : presetNodes) {
            if (presetNode == null || presetNode.ref == null) {
                continue;
            }

            float x = presetNode.x;
            float y = presetNode.y;
            int attempts = 0;
            while (overlapsPlacedNode(x, y, placedPositions) && attempts < MAX_LAYOUT_ADJUSTMENTS_PER_NODE) {
                y += MIN_NODE_VERTICAL_GAP;
                attempts++;
            }

            if (attempts >= MAX_LAYOUT_ADJUSTMENTS_PER_NODE && overlapsPlacedNode(x, y, placedPositions)) {
                x += MIN_NODE_HORIZONTAL_GAP;
                y = presetNode.y;
                attempts = 0;
                while (overlapsPlacedNode(x, y, placedPositions) && attempts < MAX_LAYOUT_ADJUSTMENTS_PER_NODE) {
                    y += MIN_NODE_VERTICAL_GAP;
                    attempts++;
                }
            }

            LayoutPosition position = new LayoutPosition(x, y);
            positionsByRef.put(presetNode.ref, position);
            placedPositions.add(position);
        }

        return positionsByRef;
    }

    private static boolean overlapsPlacedNode(float x, float y, List<LayoutPosition> placedPositions) {
        for (LayoutPosition placed : placedPositions) {
            if (Math.abs(x - placed.x) < MIN_NODE_HORIZONTAL_GAP
                    && Math.abs(y - placed.y) < MIN_NODE_VERTICAL_GAP) {
                return true;
            }
        }
        return false;
    }

    private record LayoutPosition(float x, float y) {
    }
}
