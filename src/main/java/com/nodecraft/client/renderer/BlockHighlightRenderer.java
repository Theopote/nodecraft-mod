package com.nodecraft.client.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * 方块高亮边框渲染器
 * 当鼠标光标掠过方块时显示高亮边框（绿色呼吸效果）
 * 参考 TreeFactory 的 BlockHighlightRenderer 实现
 */
public final class BlockHighlightRenderer {

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    private BlockHighlightRenderer() {}

    /**
     * 渲染方块高亮边框
     * @param matrices 矩阵栈
     * @param vertexConsumers 顶点消费者提供者
     * @param hit 方块命中结果
     */
    public static void renderBlockOutline(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockHitResult hit) {
        if (MC.world == null || hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        Vec3d camPos = MC.gameRenderer.getCamera().getCameraPos();

        var blockState = MC.world.getBlockState(pos);
        if (blockState == null) {
            return;
        }
        var shape = blockState.getOutlineShape(MC.world, pos);
        if (shape == null || shape.isEmpty()) {
            return;
        }

        Box box = shape.getBoundingBox()
                .offset(pos)
                .offset(-camPos.x, -camPos.y, -camPos.z);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayers.lines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // 高亮颜色（绿色，带呼吸效果）
        long time = System.currentTimeMillis();
        float r = 0.0f;
        float g = 1.0f;
        float b = 0.0f;
        float alpha = 0.35f + (0.5f + 0.5f * (float) Math.sin(time / 400.0)) * 0.45f;

        drawBoxEdges(consumer, matrix, box, r, g, b, alpha);
    }

    /**
     * 绘制方块的12条边
     */
    private static void drawBoxEdges(VertexConsumer consumer, Matrix4f matrix, Box box,
                                     float r, float g, float b, float a) {
        double minX = box.minX, minY = box.minY, minZ = box.minZ;
        double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;

        // 底面4条边
        drawLine(consumer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        drawLine(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        drawLine(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        drawLine(consumer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        // 顶面4条边
        drawLine(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        drawLine(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        drawLine(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        drawLine(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        // 4条垂直边
        drawLine(consumer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        drawLine(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        drawLine(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        drawLine(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    /**
     * 绘制一条线段
     */
    private static void drawLine(VertexConsumer consumer, Matrix4f matrix,
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2,
                                 float r, float g, float b, float a) {
        float lineWidth = 2.0f;
        consumer.vertex(matrix, (float) x1, (float) y1, (float) z1)
                .color(r, g, b, a)
                .normal(0, 1, 0)
                .lineWidth(lineWidth);
        consumer.vertex(matrix, (float) x2, (float) y2, (float) z2)
                .color(r, g, b, a)
                .normal(0, 1, 0)
                .lineWidth(lineWidth);
    }
}
