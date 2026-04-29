package com.nodecraft.nodesystem.nodes.math.fields;

import com.nodecraft.nodesystem.util.SpatialValueResolver;
import org.joml.Vector3d;

final class FieldSampleUtils {
    private FieldSampleUtils() {
    }

    static Vector3d resolvePoint(Object value) {
        return SpatialValueResolver.resolveVector3d(value);
    }
}
