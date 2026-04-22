package com.nodecraft.nodesystem.preview.protocol;

/**
 * Explicit preview payload kinds (v1). Renderers branch on {@link PreviewPayload#getKind()} and concrete type.
 */
public enum PreviewKind {
    BLOCKS,
    POINTS,
    VECTORS,
    CURVES,
    REGIONS,
    PLANE,
    FRAME,
    LABELS,
    SURFACE_STRIP,
    GEOMETRY
}
