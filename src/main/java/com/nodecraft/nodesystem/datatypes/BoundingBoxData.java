package com.nodecraft.nodesystem.datatypes;

import org.joml.Vector3d;

import java.util.Objects;

public class BoundingBoxData {
    private final Vector3d min;
    private final Vector3d max;

    // Constructor from min/max vectors
    public BoundingBoxData(Vector3d min, Vector3d max) {
        this.min = new Vector3d(min);
        this.max = new Vector3d(max);
    }

    // Copy constructor
    public BoundingBoxData(BoundingBoxData other) {
        this.min = new Vector3d(other.min);
        this.max = new Vector3d(other.max);
    }

    public Vector3d getMin() {
        return new Vector3d(min);
    }

    public Vector3d getMax() {
        return new Vector3d(max);
    }

    /**
     * 检测点是否在包围盒内（包含边界）
     * @param x 点的x坐标
     * @param y 点的y坐标
     * @param z 点的z坐标
     * @return 如果点在包围盒内返回true，否则返回false
     */
    public boolean testPoint(double x, double y, double z) {
        return x >= min.x && x <= max.x &&
               y >= min.y && y <= max.y &&
               z >= min.z && z <= max.z;
    }

    /**
     * 检测点是否在包围盒内（包含边界）
     * @param point 要检测的点
     * @return 如果点在包围盒内返回true，否则返回false
     */
    public boolean testPoint(Vector3d point) {
        return testPoint(point.x, point.y, point.z);
    }

    @Override
    public String toString() {
        return "BoundingBox[min=" + min + ", max=" + max + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundingBoxData that = (BoundingBoxData) o;
        return Objects.equals(this.min, that.min) && Objects.equals(this.max, that.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
} 