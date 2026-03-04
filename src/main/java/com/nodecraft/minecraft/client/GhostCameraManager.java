package com.nodecraft.minecraft.client;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.nodesystem.preview.PreviewOptions;
import com.nodecraft.nodesystem.preview.PreviewRenderer;
import com.nodecraft.nodesystem.util.Coordinate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * 幽灵相机模式管理器 (已修正)
 * 用于在NodeCraft编辑器打开时提供一种沉浸式的编辑体验。
 * 包含方块高亮显示功能，并直接利用Minecraft原生的准星目标检测。
 */
public class GhostCameraManager {
    
    private static GhostCameraManager instance;
    
    // 原始设置保存
    private Perspective originalPerspective;
    private boolean originalHudHidden;
    private double originalMouseSensitivity;
    private boolean originalAutoJump;
    
    // 状态管理
    private volatile boolean enabled = false; // 使用 volatile 保证线程可见性
    
    // 方块高亮相关
    private Coordinate currentHighlightedBlock = null;
    private String currentPreviewId = null; // 保存当前预览的ID
    
    // 新增：多种高亮模式
    public enum HighlightMode {
        SIMPLE,         // 简单高亮
        DETAILED,       // 详细信息高亮
        COMPARISON,     // 比较模式高亮
        DISABLED        // 禁用高亮
    }

    private GhostCameraManager() {
        // 私有构造函数，单例模式
    }
    
    public static synchronized GhostCameraManager getInstance() {
        if (instance == null) {
            instance = new GhostCameraManager();
        }
        return instance;
    }

    // ================= 幽灵相机模式管理 =================
    
    /**
     * 启用幽灵相机模式
     */
    public void enable() {
        if (enabled) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        // 保存原始设置
        this.originalPerspective = client.options.getPerspective();
        this.originalHudHidden = client.options.hudHidden;
        this.originalMouseSensitivity = client.options.getMouseSensitivity().getValue();
        this.originalAutoJump = client.options.getAutoJump().getValue();

        // 应用幽灵相机设置
        client.options.setPerspective(Perspective.FIRST_PERSON);
        client.options.hudHidden = true;
        client.options.getMouseSensitivity().setValue(originalMouseSensitivity * 0.8);
        client.options.getAutoJump().setValue(false);

        enabled = true;
        NodeCraft.LOGGER.info("幽灵相机模式已启用");
    }
    
    /**
     * 禁用幽灵相机模式
     */
    public void disable() {
        if (!enabled) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        // 清除方块高亮 - 强制清除所有相关预览
        forceCleanupBlockHighlight();

        // 恢复原始设置
        client.options.setPerspective(
            this.originalPerspective != null ? this.originalPerspective : Perspective.FIRST_PERSON
        );
        client.options.hudHidden = this.originalHudHidden;
        client.options.getMouseSensitivity().setValue(this.originalMouseSensitivity);
        client.options.getAutoJump().setValue(this.originalAutoJump);

        enabled = false;
        NodeCraft.LOGGER.info("幽灵相机模式已禁用，恢复原始设置");
    }
    
    /**
     * 检查幽灵相机模式是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 切换幽灵相机模式
     */
    public void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }
    
    // ================= 方块高亮功能 =================
    
    /**
     * 获取当前的方块命中结果 (修正版)
     * 这个方法直接利用Minecraft原生的crosshairTarget，这是最可靠的方式。
     * @return 命中的方块结果，如果未命中或命中类型不是方块，则返回null。
     */
    public BlockHitResult getCurrentBlockHit() {
        MinecraftClient client = MinecraftClient.getInstance();
        // 检查基本条件：客户端存在且幽灵模式启用
        if (client == null || !enabled) {
            return null;
        }
        
        // 注意：不检查 client.isPaused()，因为打开NodeCraft窗口时游戏会被"暂停"
        // 但我们仍然需要进行方块检测
        
        HitResult hitResult = client.crosshairTarget;
        
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            // 额外检查，确保目标不是空气方块
            if (client.world != null && !client.world.getBlockState(blockPos).isAir()) {
                return (BlockHitResult) hitResult;
            }
        }
        
        return null;
    }
    
    /**
     * 更新方块高亮显示
     * 这个方法逻辑是正确的，它依赖于 getCurrentBlockHit() 的结果。
     */
    public void updateBlockHighlight() {
        boolean blockHighlightEnabled = true;
        if (!enabled || !blockHighlightEnabled) {
            clearBlockHighlight(); // 如果禁用了，确保清除高亮
            return;
        }
        
        BlockHitResult hitResult = getCurrentBlockHit();
        Coordinate newHighlightedBlock = null;
        
        // 添加调试信息
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            HitResult crosshairTarget = client.crosshairTarget;
            if (crosshairTarget == null) {
                // 每100次调用输出一次调试信息，避免日志过多
                if (System.currentTimeMillis() % 1000 < 50) { // 大约每秒输出一次
                    NodeCraft.LOGGER.debug("crosshairTarget为null，可能鼠标没有指向任何物体");
                }
            } else if (crosshairTarget.getType() != HitResult.Type.BLOCK) {
                if (System.currentTimeMillis() % 1000 < 50) {
                    NodeCraft.LOGGER.debug("crosshairTarget类型: {}, 不是方块", crosshairTarget.getType());
                }
            }
        }
        
        if (hitResult != null) {
            BlockPos blockPos = hitResult.getBlockPos();
            newHighlightedBlock = new Coordinate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        
        if (java.util.Objects.equals(currentHighlightedBlock, newHighlightedBlock)) {
            return;
        }
        
        // 只有当高亮方块变化时才更新，减少日志输出
        if (newHighlightedBlock != null) {
            NodeCraft.LOGGER.info("方块高亮变化: {} -> {}", currentHighlightedBlock, newHighlightedBlock);
        } else {
            NodeCraft.LOGGER.info("清除方块高亮: {}", currentHighlightedBlock);
        }
        
        clearBlockHighlight();
        
        if (newHighlightedBlock != null) {
            showBlockHighlight(newHighlightedBlock);
        }
        
        currentHighlightedBlock = newHighlightedBlock;
    }
    
    /**
     * 显示方块高亮边框
     */
    private void showBlockHighlight(Coordinate blockPos) {
        try {
            // 创建高亮预览选项 - 使用醒目的黄色
            PreviewOptions options = new PreviewOptions()
                .setColor(1.0f, 1.0f, 0.0f) // 黄色，更醒目
                .setOpacity(0.8f) // 稍微透明
                .wireframeMode()
                .setLineWidth(3.0f) // 适中的线宽
                .enablePulse(); // 启用脉冲动画效果
            
            currentPreviewId = PreviewRenderer.getInstance().showPreview(
                "ghost_camera", // ownerNodeId - 使用幽灵相机作为拥有者
                "block_highlight", // previewType
                blockPos, // data
                options // options
            );
            
            NodeCraft.LOGGER.info("已创建黄色高亮预览，位置: {}, 线宽: 3.0, ID: {}", blockPos, currentPreviewId);
            
        } catch (Exception e) {
            NodeCraft.LOGGER.error("显示方块高亮时出错", e);
        }
    }
    
    /**
     * 清除当前的方块高亮
     */
    private void clearBlockHighlight() {
        try {
            // 使用保存的预览ID清除预览
            if (currentPreviewId != null) {
                PreviewRenderer.getInstance().hidePreview(currentPreviewId);
                currentPreviewId = null;
            }
        } catch (Exception e) {
            NodeCraft.LOGGER.error("清除方块高亮时出错", e);
        }
        currentHighlightedBlock = null;
    }
    
    /**
     * 强制清除方块高亮 - 更彻底的清理
     */
    private void forceCleanupBlockHighlight() {
        try {
            // 1. 清除特定的高亮预览
            if (currentPreviewId != null) {
                PreviewRenderer.getInstance().hidePreview(currentPreviewId);
                currentPreviewId = null;
            }
            
            // 2. 清除所有可能的幽灵相机相关预览
            PreviewRenderer.getInstance().hidePreviewsByNode("ghost_camera");
            
            // 3. 重置状态
            currentHighlightedBlock = null;
            
            NodeCraft.LOGGER.info("强制清理方块高亮完成");
        } catch (Exception e) {
            NodeCraft.LOGGER.error("强制清理方块高亮时出错", e);
        }
    }
    
    /**
     * 强制清理
     * 在某些异常情况下确保设置被恢复
     */
    public void forceCleanup() {
        if (enabled) {
            disable();
        } else {
            // 即使没有启用，也要清理可能残留的预览
            forceCleanupBlockHighlight();
        }
    }

    
    /**
     * 相机控制设置
     */
    private static class CameraControlSettings {
        // 相机移动设置
        public volatile float movementSpeed = 1.0f;
        public volatile float sprintMultiplier = 2.0f;
        public volatile float sneakMultiplier = 0.3f;
        
        // 鼠标灵敏度设置
        public volatile float mouseSensitivity = 1.0f;
        public volatile boolean invertY = false;
        
        // 高亮设置
        public volatile boolean enableBlockHighlight = true;
        public volatile float highlightDistance = 64.0f;
        public volatile boolean enableDetailedInfo = false;
        
        // 渲染设置
        public volatile boolean enableXRay = false;
        public volatile boolean enableNightVision = false;
        public volatile float gamma = 1.0f;
        
        // 交互设置
        public volatile boolean enableBlockPicking = true;
        public volatile boolean enableAreaSelection = true;
        public volatile boolean showCoordinates = true;
        
        /**
         * 应用设置到Minecraft客户端
         */
        public void applyToClient() {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                // 应用鼠标灵敏度
                double originalSensitivity = client.options.getMouseSensitivity().getValue();
                client.options.getMouseSensitivity().setValue(originalSensitivity * mouseSensitivity);
                
                // 应用伽马值
                client.options.getGamma().setValue((double) gamma);
            }
        }
        
        /**
         * 恢复默认设置
         */
        public void restoreDefaults() {
            movementSpeed = 1.0f;
            sprintMultiplier = 2.0f;
            sneakMultiplier = 0.3f;
            mouseSensitivity = 1.0f;
            invertY = false;
            enableBlockHighlight = true;
            highlightDistance = 64.0f;
            enableDetailedInfo = false;
            enableXRay = false;
            enableNightVision = false;
            gamma = 1.0f;
            enableBlockPicking = true;
            enableAreaSelection = true;
            showCoordinates = true;
        }
    }
} 