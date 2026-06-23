package com.nodecraft.gui.editor.base;

import com.nodecraft.nodesystem.api.INode;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

/**
 * Editor surface used by AI services to mutate the live graph with undo support.
 *
 * <p>Implementations must be invoked from the editor/render thread.</p>
 */
public interface GraphApplyTarget {

    @Nullable
    INode addNode(String typeId, float x, float y);

    @Nullable
    INode addNodeWithState(String typeId, @Nullable UUID oldNodeId, float x, float y, @Nullable Object nodeState);

    boolean connectPorts(UUID sourceNodeId, String sourcePortId, UUID targetNodeId, String targetPortId);

    boolean disconnectPorts(UUID sourceNodeId, String sourcePortId, UUID targetNodeId, String targetPortId);

    boolean undo();

    void recordAiPatchApply(String summary, Map<UUID, Object> previousStates, int undoStepsTaken);

    GraphApplyHistoryView getApplyHistoryView();

    @Nullable
    GraphNodeAnchor getNodeAnchor(UUID nodeId);
}
