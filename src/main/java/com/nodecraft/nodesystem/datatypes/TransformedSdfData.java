package com.nodecraft.nodesystem.datatypes;

import org.joml.Matrix3d;
import org.joml.Vector3d;

/**
 * Applies translation / rotation / uniform scale to an input SDF.
 */
public class TransformedSdfData implements SignedDistanceFieldData {
    private static final double EPS = 1.0e-9d;

    private final SignedDistanceFieldData source;
    private final Vector3d translation;
    private final Matrix3d inverseRotation;
    private final double scale;

    public TransformedSdfData(SignedDistanceFieldData source,
                              Vector3d translation,
                              double rotationXDeg,
                              double rotationYDeg,
                              double rotationZDeg,
                              double scale) {
        this.source = source;
        this.translation = new Vector3d(translation);
        this.scale = Math.max(EPS, Math.abs(scale));
        Matrix3d rotation = new Matrix3d().rotateXYZ(
            Math.toRadians(rotationXDeg),
            Math.toRadians(rotationYDeg),
            Math.toRadians(rotationZDeg)
        );
        this.inverseRotation = rotation.transpose(new Matrix3d());
    }

    @Override
    public double sampleDistance(Vector3d point) {
        Vector3d local = new Vector3d(point).sub(translation);
        inverseRotation.transform(local);
        local.div(scale);
        return source.sampleDistance(local) * scale;
    }
}
