package com.nodecraft.nodesystem.preview.gizmo;

/**
 * Interactive handles exposed by the transformation gizmo.
 */
public enum GizmoHandle {
    AXIS_X,
    AXIS_Y,
    AXIS_Z,
    RING_X,
    RING_Y,
    RING_Z,
    SCALE_X,
    SCALE_Y,
    SCALE_Z,
    SCALE_UNIFORM;

    public boolean isTranslation() {
        return this == AXIS_X || this == AXIS_Y || this == AXIS_Z;
    }

    public boolean isRotation() {
        return this == RING_X || this == RING_Y || this == RING_Z;
    }

    public boolean isScale() {
        return this == SCALE_X || this == SCALE_Y || this == SCALE_Z || this == SCALE_UNIFORM;
    }
}
