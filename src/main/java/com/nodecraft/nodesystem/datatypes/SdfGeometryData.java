package com.nodecraft.nodesystem.datatypes;

import org.joml.Vector3d;

/**
 * Wraps an SDF with explicit sampling bounds so it can flow through GeometryData pipelines.
 */
public class SdfGeometryData implements GeometryData {
    private final SignedDistanceFieldData sdf;
    private final Vector3d min;
    private final Vector3d max;
    private final double isoValue;

    public SdfGeometryData(SignedDistanceFieldData sdf, Vector3d min, Vector3d max, double isoValue) {
        this.sdf = sdf;
        this.min = new Vector3d(min);
        this.max = new Vector3d(max);
        this.isoValue = isoValue;
    }

    public SignedDistanceFieldData getSdf() {
        return sdf;
    }

    public Vector3d getMin() {
        return new Vector3d(min);
    }

    public Vector3d getMax() {
        return new Vector3d(max);
    }

    public double getIsoValue() {
        return isoValue;
    }
}
