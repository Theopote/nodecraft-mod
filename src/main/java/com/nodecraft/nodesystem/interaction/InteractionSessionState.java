package com.nodecraft.nodesystem.interaction;

import java.util.Map;

final class InteractionSessionState {

    private final Map<NodeEditorInteractionManager.EditorInteractionMode, NodeEditorInteractionManager.InteractionModeHandler> modeHandlers;

    private NodeEditorInteractionManager.EditorInteractionMode mode = NodeEditorInteractionManager.EditorInteractionMode.NONE;
    private NodeEditorInteractionManager.IInteractionCallback callback;
    private String nodeId;

    InteractionSessionState(
        Map<NodeEditorInteractionManager.EditorInteractionMode, NodeEditorInteractionManager.InteractionModeHandler> modeHandlers
    ) {
        this.modeHandlers = modeHandlers;
    }

    void setInteraction(
        NodeEditorInteractionManager.EditorInteractionMode mode,
        String nodeId,
        NodeEditorInteractionManager.IInteractionCallback callback
    ) {
        clear();
        this.mode = mode;
        this.callback = callback;
        this.nodeId = nodeId;
    }

    void clear() {
        if (mode != NodeEditorInteractionManager.EditorInteractionMode.NONE) {
            NodeEditorInteractionManager.InteractionModeHandler handler = modeHandlers.get(mode);
            if (handler != null) {
                handler.onCancel();
            }
        }

        mode = NodeEditorInteractionManager.EditorInteractionMode.NONE;
        callback = null;
        nodeId = null;
    }

    void complete() {
        if (mode != NodeEditorInteractionManager.EditorInteractionMode.NONE) {
            NodeEditorInteractionManager.InteractionModeHandler handler = modeHandlers.get(mode);
            if (handler != null) {
                handler.onComplete();
            }
        }

        mode = NodeEditorInteractionManager.EditorInteractionMode.NONE;
        callback = null;
        nodeId = null;
    }

    NodeEditorInteractionManager.EditorInteractionMode getMode() {
        return mode;
    }

    IBlockPickerCallback getBlockPickerCallback() {
        return callback instanceof IBlockPickerCallback ? (IBlockPickerCallback) callback : null;
    }

    boolean isPendingBlockPick() {
        return mode == NodeEditorInteractionManager.EditorInteractionMode.BLOCK_PICKING && callback != null;
    }

    boolean isPendingBlockPick(String nodeId) {
        return nodeId != null
            && nodeId.equals(this.nodeId)
            && mode == NodeEditorInteractionManager.EditorInteractionMode.BLOCK_PICKING;
    }

    boolean isInInteractionMode() {
        return mode != NodeEditorInteractionManager.EditorInteractionMode.NONE;
    }

    boolean isCurrentInteractionNode(String nodeId) {
        return nodeId != null
            && nodeId.equals(this.nodeId)
            && mode != NodeEditorInteractionManager.EditorInteractionMode.NONE;
    }
}
