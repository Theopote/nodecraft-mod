package com.nodecraft.nodesystem.util;

import net.minecraft.util.math.BlockPos;

/**
 * 单个方块的放置信息：坐标 + 方块类型 ID。
 * 用于材质代理等节点输出「按位置分配不同方块」的列表。
 */
public record BlockPlacementData(BlockPos pos, String blockId) {
    public BlockPos pos() { return pos != null ? pos.toImmutable() : null; }
}
