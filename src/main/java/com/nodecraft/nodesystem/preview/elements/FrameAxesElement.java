package com.nodecraft.nodesystem.preview.elements;

import com.nodecraft.nodesystem.preview.AbstractPreviewElement;
import com.nodecraft.nodesystem.preview.FrameAxesPreviewData;
import com.nodecraft.nodesystem.preview.PreviewOptions;
import com.nodecraft.nodesystem.preview.PreviewRenderer;
import com.nodecraft.nodesystem.preview.protocol.PreviewFramePayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class FrameAxesElement extends AbstractPreviewElement {

    private FrameAxesPreviewData frameData;
    private float lineWidth = 1.5f;

    public FrameAxesElement(String id, String ownerNodeId, Object data, PreviewOptions options) {
        super(id, ownerNodeId, data, options);
        this.renderPriority = 16;
        if (options.lineWidth != null) {
            this.lineWidth = Math.max(0.25f, options.lineWidth);
        }
    }

    @Override
    protected void processData(Object data) {
        if (data instanceof PreviewFramePayload payload) {
            this.frameData = payload.getFrameData();
            return;
        }
        this.frameData = data instanceof FrameAxesPreviewData previewData ? previewData : null;
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, float partialTicks, float globalOpacity) {
        if (frameData == null) {
            return;
        }

        float finalOpacity = opacity * globalOpacity;
        if (finalOpacity <= 0.01f) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayers.lines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Vec3d cameraPos = camera.getCameraPos();

        Vec3d origin = frameData.getOrigin().subtract(cameraPos);
        drawAxis(vertexConsumer, matrix, origin, frameData.getXAxis(), frameData.getAxisLength(), 1.0f, 0.25f, 0.25f, finalOpacity);
        drawAxis(vertexConsumer, matrix, origin, frameData.getYAxis(), frameData.getAxisLength(), 0.25f, 1.0f, 0.25f, finalOpacity);
        drawAxis(vertexConsumer, matrix, origin, frameData.getZAxis(), frameData.getAxisLength(), 0.25f, 0.55f, 1.0f, finalOpacity);

        immediate.draw();
    }

    private void drawAxis(VertexConsumer vertexConsumer, Matrix4f matrix, Vec3d origin, Vec3d axis, double length,
                          float r, float g, float b, float alpha) {
        if (axis.lengthSquared() < 1.0e-9d) {
            return;
        }

        Vec3d normalized = axis.normalize();
        Vec3d end = origin.add(normalized.multiply(length));
        drawLine(vertexConsumer, matrix, origin, end, r, g, b, alpha);

        Vec3d reference = Math.abs(normalized.y) < 0.95d ? new Vec3d(0.0d, 1.0d, 0.0d) : new Vec3d(1.0d, 0.0d, 0.0d);
        Vec3d side = normalized.crossProduct(reference).normalize().multiply(length * 0.12d);
        Vec3d back = normalized.multiply(-length * 0.18d);
        drawLine(vertexConsumer, matrix, end, end.add(back).add(side), r, g, b, alpha);
        drawLine(vertexConsumer, matrix, end, end.add(back).subtract(side), r, g, b, alpha);
    }

    private void drawLine(VertexConsumer vertexConsumer, Matrix4f matrix, Vec3d start, Vec3d end,
                          float r, float g, float b, float alpha) {
        Vector3f normal = new Vector3f((float) (end.x - start.x), (float) (end.y - start.y), (float) (end.z - start.z));
        if (normal.lengthSquared() < 1.0e-12f) {
            normal.set(0.0f, 1.0f, 0.0f);
        } else {
            normal.normalize();
        }

        vertexConsumer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z)
            .color(r, g, b, alpha)
            .normal(normal.x, normal.y, normal.z)
            .lineWidth(lineWidth);
        vertexConsumer.vertex(matrix, (float) end.x, (float) end.y, (float) end.z)
            .color(r, g, b, alpha)
            .normal(normal.x, normal.y, normal.z)
            .lineWidth(lineWidth);
    }

    @Override
    public boolean shouldRender(Camera camera) {
        if (frameData == null || isExpired()) {
            return false;
        }
        double distance = camera.getCameraPos().distanceTo(frameData.getOrigin());
        return distance <= PreviewRenderer.getInstance().getSettings().maxRenderDistance + frameData.getAxisLength();
    }

    @Override
    public void cleanup() {
        frameData = null;
    }
}
