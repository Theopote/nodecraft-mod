package com.nodecraft.nodesystem.preview;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.nodesystem.util.Coordinate;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * 预览管理器
 * 提供高级 API 来简化预览系统的使用
 */
public class PreviewManager {
    
    private static final PreviewRenderer renderer = PreviewRenderer.getInstance();

    // ================= 方块高亮 API =================

    /**
     * 高亮单个方块
     */
    public static String highlightBlock(String nodeId, Coordinate position) {
        return highlightBlock(nodeId, position, PreviewOptions.createBlockHighlight());
    }
    
    /**
     * 高亮单个方块（自定义选项）
     */
    public static String highlightBlock(String nodeId, Coordinate position, PreviewOptions options) {
        try {
            String previewId = renderer.showPreview(nodeId, "block_highlight", position, options);

            if (previewId == null) {
                NodeCraft.LOGGER.error("PreviewManager.highlightBlock 失败: renderer.showPreview 返回 null");
            }

            return previewId;
        } catch (Exception e) {
            NodeCraft.LOGGER.error("PreviewManager.highlightBlock 异常: 节点={}, 位置={}", nodeId, position, e);
            return null;
        }
    }

    /**
     * 高亮多个方块
     */
    public static String highlightBlocks(String nodeId, List<Coordinate> positions) {
        return highlightBlocks(nodeId, positions, PreviewOptions.createBlockHighlight());
    }

    /**
     * 高亮多个方块（自定义选项）
     */
    public static String highlightBlocks(String nodeId, List<Coordinate> positions, PreviewOptions options) {
        return renderer.showPreview(nodeId, "block_highlight", positions, options);
    }

    // ================= 幽灵方块 API =================

    /**
     * 显示幽灵方块
     */
    public static String showGhostBlocks(String nodeId, List<Coordinate> positions) {
        return showGhostBlocks(nodeId, positions, new PreviewOptions().ghostBlockMode().setOpacity(0.5f));
    }

    /**
     * 显示幽灵方块（自定义选项）
     */
    public static String showGhostBlocks(String nodeId, List<Coordinate> positions, PreviewOptions options) {
        return renderer.showPreview(nodeId, "ghost_block", positions, options);
    }

    // ================= 区域框 API =================

    /**
     * 显示区域框
     */
    public static String showRegionBox(String nodeId, Vec3d min, Vec3d max) {
        return showRegionBox(nodeId, min, max, PreviewOptions.createRegionBox());
    }

    /**
     * 显示区域框（自定义选项）
     */
    public static String showRegionBox(String nodeId, Vec3d min, Vec3d max, PreviewOptions options) {
        Object[] regionData = {min, max};
        return renderer.showPreview(nodeId, "region_box", regionData, options);
    }

    // ================= 点显示 API =================

    /**
     * 显示点
     */
    public static String showPoints(String nodeId, List<Coordinate> points) {
        return showPoints(nodeId, points, PreviewOptions.createPoints());
    }

    /**
     * 显示点（自定义选项）
     */
    public static String showPoints(String nodeId, List<Coordinate> points, PreviewOptions options) {
        return renderer.showPreview(nodeId, "points", points, options);
    }

    // ================= 向量箭头 API =================

    /**
     * 显示向量箭头
     */
    public static String showVectors(String nodeId, List<Vec3d> vectors, List<Vec3d> startPoints) {
        return showVectors(nodeId, vectors, startPoints, PreviewOptions.createVectorArrows());
    }

    /**
     * 显示向量箭头（自定义选项）
     */
    public static String showVectors(String nodeId, List<Vec3d> vectors, List<Vec3d> startPoints, PreviewOptions options) {
        Object[] vectorData = {vectors, startPoints};
        return renderer.showPreview(nodeId, "vectors", vectorData, options);
    }

    // ================= 变换 Gizmo API =================

    /**
     * 显示变换 Gizmo
     */
    public static String showTransformGizmo(String nodeId, Vec3d center) {
        return showTransformGizmo(nodeId, center, PreviewOptions.createTransformGizmo());
    }

    /**
     * 显示变换 Gizmo（自定义选项）
     */
    public static String showTransformGizmo(String nodeId, Vec3d center, PreviewOptions options) {
        return renderer.showPreview(nodeId, "transformation_gizmo", center, options);
    }

    // ================= 通用控制 API =================
    
    /**
     * 隐藏预览
     */
    public static void hidePreview(String previewId) {
        renderer.hidePreview(previewId);
    }
    
    /**
     * 隐藏节点的所有预览
     */
    public static void hideNodePreviews(String nodeId) {
        renderer.hidePreviewsByNode(nodeId);
    }

    /**
     * 清除所有预览
     */
    public static void clearAllPreviews() {
        renderer.clearAllPreviews();
    }

    /**
     * 更新预览数据
     */
    public static void updatePreview(String previewId, Object newData) {
        renderer.updatePreview(previewId, newData, null);
    }

    /**
     * 更新预览选项
     */
    public static void updatePreviewOptions(String previewId, PreviewOptions newOptions) {
        renderer.updatePreview(previewId, null, newOptions);
    }

    // ================= 配置 API =================

    /**
     * 设置全局预览开关
     */
    public static void setGlobalPreviewEnabled(boolean enabled) {
        renderer.setGlobalPreviewEnabled(enabled);
    }

    /**
     * 设置全局透明度
     */
    public static void setGlobalOpacity(float opacity) {
        renderer.setGlobalOpacity(opacity);
    }

    /**
     * 获取渲染设置
     */
    public static PreviewRenderer.PreviewRenderSettings getSettings() {
        return renderer.getSettings();
    }

    // ================= 便捷方法 =================

    /**
     * 创建自定义颜色的方块高亮选项
     */
    public static PreviewOptions createColoredHighlight(float r, float g, float b, float opacity) {
        return new PreviewOptions()
                .setColor(r, g, b)
                .setOpacity(opacity)
                .wireframeMode()
                .setLineWidth(2.0f);
    }

    /**
     * 创建脉冲动画的区域框选项
     */
    public static PreviewOptions createPulsingRegionBox(float r, float g, float b) {
        return new PreviewOptions()
                .setColor(r, g, b)
                .setOpacity(0.4f)
                .enablePulse()
                .setLineWidth(1.5f);
    }

    /**
     * 创建半透明幽灵方块选项
     */
    public static PreviewOptions createTransparentGhostBlocks(float opacity) {
        return new PreviewOptions()
                .ghostBlockMode()
                .setOpacity(opacity);
    }
} 