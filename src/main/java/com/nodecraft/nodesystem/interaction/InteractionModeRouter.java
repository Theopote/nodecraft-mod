package com.nodecraft.nodesystem.interaction;

import com.nodecraft.nodesystem.util.Coordinate;
import net.minecraft.util.hit.BlockHitResult;

import java.util.Map;

final class InteractionModeRouter {

    @FunctionalInterface
    interface BlockPickFallback {
        void handle(Coordinate coordinate, BlockHitResult hitResult);
    }

    private final Map<NodeEditorInteractionManager.EditorInteractionMode, NodeEditorInteractionManager.InteractionModeHandler> modeHandlers;
    private final InteractionSessionState interactionState;
    private final BlockPickFallback blockPickFallback;

    InteractionModeRouter(
        Map<NodeEditorInteractionManager.EditorInteractionMode, NodeEditorInteractionManager.InteractionModeHandler> modeHandlers,
        InteractionSessionState interactionState,
        BlockPickFallback blockPickFallback
    ) {
        this.modeHandlers = modeHandlers;
        this.interactionState = interactionState;
        this.blockPickFallback = blockPickFallback;
    }

    void route(
        Coordinate hoveredBlock,
        BlockHitResult hitResult,
        boolean isLeftMouseClicked,
        boolean isRightMouseClicked
    ) {
        if (!interactionState.isInInteractionMode()) {
            return;
        }

        NodeEditorInteractionManager.EditorInteractionMode currentMode = interactionState.getMode();
        NodeEditorInteractionManager.InteractionModeHandler handler = modeHandlers.get(currentMode);

        if (handler != null) {
            handler.onUpdate(hoveredBlock, hitResult, isLeftMouseClicked, isRightMouseClicked);
            return;
        }

        if (interactionState.isPendingBlockPick() && hoveredBlock != null && hitResult != null) {
            blockPickFallback.handle(hoveredBlock, hitResult);
        }
    }
}
