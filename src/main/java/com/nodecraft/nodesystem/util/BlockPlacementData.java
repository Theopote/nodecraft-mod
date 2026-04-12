package com.nodecraft.nodesystem.util;

import net.minecraft.util.math.BlockPos;

/**
 * Immutable placement data for a single block position, block id, and optional state overrides.
 */
public record BlockPlacementData(BlockPos pos, String blockId, BlockStateData stateData) {

    public BlockPlacementData(BlockPos pos, String blockId) {
        this(pos, blockId, null);
    }

    public BlockPlacementData {
        pos = pos != null ? pos.toImmutable() : null;
        stateData = stateData != null ? stateData.copy() : null;
    }

    @Override
    public BlockPos pos() {
        return pos != null ? pos.toImmutable() : null;
    }

    @Override
    public BlockStateData stateData() {
        return stateData != null ? stateData.copy() : null;
    }
}
