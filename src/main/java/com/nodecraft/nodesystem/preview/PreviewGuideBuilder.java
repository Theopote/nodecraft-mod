package com.nodecraft.nodesystem.preview;

import com.nodecraft.nodesystem.util.BlockPosList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public final class PreviewGuideBuilder {

    private PreviewGuideBuilder() {
    }

    public static Optional<GuideData> fromBlocks(BlockPosList blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return Optional.empty();
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int count = 0;

        for (BlockPos pos : blocks) {
            if (pos == null) {
                continue;
            }
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
            count++;
        }

        if (count == 0) {
            return Optional.empty();
        }

        int length = maxX - minX + 1;
        int height = maxY - minY + 1;
        int width = maxZ - minZ + 1;
        Vec3d pivot = new Vec3d(
            (minX + maxX + 1) * 0.5d,
            (minY + maxY + 1) * 0.5d,
            (minZ + maxZ + 1) * 0.5d
        );
        Vec3d labelPosition = new Vec3d(pivot.x, maxY + 1.35d, pivot.z);
        TextLabelPreviewData dimensionsLabel = new TextLabelPreviewData(
            labelPosition,
            "L" + length + " x W" + width + " x H" + height
        );
        TextLabelPreviewData pivotLabel = new TextLabelPreviewData(
            pivot.add(0.0d, 0.35d, 0.0d),
            String.format("Pivot %.1f, %.1f, %.1f", pivot.x, pivot.y, pivot.z)
        );

        int maxDimension = Math.max(length, Math.max(width, height));
        double axisLength = Math.max(1.0d, Math.min(16.0d, maxDimension * 0.25d));
        Vec3d tangentDirection = dominantAxis(length, width, height);
        FrameAxesPreviewData frameAxes = new FrameAxesPreviewData(
            pivot,
            new Vec3d(1.0d, 0.0d, 0.0d),
            new Vec3d(0.0d, 1.0d, 0.0d),
            new Vec3d(0.0d, 0.0d, 1.0d),
            axisLength
        );

        return Optional.of(new GuideData(
            minX,
            minY,
            minZ,
            maxX,
            maxY,
            maxZ,
            length,
            width,
            height,
            pivot,
            dimensionsLabel,
            pivotLabel,
            frameAxes,
            tangentDirection,
            axisLength
        ));
    }

    private static Vec3d dominantAxis(int length, int width, int height) {
        if (length >= width && length >= height) {
            return new Vec3d(1.0d, 0.0d, 0.0d);
        }
        if (width >= length && width >= height) {
            return new Vec3d(0.0d, 0.0d, 1.0d);
        }
        return new Vec3d(0.0d, 1.0d, 0.0d);
    }

    public record GuideData(
        int minX,
        int minY,
        int minZ,
        int maxX,
        int maxY,
        int maxZ,
        int length,
        int width,
        int height,
        Vec3d pivot,
        TextLabelPreviewData dimensionsLabel,
        TextLabelPreviewData pivotLabel,
        FrameAxesPreviewData frameAxes,
        Vec3d tangentDirection,
        double axisLength
    ) {
    }
}
