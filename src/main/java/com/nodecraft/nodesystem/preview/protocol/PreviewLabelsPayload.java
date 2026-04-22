package com.nodecraft.nodesystem.preview.protocol;

/**
 * v1 wrapper for text label preview data (single {@link com.nodecraft.nodesystem.preview.TextLabelPreviewData} or list thereof).
 */
public final class PreviewLabelsPayload implements PreviewPayload {
    private final Object labelData;

    public PreviewLabelsPayload(Object labelData) {
        this.labelData = labelData;
    }

    @Override
    public PreviewKind getKind() {
        return PreviewKind.LABELS;
    }

    public Object getLabelData() {
        return labelData;
    }
}
