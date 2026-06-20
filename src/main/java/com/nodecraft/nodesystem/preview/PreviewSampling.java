package com.nodecraft.nodesystem.preview;

import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic preview-only downsampling utilities.
 */
public final class PreviewSampling {

    private PreviewSampling() {
    }

    public static BlockSample sampleBlocks(BlockPosList blocks, int maxBlocks) {
        if (blocks == null || blocks.isEmpty()) {
            return new BlockSample(new BlockPosList(), 0, 0, false, 1);
        }

        List<BlockPos> positions = blocks.getPositions();
        int total = positions.size();
        int limit = Math.max(1, maxBlocks);
        if (total <= limit) {
            return new BlockSample(new BlockPosList(positions), total, total, false, 1);
        }

        int stride = Math.max(1, (int) Math.ceil(total / (double) limit));
        List<BlockPos> sampled = new ArrayList<>(limit);
        for (int i = 0; i < total && sampled.size() < limit; i += stride) {
            BlockPos pos = positions.get(i);
            if (pos != null) {
                sampled.add(pos.toImmutable());
            }
        }

        return new BlockSample(new BlockPosList(sampled), total, sampled.size(), true, stride);
    }

    public record BlockSample(
        BlockPosList blocks,
        int totalCount,
        int renderedCount,
        boolean sampled,
        int stride
    ) {
    }
}
