package com.nodecraft.nodesystem.nodes.reference.frames;

import com.nodecraft.nodesystem.datatypes.PointData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

final class FrameUtils {
    static final double EPS = 1.0e-12d;

    private FrameUtils() {
    }

    static Vector3d resolvePoint(Object value) {
        if (value instanceof PointData pointData) {
            return pointData.getPosition();
        }
        if (value instanceof Vector3d vector) {
            return new Vector3d(vector);
        }
        if (value instanceof Vec3d vector) {
            return new Vector3d(vector.x, vector.y, vector.z);
        }
        if (value instanceof BlockPos blockPos) {
            return new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        return null;
    }

    static boolean isFinite(Vector3d vector) {
        return vector != null
            && Double.isFinite(vector.x)
            && Double.isFinite(vector.y)
            && Double.isFinite(vector.z);
    }

    static boolean isUsableAxis(Vector3d axis) {
        return isFinite(axis) && axis.lengthSquared() > EPS;
    }

    static Vector3d normalizedDirection(Vector3d from, Vector3d to) {
        if (!isFinite(from) || !isFinite(to)) {
            return null;
        }
        Vector3d axis = new Vector3d(to).sub(from);
        if (!isUsableAxis(axis)) {
            return null;
        }
        return axis.normalize();
    }
}
