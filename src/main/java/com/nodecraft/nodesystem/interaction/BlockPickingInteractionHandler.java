package com.nodecraft.nodesystem.interaction;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.minecraft.client.MinecraftClientController;
import com.nodecraft.nodesystem.util.Coordinate;
import net.minecraft.util.hit.BlockHitResult;

final class BlockPickingInteractionHandler implements NodeEditorInteractionManager.InteractionModeHandler {

    private final Runnable cancelInteraction;
    private final Runnable completeInteraction;

    private String currentNodeId;
    private IBlockPickerCallback currentCallback;

    BlockPickingInteractionHandler(Runnable cancelInteraction, Runnable completeInteraction) {
        this.cancelInteraction = cancelInteraction;
        this.completeInteraction = completeInteraction;
    }

    @Override
    public void onEnter(String nodeId, NodeEditorInteractionManager.IInteractionCallback callback) {
        if (!(callback instanceof IBlockPickerCallback)) {
            throw new IllegalArgumentException("方块拾取模式需要IBlockPickerCallback");
        }

        currentNodeId = nodeId;
        currentCallback = (IBlockPickerCallback) callback;

        MinecraftClientController.getInstance().showHudMessage(getHintMessage());
        NodeCraft.LOGGER.info("节点 {} 进入方块拾取模式，回调已设置: {}", nodeId, currentCallback != null);
    }

    @Override
    public void onUpdate(
        Coordinate hoveredBlock,
        BlockHitResult hitResult,
        boolean isLeftMouseClicked,
        boolean isRightMouseClicked
    ) {
        if (NodeCraft.LOGGER.isDebugEnabled() && (isLeftMouseClicked || isRightMouseClicked)) {
            NodeCraft.LOGGER.debug(
                "BlockPickingHandler.onUpdate() - 悬停方块:{} 左键:{} 右键:{}",
                hoveredBlock,
                isLeftMouseClicked,
                isRightMouseClicked
            );
        }

        if (isRightMouseClicked) {
            NodeCraft.LOGGER.info("方块拾取通过右键结束");
            cancelInteraction.run();
            return;
        }

        if (isLeftMouseClicked && hoveredBlock != null && hitResult != null) {
            try {
                if (currentCallback == null) {
                    NodeCraft.LOGGER.error("方块拾取处理器的回调为空！节点ID: {}", currentNodeId);
                    cancelInteraction.run();
                    return;
                }

                BlockSnapshot snapshot = BlockSnapshot.fromHitResult(hitResult);
                NodeCraft.LOGGER.info(
                    "准备调用回调 - 节点: {} 方块: {} 位置: {}",
                    currentNodeId,
                    snapshot.blockId(),
                    hoveredBlock
                );

                currentCallback.onBlockPicked(hoveredBlock, snapshot.blockId(), snapshot.stateData());
                completeInteraction.run();

                NodeCraft.LOGGER.info("方块拾取完成: {} at {}", snapshot.blockId(), hoveredBlock);
            } catch (Exception e) {
                NodeCraft.LOGGER.error("处理方块拾取时出错", e);
                cancelInteraction.run();
            }
        }
    }

    @Override
    public void onCancel() {
        if (currentCallback != null) {
            currentCallback.onPickingCancelled();
        }
        MinecraftClientController.getInstance().clearHudMessage();
        NodeCraft.LOGGER.info("方块拾取已取消");
        resetState();
    }

    @Override
    public void onComplete() {
        MinecraftClientController.getInstance().clearHudMessage();
        resetState();
    }

    @Override
    public String getDisplayName() {
        return "方块拾取";
    }

    @Override
    public String getHintMessage() {
        return "请左键点击一个方块进行拾取";
    }

    private void resetState() {
        currentCallback = null;
        currentNodeId = null;
    }
}
