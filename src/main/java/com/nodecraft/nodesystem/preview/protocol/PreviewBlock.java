package com.nodecraft.nodesystem.preview.protocol;

import java.util.Objects;

/**
 * One block cell in world space. Coordinates are world-space doubles; consumers snap to cells as needed.
 */
public final class PreviewBlock {
    private final double x;
    private final double y;
    private final double z;
    private final String blockId;

    public PreviewBlock(double x, double y, double z, String blockId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = Objects.requireNonNull(blockId, "blockId");
        if (blockId.isEmpty()) {
            throw new IllegalArgumentException("blockId must be non-empty");
        }
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public String blockId() {
        return blockId;
    }
}
