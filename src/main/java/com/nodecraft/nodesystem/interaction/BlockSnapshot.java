package com.nodecraft.nodesystem.interaction;

import com.nodecraft.nodesystem.util.BlockStateData;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

record BlockSnapshot(String blockId, BlockStateData stateData) {

    static BlockSnapshot fromHitResult(BlockHitResult hitResult) {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = null;
        if (client.world != null) {
            blockState = client.world.getBlockState(blockPos);
        }

        String blockId = null;
        if (blockState != null) {
            blockId = Registries.BLOCK.getId(blockState.getBlock()).toString();
        }

        return new BlockSnapshot(blockId, BlockStateDataMapper.fromBlockState(blockState));
    }
}
