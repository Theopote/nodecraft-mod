package com.nodecraft.nodesystem.preview;

import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewSamplingTest {

    @Test
    void returnsFullListWhenUnderLimit() {
        BlockPosList blocks = blocks(4);

        PreviewSampling.BlockSample sample = PreviewSampling.sampleBlocks(blocks, 10);

        assertFalse(sample.sampled());
        assertEquals(4, sample.totalCount());
        assertEquals(4, sample.renderedCount());
        assertEquals(4, sample.blocks().size());
    }

    @Test
    void deterministicallySamplesLargeBlockLists() {
        BlockPosList blocks = blocks(10);

        PreviewSampling.BlockSample sample = PreviewSampling.sampleBlocks(blocks, 3);

        assertTrue(sample.sampled());
        assertEquals(10, sample.totalCount());
        assertEquals(3, sample.renderedCount());
        assertEquals(4, sample.stride());
        assertEquals(new BlockPos(0, 64, 0), sample.blocks().getPositions().get(0));
        assertEquals(new BlockPos(4, 64, 0), sample.blocks().getPositions().get(1));
        assertEquals(new BlockPos(8, 64, 0), sample.blocks().getPositions().get(2));
    }

    @Test
    void clampsInvalidLimitToOne() {
        BlockPosList blocks = blocks(3);

        PreviewSampling.BlockSample sample = PreviewSampling.sampleBlocks(blocks, 0);

        assertTrue(sample.sampled());
        assertEquals(1, sample.renderedCount());
    }

    private static BlockPosList blocks(int count) {
        BlockPosList blocks = new BlockPosList();
        for (int i = 0; i < count; i++) {
            blocks.add(new BlockPos(i, 64, 0));
        }
        return blocks;
    }
}
