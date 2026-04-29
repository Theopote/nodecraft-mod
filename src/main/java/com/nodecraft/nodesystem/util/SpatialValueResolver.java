package com.nodecraft.nodesystem.util;

import com.nodecraft.nodesystem.datatypes.PointData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

/**
 * Normalizes common spatial value representations into JOML vectors so geometry nodes can
 * consistently consume world positions, centers, and point-like values.
 */
public final class SpatialValueResolver {
    private SpatialValueResolver() {
    }

    public static @Nullable Vector3d resolveVector3d(@Nullable Object value) {
        if (value instanceof PointData pointData) {
            return pointData.getPosition();
        }
        if (value instanceof Coordinate coordinate) {
            return new Vector3d(coordinate.getX(), coordinate.getY(), coordinate.getZ());
        }
        if (value instanceof Vector3 vector) {
            return new Vector3d(vector.getX(), vector.getY(), vector.getZ());
        }
        if (value instanceof Vector3d vector) {
            return new Vector3d(vector);
        }
        if (value instanceof Vec3d vec3d) {
            return new Vector3d(vec3d.x, vec3d.y, vec3d.z);
        }
        if (value instanceof BlockPos blockPos) {
            return new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        return null;
    }

    public static @Nullable BlockPos resolveBlockPos(@Nullable Object value) {
        if (value instanceof BlockPos blockPos) {
            return blockPos;
        }
        Vector3d resolved = resolveVector3d(value);
        if (resolved == null) {
            return null;
        }
        return BlockPos.ofFloored(resolved.x, resolved.y, resolved.z);
    }
}
