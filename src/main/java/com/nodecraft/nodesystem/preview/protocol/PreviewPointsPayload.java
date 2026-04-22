package com.nodecraft.nodesystem.preview.protocol;

import java.util.List;

public final class PreviewPointsPayload implements PreviewPayload {
    private final List<PreviewPoint> points;

    public PreviewPointsPayload(List<PreviewPoint> points) {
        this.points = List.copyOf(points);
    }

    @Override
    public PreviewKind getKind() {
        return PreviewKind.POINTS;
    }

    public List<PreviewPoint> getPoints() {
        return points;
    }
}
