package com.nodecraft.nodesystem.preview.elements;

import com.nodecraft.nodesystem.preview.AbstractPreviewElement;
import com.nodecraft.nodesystem.preview.PreviewOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Placeholder for future field/domain visualization previews.
 *
 * This element is intentionally not registered in PreviewRenderer until there is a concrete
 * payload contract and renderer implementation.
 */
public class FieldVisualizationElement extends AbstractPreviewElement {

    public FieldVisualizationElement(String id, String ownerNodeId, Object data, PreviewOptions options) {
        super(id, ownerNodeId, data, options);
    }

    @Override
    protected void processData(Object data) {
        // Future implementation: parse scalar/vector field preview payloads.
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, float partialTicks, float globalOpacity) {
        // Intentionally no-op until field preview payloads are defined.
    }

    @Override
    public boolean shouldRender(Camera camera) {
        return false;
    }

    @Override
    public void cleanup() {
        // No resources allocated yet.
    }
}
