package com.nodecraft.nodesystem.preview.protocol;

public final class PreviewPoint {
    private final double x;
    private final double y;
    private final double z;

    public PreviewPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }
}
