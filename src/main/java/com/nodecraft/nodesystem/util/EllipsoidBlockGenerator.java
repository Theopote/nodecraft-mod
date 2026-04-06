package com.nodecraft.nodesystem.util;

import com.nodecraft.nodesystem.datatypes.EllipsoidGeometryData;
import com.nodecraft.nodesystem.datatypes.RegionData;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;

public final class EllipsoidBlockGenerator {

    public enum VoxelMode {
        SOLID,
        SHELL
    }

    private EllipsoidBlockGenerator() {
    }

    public static RegionData createBoundingRegion(EllipsoidGeometryData geometry) {
        Vector3d center = geometry.getCenter();
        Vector3d radii = geometry.getRadii();

        BlockPos minCorner = BlockPos.ofFloored(
            center.x - radii.x,
            center.y - radii.y,
            center.z - radii.z
        );
        BlockPos maxCorner = BlockPos.ofFloored(
            center.x + radii.x,
            center.y + radii.y,
            center.z + radii.z
        );
        return new RegionData(minCorner, maxCorner);
    }

    public static void populateEllipsoid(BlockPosList blocks,
                                         RegionData region,
                                         EllipsoidGeometryData geometry,
                                         boolean fillSolid) {
        populateEllipsoid(blocks, region, geometry, fillSolid ? VoxelMode.SOLID : VoxelMode.SHELL, 1.0d);
    }

    public static void populateEllipsoid(BlockPosList blocks,
                                         RegionData region,
                                         EllipsoidGeometryData geometry,
                                         VoxelMode voxelMode,
                                         double shellThickness) {
        if (region == null || !region.isComplete()) {
            return;
        }

        BlockPos minCorner = region.getMinCorner();
        BlockPos maxCorner = region.getMaxCorner();
        if (minCorner == null || maxCorner == null) {
            return;
        }

        Vector3d center = geometry.getCenter();
        Vector3d radii = geometry.getRadii();
        boolean fillSolid = voxelMode == null || voxelMode == VoxelMode.SOLID;

        double innerRx = Math.max(0.0d, radii.x - Math.max(0.0d, shellThickness));
        double innerRy = Math.max(0.0d, radii.y - Math.max(0.0d, shellThickness));
        double innerRz = Math.max(0.0d, radii.z - Math.max(0.0d, shellThickness));
        boolean hasInner = innerRx > 0.0d && innerRy > 0.0d && innerRz > 0.0d;

        for (int x = minCorner.getX(); x <= maxCorner.getX(); x++) {
            for (int y = minCorner.getY(); y <= maxCorner.getY(); y++) {
                for (int z = minCorner.getZ(); z <= maxCorner.getZ(); z++) {
                    double dx = x - center.x;
                    double dy = y - center.y;
                    double dz = z - center.z;
                    double outerDist = normalizedDistance(dx, dy, dz, radii.x, radii.y, radii.z);
                    if (outerDist > 1.0d) {
                        continue;
                    }

                    if (!fillSolid && hasInner) {
                        double innerDist = normalizedDistance(dx, dy, dz, innerRx, innerRy, innerRz);
                        if (innerDist < 1.0d) {
                            continue;
                        }
                    }

                    blocks.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    private static double normalizedDistance(double dx, double dy, double dz, double rx, double ry, double rz) {
        if (rx <= 0.0d || ry <= 0.0d || rz <= 0.0d) {
            return Double.POSITIVE_INFINITY;
        }
        return (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) + (dz * dz) / (rz * rz);
    }
}
