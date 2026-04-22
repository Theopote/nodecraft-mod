package com.nodecraft.nodesystem.preview.protocol;

import java.util.List;

public final class PreviewBlocksPayload implements PreviewPayload {
    private final List<PreviewBlock> blocks;

    public PreviewBlocksPayload(List<PreviewBlock> blocks) {
        this.blocks = List.copyOf(blocks);
    }

    @Override
    public PreviewKind getKind() {
        return PreviewKind.BLOCKS;
    }

    public List<PreviewBlock> getBlocks() {
        return blocks;
    }
}
