package com.nodecraft.nodesystem.preview.protocol;

import java.util.List;

public final class PreviewVectorsPayload implements PreviewPayload {
    private final List<PreviewVector> vectors;

    public PreviewVectorsPayload(List<PreviewVector> vectors) {
        this.vectors = List.copyOf(vectors);
    }

    @Override
    public PreviewKind getKind() {
        return PreviewKind.VECTORS;
    }

    public List<PreviewVector> getVectors() {
        return vectors;
    }
}
