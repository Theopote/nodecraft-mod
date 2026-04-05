package com.nodecraft.nodesystem.preview;

import net.minecraft.util.math.Vec3d;

public class FrameAxesPreviewData {

    private final Vec3d origin;
    private final Vec3d xAxis;
    private final Vec3d yAxis;
    private final Vec3d zAxis;
    private final double axisLength;

    public FrameAxesPreviewData(Vec3d origin, Vec3d xAxis, Vec3d yAxis, Vec3d zAxis, double axisLength) {
        this.origin = origin;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;
        this.axisLength = Math.max(0.25d, axisLength);
    }

    public Vec3d getOrigin() {
        return origin;
    }

    public Vec3d getXAxis() {
        return xAxis;
    }

    public Vec3d getYAxis() {
        return yAxis;
    }

    public Vec3d getZAxis() {
        return zAxis;
    }

    public double getAxisLength() {
        return axisLength;
    }
}
