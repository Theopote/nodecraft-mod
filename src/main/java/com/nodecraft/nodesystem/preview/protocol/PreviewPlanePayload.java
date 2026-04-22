package com.nodecraft.nodesystem.preview.protocol;

import com.nodecraft.nodesystem.preview.PlaneGridPreviewData;

/**
 * v1 wrapper for plane grid preview; {@link PlaneGridPreviewData} remains the concrete layout until fully protocolized.
 */
public final class PreviewPlanePayload implements PreviewPayload {
    private final PlaneGridPreviewData planeGridData;

    public PreviewPlanePayload(PlaneGridPreviewData planeGridData) {
        this.planeGridData = planeGridData;
    }

    @Override
    public PreviewKind getKind() {
        return PreviewKind.PLANE;
    }

    public PlaneGridPreviewData getPlaneGridData() {
        return planeGridData;
    }
}
