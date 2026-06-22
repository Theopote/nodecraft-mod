package com.nodecraft.nodesystem.preview;

import net.minecraft.util.math.Vec3d;

/**
 * Data payload for {@link com.nodecraft.nodesystem.preview.elements.TransformationGizmoElement}.
 */
public final class TransformGizmoPreviewData {

    private final Vec3d origin;
    private final Vec3d xAxis;
    private final Vec3d yAxis;
    private final Vec3d zAxis;
    private final double baseAxisLength;
    private final String gizmoType;

    public TransformGizmoPreviewData(Vec3d origin) {
        this(origin, new Vec3d(1.0d, 0.0d, 0.0d), new Vec3d(0.0d, 1.0d, 0.0d), new Vec3d(0.0d, 0.0d, 1.0d), 1.0d, "all");
    }

    public TransformGizmoPreviewData(
        Vec3d origin,
        Vec3d xAxis,
        Vec3d yAxis,
        Vec3d zAxis,
        double baseAxisLength,
        String gizmoType
    ) {
        this.origin = origin == null ? Vec3d.ZERO : origin;
        this.xAxis = normalizeOrDefault(xAxis, new Vec3d(1.0d, 0.0d, 0.0d));
        this.yAxis = normalizeOrDefault(yAxis, new Vec3d(0.0d, 1.0d, 0.0d));
        this.zAxis = normalizeOrDefault(zAxis, new Vec3d(0.0d, 0.0d, 1.0d));
        this.baseAxisLength = Math.max(0.25d, baseAxisLength);
        this.gizmoType = gizmoType == null || gizmoType.isBlank() ? "all" : gizmoType.trim().toLowerCase();
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

    public double getBaseAxisLength() {
        return baseAxisLength;
    }

    public String getGizmoType() {
        return gizmoType;
    }

    public boolean showsMove() {
        return "all".equals(gizmoType) || "move".equals(gizmoType) || "translate".equals(gizmoType);
    }

    public boolean showsRotate() {
        return "all".equals(gizmoType) || "rotate".equals(gizmoType) || "rotation".equals(gizmoType);
    }

    public boolean showsScale() {
        return "all".equals(gizmoType) || "scale".equals(gizmoType);
    }

    private static Vec3d normalizeOrDefault(Vec3d axis, Vec3d fallback) {
        if (axis == null || axis.lengthSquared() < 1.0e-9d) {
            return fallback;
        }
        return axis.normalize();
    }
}
