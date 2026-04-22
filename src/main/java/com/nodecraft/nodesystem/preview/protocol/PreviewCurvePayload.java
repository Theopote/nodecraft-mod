package com.nodecraft.nodesystem.preview.protocol;

import java.util.List;

public final class PreviewCurvePayload implements PreviewPayload {
    private final List<PreviewPoint> points;
    private final boolean closed;

    public PreviewCurvePayload(List<PreviewPoint> points, boolean closed) {
        this.points = List.copyOf(points);
        this.closed = closed;
    }

    @Override
    public PreviewKind getKind() {
        return PreviewKind.CURVES;
    }

    public List<PreviewPoint> getPoints() {
        return points;
    }

    public boolean closed() {
        return closed;
    }
}
