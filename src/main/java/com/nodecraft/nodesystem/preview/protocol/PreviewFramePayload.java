package com.nodecraft.nodesystem.preview.protocol;

import com.nodecraft.nodesystem.preview.FrameAxesPreviewData;

/**
 * v1 wrapper for local frame axes preview.
 */
public final class PreviewFramePayload implements PreviewPayload {
    private final FrameAxesPreviewData frameData;

    public PreviewFramePayload(FrameAxesPreviewData frameData) {
        this.frameData = frameData;
    }

    @Override
    public PreviewKind getKind() {
        return PreviewKind.FRAME;
    }

    public FrameAxesPreviewData getFrameData() {
        return frameData;
    }
}
