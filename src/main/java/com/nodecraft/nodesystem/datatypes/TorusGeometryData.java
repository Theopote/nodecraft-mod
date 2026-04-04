package com.nodecraft.nodesystem.datatypes;

import org.joml.Vector3d;

import java.util.Objects;

/**
 * Represents an oriented torus geometry.
 */
public class TorusGeometryData {
    private final Vector3d center;
    private final Vector3d axis;
    private final double majorRadius;
    private final double minorRadius;

    public TorusGeometryData(Vector3d center, Vector3d axis, double majorRadius, double minorRadius) {
        this.center = new Vector3d(center);
        this.axis = new Vector3d(axis).normalize();
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    public Vector3d getCenter() {
        return new Vector3d(center);
    }

    public Vector3d getAxis() {
        return new Vector3d(axis);
    }

    public double getMajorRadius() {
        return majorRadius;
    }

    public double getMinorRadius() {
        return minorRadius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TorusGeometryData that)) return false;
        return Double.compare(majorRadius, that.majorRadius) == 0
            && Double.compare(minorRadius, that.minorRadius) == 0
            && Objects.equals(center, that.center)
            && Objects.equals(axis, that.axis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(center, axis, majorRadius, minorRadius);
    }

    @Override
    public String toString() {
        return "TorusGeometryData{center=" + center
            + ", axis=" + axis
            + ", majorRadius=" + majorRadius
            + ", minorRadius=" + minorRadius + "}";
    }
}
