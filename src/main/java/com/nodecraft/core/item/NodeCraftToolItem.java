package com.nodecraft.core.item;

import com.nodecraft.core.NodeCraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.Screen;

/**
 * NodeCraft工具物品
 * 右键点击时会打开NodeCraft节点编辑器界面
 */
public class NodeCraftToolItem extends Item {
    
    public NodeCraftToolItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        // 只在客户端打开界面
        if (world.isClient) {
            NodeCraft.LOGGER.info("NodeCraft工具右键点击 - 准备打开编辑器界面");
            
            // 播放音效
            world.playSound(
                user,
                user.getBlockPos(),
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.BLOCKS,
                1.0F,
                1.0F
            );
            
            try {
                // 尝试通过反射创建 NodecraftScreen，避免直接类引用
                Class<?> nodecraftScreenClass = Class.forName("com.nodecraft.gui.screens.NodecraftScreen");
                Screen nodecraftScreen = (Screen) nodecraftScreenClass.getDeclaredConstructor().newInstance();
                MinecraftClient.getInstance().setScreen(nodecraftScreen);
                NodeCraft.LOGGER.info("NodeCraft工具右键点击 - 编辑器界面已打开");
            } catch (ClassNotFoundException e) {
                NodeCraft.LOGGER.error("无法找到 NodecraftScreen 类: {}", e.getMessage());
                showErrorMessage("NodeCraft 编辑器类未找到");
            } catch (NoClassDefFoundError e) {
                NodeCraft.LOGGER.error("NodeCraft 编辑器依赖类缺失: {}", e.getMessage());
                showErrorMessage("NodeCraft 编辑器依赖缺失: " + e.getMessage());
            } catch (Exception e) {
                NodeCraft.LOGGER.error("无法创建 NodeCraft 编辑器界面: {}", e.getMessage(), e);
                showErrorMessage("无法打开 NodeCraft 编辑器: " + e.getMessage());
            }
        }
        
        // 返回成功
        return ActionResult.SUCCESS;
    }
    
    /**
     * 显示错误消息给用户
     */
    private void showErrorMessage(String message) {
        try {
            // 创建一个简单的错误屏幕
            Screen errorScreen = new ErrorScreen(Text.literal("NodeCraft 错误"), Text.literal(message));
            MinecraftClient.getInstance().setScreen(errorScreen);
        } catch (Exception e) {
            NodeCraft.LOGGER.error("无法显示错误屏幕: {}", e.getMessage());
            // 如果连错误屏幕都无法显示，至少在聊天中显示消息
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("NodeCraft 错误: " + message), false);
            }
        }
    }
    
    /**
     * 简单的错误显示屏幕
     */
    private static class ErrorScreen extends Screen {
        private final Text errorMessage;
        
        protected ErrorScreen(Text title, Text errorMessage) {
            super(title);
            this.errorMessage = errorMessage;
        }
        
        @Override
        public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            
            // 显示错误消息
            context.drawCenteredTextWithShadow(
                this.textRenderer, 
                this.title, 
                this.width / 2, 
                this.height / 2 - 20, 
                0xFFFFFF
            );
            
            context.drawCenteredTextWithShadow(
                this.textRenderer, 
                this.errorMessage, 
                this.width / 2, 
                this.height / 2, 
                0xFF5555
            );
            
            context.drawCenteredTextWithShadow(
                this.textRenderer, 
                Text.literal("按 ESC 键返回"), 
                this.width / 2, 
                this.height / 2 + 40, 
                0xAAAAAA
            );
            
            super.render(context, mouseX, mouseY, delta);
        }
        
        @Override
        public boolean shouldPause() {
            return false;
        }
    }
} 