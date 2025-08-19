package com.nodecraft.nodesystem.datatypes;

import org.joml.Vector3d;
import java.util.Objects;

public class PointData {
    private final Vector3d position;

    public PointData(Vector3d position) {
        this.position = new Vector3d(position); // Defensive copy
    }

    public PointData(double x, double y, double z) {
        this.position = new Vector3d(x, y, z);
    }

    public Vector3d getPosition() {
        return new Vector3d(position); // Return defensive copy
    }

    public double getX() { return position.x; }
    public double getY() { return position.y; }
    public double getZ() { return position.z; }

    @Override
    public String toString() {
        return "Point[" + position.x + ", " + position.y + ", " + position.z + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointData pointData = (PointData) o;
        return Objects.equals(position, pointData.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }
} 