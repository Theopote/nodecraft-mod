package com.nodecraft.nodesystem.datatypes;

import org.joml.Vector3d;

import java.util.List;
import java.util.Objects;

/**
 * Represents a square pyramid defined by a base center and an oriented square base.
 */
public final class SquarePyramidGeometryData implements GeometryData {

    private final Vector3d baseCenter;
    private final Vector3d xAxis;
    private final Vector3d yAxis;
    private final Vector3d normal;
    private final double baseSize;
    private final double height;

    public SquarePyramidGeometryData(
        Vector3d baseCenter,
        Vector3d xAxis,
        Vector3d yAxis,
        Vector3d normal,
        double baseSize,
        double height
    ) {
        this.baseCenter = new Vector3d(Objects.requireNonNull(baseCenter, "baseCenter"));
        this.xAxis = new Vector3d(Objects.requireNonNull(xAxis, "xAxis")).normalize();
        this.yAxis = new Vector3d(Objects.requireNonNull(yAxis, "yAxis")).normalize();
        this.normal = new Vector3d(Objects.requireNonNull(normal, "normal")).normalize();
        this.baseSize = baseSize;
        this.height = height;
    }

    public Vector3d getBaseCenter() {
        return new Vector3d(baseCenter);
    }

    public Vector3d getXAxis() {
        return new Vector3d(xAxis);
    }

    public Vector3d getYAxis() {
        return new Vector3d(yAxis);
    }

    public Vector3d getNormal() {
        return new Vector3d(normal);
    }

    public double getBaseSize() {
        return baseSize;
    }

    public double getHeight() {
        return height;
    }

    public Vector3d getApex() {
        return new Vector3d(baseCenter).fma(height, normal);
    }

    public List<Vector3d> getBaseVertices() {
        double half = baseSize * 0.5d;
        Vector3d halfX = new Vector3d(xAxis).mul(half);
        Vector3d halfY = new Vector3d(yAxis).mul(half);
        return List.of(
            new Vector3d(baseCenter).sub(halfX).sub(halfY),
            new Vector3d(baseCenter).add(halfX).sub(halfY),
            new Vector3d(baseCenter).add(halfX).add(halfY),
            new Vector3d(baseCenter).sub(halfX).add(halfY)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SquarePyramidGeometryData that)) return false;
        return Double.compare(baseSize, that.baseSize) == 0
            && Double.compare(height, that.height) == 0
            && Objects.equals(baseCenter, that.baseCenter)
            && Objects.equals(xAxis, that.xAxis)
            && Objects.equals(yAxis, that.yAxis)
            && Objects.equals(normal, that.normal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseCenter, xAxis, yAxis, normal, baseSize, height);
    }

    @Override
    public String toString() {
        return "SquarePyramidGeometryData{baseSize=" + baseSize + ", height=" + height + "}";
    }
}
