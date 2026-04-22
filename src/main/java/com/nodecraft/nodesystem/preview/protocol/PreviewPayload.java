package com.nodecraft.nodesystem.preview.protocol;

/**
 * All preview requests carry a {@link PreviewKind} and a stable protocol version for logging / export.
 */
public interface PreviewPayload {

    PreviewKind getKind();

    /**
     * Wire / schema revision (v1.1 清单中的 {@code getVersion()}).
     * Bump when breaking payload shape on the wire or in persistence.
     */
    default int getVersion() {
        return 1;
    }

    /**
     * @deprecated 使用 {@link #getVersion()}；保留别名以免外部工具按旧名扫描。
     */
    @Deprecated
    default int protocolVersion() {
        return getVersion();
    }
}
