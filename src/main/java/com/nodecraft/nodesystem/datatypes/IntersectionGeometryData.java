package com.nodecraft.nodesystem.datatypes;

import java.util.Objects;

/**
 * Geometry value that represents a voxel-evaluated boolean intersection.
 * Only voxels occupied by both operands are kept.
 */
public final class IntersectionGeometryData implements GeometryData {

    private final GeometryData left;
    private final GeometryData right;

    public IntersectionGeometryData(GeometryData left, GeometryData right) {
        this.left = Objects.requireNonNull(left, "left");
        this.right = Objects.requireNonNull(right, "right");
    }

    public GeometryData getLeft() {
        return left;
    }

    public GeometryData getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntersectionGeometryData that)) return false;
        return Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "IntersectionGeometryData{left=" + left + ", right=" + right + "}";
    }
}
