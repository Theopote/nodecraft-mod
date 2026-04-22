package com.nodecraft.nodesystem.preview.protocol;

/**
 * All preview requests carry a {@link PreviewKind} and a stable protocol version for logging / export.
 */
public interface PreviewPayload {

    PreviewKind getKind();

    /** Protocol revision; bump when breaking wire shape. */
    default int protocolVersion() {
        return 1;
    }
}
