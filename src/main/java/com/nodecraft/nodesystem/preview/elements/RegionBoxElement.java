package com.nodecraft.nodesystem.preview.elements;

import com.nodecraft.nodesystem.datatypes.RegionData;
import com.nodecraft.nodesystem.preview.AbstractPreviewElement;
import com.nodecraft.nodesystem.preview.PreviewOptions;
import com.nodecraft.nodesystem.preview.PreviewRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class RegionBoxElement extends AbstractPreviewElement {

    private volatile List<BoundingBox> regions = new ArrayList<>();
    private Vector3f color = new Vector3f(0.2f, 0.7f, 1.0f);
    private boolean enablePulse = false;

    public RegionBoxElement(String id, String ownerNodeId, Object data, PreviewOptions options) {
        super(id, ownerNodeId, data, options);
        this.renderPriority = 15;

        if (options.color != null) {
            this.color = new Vector3f(options.color);
        }
        if (options.pulseAnimation != null) {
            this.enablePulse = options.pulseAnimation;
        }
    }

    @Override
    protected void processData(Object data) {
        List<BoundingBox> nextRegions = new ArrayList<>();

        if (data instanceof List<?> list) {
            for (Object item : list) {
                processItem(nextRegions, item);
            }
            regions = nextRegions;
            return;
        }

        processItem(nextRegions, data);
        regions = nextRegions;
    }

    private void processItem(List<BoundingBox> target, Object item) {
        if (item instanceof BoundingBox box) {
            target.add(box);
        } else if (item instanceof RegionData region && region.isComplete()) {
            BlockPos min = region.getMinCorner();
            BlockPos max = region.getMaxCorner();
            if (min != null && max != null) {
                target.add(new BoundingBox(
                    new Vec3d(min.getX(), min.getY(), min.getZ()),
                    new Vec3d(max.getX() + 1.0d, max.getY() + 1.0d, max.getZ() + 1.0d)
                ));
            }
        } else if (item instanceof Object[] array && array.length >= 2) {
            Vec3d min = extractPosition(array[0]);
            Vec3d max = extractPosition(array[1]);
            if (min != null && max != null) {
                target.add(new BoundingBox(min, max));
            }
        }
    }

    private Vec3d extractPosition(Object obj) {
        if (obj instanceof Vec3d vec) {
            return vec;
        }
        if (obj instanceof BlockPos pos) {
            return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
        }
        return null;
    }

    @Override
    public void render(MatrixStack matrices, Camera camera, float partialTicks, float globalOpacity) {
        List<BoundingBox> regionsSnapshot = regions;
        if (regionsSnapshot.isEmpty()) {
            return;
        }

        float finalOpacity = opacity * globalOpacity;
        if (enablePulse) {
            finalOpacity *= (float) (0.5f + 0.5f * Math.sin(System.currentTimeMillis() * 0.003));
        }
        if (finalOpacity <= 0.01f) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayers.lines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Vec3d cameraPos = camera.getCameraPos();

        for (BoundingBox region : regionsSnapshot) {
            drawBox(vertexConsumer, matrix, region, cameraPos, finalOpacity);
        }

        immediate.draw();
    }

    private void drawBox(VertexConsumer vertexConsumer, Matrix4f matrix, BoundingBox box, Vec3d cameraPos, float alpha) {
        Vec3d min = box.min.subtract(cameraPos);
        Vec3d max = box.max.subtract(cameraPos);

        Vec3d[] vertices = new Vec3d[] {
            new Vec3d(min.x, min.y, min.z),
            new Vec3d(max.x, min.y, min.z),
            new Vec3d(max.x, max.y, min.z),
            new Vec3d(min.x, max.y, min.z),
            new Vec3d(min.x, min.y, max.z),
            new Vec3d(max.x, min.y, max.z),
            new Vec3d(max.x, max.y, max.z),
            new Vec3d(min.x, max.y, max.z)
        };

        int[][] edges = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        for (int[] edge : edges) {
            drawLine(vertexConsumer, matrix, vertices[edge[0]], vertices[edge[1]], alpha);
        }
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
        List<BoundingBox> regionsSnapshot = regions;
        if (regionsSnapshot.isEmpty() || isExpired()) {
            return false;
        }
        Vec3d cameraPos = camera.getCameraPos();
        float maxDistance = PreviewRenderer.getInstance().getSettings().maxRenderDistance;
        for (BoundingBox region : regionsSnapshot) {
            if (cameraPos.distanceTo(region.getCenter()) <= maxDistance + region.getSize()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void cleanup() {
        regions = new ArrayList<>();
    }

    public static class BoundingBox {
        public final Vec3d min;
        public final Vec3d max;

        public BoundingBox(Vec3d min, Vec3d max) {
            this.min = new Vec3d(Math.min(min.x, max.x), Math.min(min.y, max.y), Math.min(min.z, max.z));
            this.max = new Vec3d(Math.max(min.x, max.x), Math.max(min.y, max.y), Math.max(min.z, max.z));
        }

        public Vec3d getCenter() {
            return new Vec3d((min.x + max.x) / 2.0d, (min.y + max.y) / 2.0d, (min.z + max.z) / 2.0d);
        }

        public double getSize() {
            return Math.max(Math.max(max.x - min.x, max.y - min.y), max.z - min.z);
        }
    }
}
