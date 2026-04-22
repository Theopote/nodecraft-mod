package com.nodecraft.nodesystem.preview.protocol;

import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.preview.PreviewBackend;
import org.jetbrains.annotations.Nullable;

/**
 * Unified preview show request: payload + style + backend. World-backed backends need {@link #executionContext()}.
 */
public record PreviewRequest(
    String ownerNodeId,
    PreviewPayload payload,
    PreviewStyle style,
    PreviewBackend backend,
    @Nullable ExecutionContext executionContext
) {
}
