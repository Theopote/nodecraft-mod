package com.nodecraft.nodesystem.nodes.transform.orientation;

import com.nodecraft.nodesystem.datatypes.PlaneData;
import com.nodecraft.nodesystem.datatypes.PointData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

final class OrientationUtils {
    static final double EPS = 1.0e-12d;

    private OrientationUtils() {
    }

    static Vector3d resolveVector(Object value) {
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

    static boolean isUsableDirection(Vector3d vector) {
        return isFinite(vector) && vector.lengthSquared() > EPS;
    }

    static boolean isUsablePlane(PlaneData plane) {
        return plane != null && isUsableDirection(plane.getNormal());
    }
}
