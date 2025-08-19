package com.nodecraft.minecraft.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import com.nodecraft.core.NodeCraft;

/**
 * Minecraft客户端控制器
 * 负责管理NodeCraft与Minecraft客户端的交互，包括：
 * - 十字星显示控制
 * - HUD元素管理
 * - 玩家输入控制
 * - 游戏交互禁用/启用
 */
public class MinecraftClientController {
    
    private static volatile MinecraftClientController instance;
    private final MinecraftClient client;
    
    // 状态管理
    private boolean nodecraftModeActive = false;
    private Double originalMouseSensitivity = null;
    
    // HUD消息状态
    private String currentHudMessage = null;
    private int hudMessageTicks = 0;
    private static final int HUD_MESSAGE_DURATION = 100; // 5秒（20 ticks/秒）
    
    private MinecraftClientController() {
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * 获取单例实例
     */
    public static MinecraftClientController getInstance() {
        if (instance == null) {
            synchronized (MinecraftClientController.class) {
                if (instance == null) {
                    instance = new MinecraftClientController();
                }
            }
        }
        return instance;
    }
    
    /**
     * 激活NodeCraft模式
     * 这会：
     * 1. 隐藏十字星
     * 2. 禁用默认的方块交互
     * 3. 保存当前设置以便恢复
     */
    public void activateNodeCraftMode() {
        if (nodecraftModeActive || client.player == null) {
            return;
        }
        
        try {
            // 保存原始设置
            GameOptions options = client.options;
            originalMouseSensitivity = options.getMouseSensitivity().getValue();
            
            // 这里我们需要通过其他方式控制十字星
            // 由于Minecraft没有直接的"隐藏十字星"选项，我们使用Mixin
            
            nodecraftModeActive = true;
            
            NodeCraft.LOGGER.info("NodeCraft模式已激活 - 十字星已隐藏，游戏交互已禁用");
            
        } catch (Exception e) {
            NodeCraft.LOGGER.error("激活NodeCraft模式时出错", e);
        }
    }
    
    /**
     * 停用NodeCraft模式
     * 恢复所有Minecraft设置到原始状态
     */
    public void deactivateNodeCraftMode() {
        if (!nodecraftModeActive) {
            return;
        }
        
        try {
            // 恢复原始设置
            if (originalMouseSensitivity != null) {
                client.options.getMouseSensitivity().setValue(originalMouseSensitivity);
            }
            
            // 清除HUD消息
            clearHudMessage();
            
            nodecraftModeActive = false;
            originalMouseSensitivity = null;
            
            NodeCraft.LOGGER.info("NodeCraft模式已停用 - 十字星已恢复，游戏交互已启用");
            
        } catch (Exception e) {
            NodeCraft.LOGGER.error("停用NodeCraft模式时出错", e);
        }
    }
    
    /**
     * 检查是否应该隐藏十字星
     * 这个方法会被Mixin调用
     * 只要NodeCraft界面打开就隐藏十字星，不管是否在拾取模式
     */
    public boolean shouldHideCrosshair() {
        // 检查是否有NodeCraft界面打开
        if (client != null && client.currentScreen instanceof com.nodecraft.gui.screens.NodecraftScreen) {
            return true; // 始终在NodeCraft界面打开时隐藏十字星
        }
        
        // 或者如果明确激活了NodeCraft模式也隐藏
        return nodecraftModeActive;
    }

    /**
     * 显示游戏内HUD消息
     * @param message 要显示的消息
     */
    public void showHudMessage(String message) {
        if (client.player == null) {
            return;
        }
        
        try {
            currentHudMessage = message;
            hudMessageTicks = HUD_MESSAGE_DURATION;
            
            // 使用Minecraft的原生HUD系统显示消息
            if (client.inGameHud != null) {
                client.inGameHud.setOverlayMessage(Text.literal(message), false);
            }
            
            NodeCraft.LOGGER.debug("显示HUD消息: {}", message);
            
        } catch (Exception e) {
            NodeCraft.LOGGER.error("显示HUD消息时出错", e);
        }
    }
    
    /**
     * 清除HUD消息
     */
    public void clearHudMessage() {
        if (currentHudMessage == null) {
            return;
        }
        
        try {
            currentHudMessage = null;
            hudMessageTicks = 0;
            
            if (client.inGameHud != null) {
                client.inGameHud.setOverlayMessage(Text.empty(), false);
            }
            
            NodeCraft.LOGGER.debug("清除HUD消息");
            
        } catch (Exception e) {
            NodeCraft.LOGGER.error("清除HUD消息时出错", e);
        }
    }
    
    /**
     * 更新HUD消息状态
     * 应该在每个客户端tick中调用
     */
    public void updateHudMessage() {
        if (currentHudMessage != null && hudMessageTicks > 0) {
            hudMessageTicks--;
            if (hudMessageTicks <= 0) {
                clearHudMessage();
            }
        }
    }
    
    /**
     * 处理方块右键点击事件
     * 这个方法会被Mixin调用来处理NodeCraft的方块拾取
     * 
     * 注意：此方法保留是为了向后兼容性。建议新的实现直接使用
     * NodeEditorInteractionManager 的统一交互逻辑（左键点击）。
     * 
     * @param x 方块X坐标
     * @param y 方块Y坐标
     * @param z 方块Z坐标
     * @param blockId 方块ID
     * @param blockStateData 方块状态数据
     * @return 是否已处理此事件（如果返回true，则阻止默认处理）
     * @deprecated 建议使用 NodeEditorInteractionManager 的左键点击交互逻辑
     */
    @Deprecated
    public boolean handleBlockRightClick(int x, int y, int z, String blockId, com.nodecraft.nodesystem.util.BlockStateData blockStateData) {
        if (!nodecraftModeActive) {
            return false; // 不在NodeCraft模式，不处理
        }
        
        try {
            // 获取交互管理器并转发事件
            com.nodecraft.nodesystem.interaction.NodeEditorInteractionManager interactionManager = 
                com.nodecraft.nodesystem.interaction.NodeEditorInteractionManager.getInstance();
            
            com.nodecraft.nodesystem.util.Coordinate position = new com.nodecraft.nodesystem.util.Coordinate(x, y, z);
            
            // 为了消除废弃警告，此方法内部调用未废弃的左键处理逻辑。
            // 功能上是等价的，因为它们都调用同一个核心拾取方法。
            boolean handled = interactionManager.handleBlockLeftClick(position, blockId, blockStateData);
            
            if (handled) {
                NodeCraft.LOGGER.debug("NodeCraft通过(兼容模式的)右键点击处理了方块拾取: {} at ({}, {}, {})", 
                    blockId, x, y, z);
            }
            
            return handled;
            
        } catch (Exception e) {
            NodeCraft.LOGGER.error("处理方块右键点击时出错", e);
            return false;
        }
    }

    /**
     * 强制清理所有状态（用于紧急情况）
     */
    public void forceCleanup() {
        if (nodecraftModeActive) {
            NodeCraft.LOGGER.warn("强制清理MinecraftClientController状态");
            deactivateNodeCraftMode();
        }
    }
} 