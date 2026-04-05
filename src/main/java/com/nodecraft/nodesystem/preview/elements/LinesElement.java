package com.nodecraft.nodesystem.preview.elements;

import com.nodecraft.nodesystem.datatypes.LineData;
import com.nodecraft.nodesystem.datatypes.PolylineData;
import com.nodecraft.nodesystem.preview.AbstractPreviewElement;
import com.nodecraft.nodesystem.preview.PreviewOptions;
import com.nodecraft.nodesystem.preview.PreviewRenderer;
import com.nodecraft.nodesystem.util.Curve;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class LinesElement extends AbstractPreviewElement {

    private final List<Vec3d> points = new ArrayList<>();
    private Vector3f color = new Vector3f(1.0f, 0.85f, 0.2f);
    private boolean smoothCurves = false;

    public LinesElement(String id, String ownerNodeId, Object data, PreviewOptions options) {
        super(id, ownerNodeId, data, options);
        this.renderPriority = 17;

        if (options.color != null) {
            this.color = new Vector3f(options.color);
        }
        if (options.smoothCurves != null) {
            this.smoothCurves = options.smoothCurves;
        }
    }

    @Override
    protected void processData(Object data) {
        points.clear();

        if (data instanceof LineData line) {
            points.add(line.getStart());
            points.add(line.getEnd());
        } else if (data instanceof PolylineData polyline) {
            points.addAll(polyline.getPoints());
        } else if (data instanceof Curve curve) {
            points.addAll(curve.getSamplePoints());
        } else if (data instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Vec3d vec) {
                    points.add(vec);
                }
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, float partialTicks, float globalOpacity) {
        if (points.size() < 2) {
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

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3d start = points.get(i).subtract(cameraPos);
            Vec3d end = points.get(i + 1).subtract(cameraPos);
            drawLine(vertexConsumer, matrix, start, end, finalOpacity);
        }

        immediate.draw();
    }

    private void drawLine(VertexConsumer vertexConsumer, Matrix4f matrix, Vec3d start, Vec3d end, float alpha) {
        Vector3f normal = new Vector3f((float) (end.x - start.x), (float) (end.y - start.y), (float) (end.z - start.z));
        if (normal.lengthSquared() < 1.0e-12f) {
            normal.set(0.0f, 1.0f, 0.0f);
        } else {
            normal.normalize();
        }

        vertexConsumer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z)
            .color(color.x(), color.y(), color.z(), alpha)
            .normal(normal.x, normal.y, normal.z);
        vertexConsumer.vertex(matrix, (float) end.x, (float) end.y, (float) end.z)
            .color(color.x(), color.y(), color.z(), alpha)
            .normal(normal.x, normal.y, normal.z);
    }

    @Override
    public boolean shouldRender(Camera camera) {
        if (points.size() < 2 || isExpired()) {
            return false;
        }
        float maxDistance = PreviewRenderer.getInstance().getSettings().maxRenderDistance;
        Vec3d cameraPos = camera.getCameraPos();
        for (Vec3d point : points) {
            if (cameraPos.distanceTo(point) <= maxDistance) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void cleanup() {
        points.clear();
    }
}
