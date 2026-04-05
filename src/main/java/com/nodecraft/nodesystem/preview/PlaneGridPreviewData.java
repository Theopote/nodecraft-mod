package com.nodecraft.nodesystem.preview;

import com.nodecraft.nodesystem.datatypes.PlaneData;

public class PlaneGridPreviewData {

    private final PlaneData plane;
    private final double gridSize;
    private final double gridSpacing;
    private final boolean showAxes;
    private final boolean showNormal;

    public PlaneGridPreviewData(PlaneData plane, double gridSize, double gridSpacing, boolean showAxes, boolean showNormal) {
        this.plane = plane;
        this.gridSize = Math.max(1.0d, gridSize);
        this.gridSpacing = Math.max(0.25d, gridSpacing);
        this.showAxes = showAxes;
        this.showNormal = showNormal;
    }

    public PlaneData getPlane() {
        return plane;
    }

    public double getGridSize() {
        return gridSize;
    }

    public double getGridSpacing() {
        return gridSpacing;
    }

    public boolean isShowAxes() {
        return showAxes;
    }

    public boolean isShowNormal() {
        return showNormal;
    }
}
