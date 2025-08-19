package com.nodecraft.mixin;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.gui.screens.NodecraftScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 鼠标光标Mixin
 * 确保在NodecraftScreen打开时鼠标始终保持自由状态，可以在界面和游戏之间自由移动
 */
@Mixin(Mouse.class)
public class MouseCursorMixin {
    
    @Shadow @Final private MinecraftClient client;
    @Shadow private boolean cursorLocked;
    
    /**
     * 当NodecraftScreen打开时，完全阻止锁定光标
     * 确保鼠标始终处于自由状态，能够与UI交互和在窗口外控制游戏
     */
    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    private void onLockCursor(CallbackInfo ci) {
        if (client.currentScreen instanceof NodecraftScreen) {
            // 当NodecraftScreen打开时，完全阻止锁定光标
            // 确保鼠标始终处于自由状态，能够与UI交互
            if (this.cursorLocked) {
                this.cursorLocked = false;
                // 将鼠标设置为正常模式（可见光标）
                GLFW.glfwSetInputMode(client.getWindow().getHandle(), 
                    GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
                NodeCraft.LOGGER.debug("NodecraftScreen打开，强制解锁光标");
            }
            
            // 取消原版锁定光标的操作
            ci.cancel();
        }
    }
    
    /**
     * 当NodecraftScreen打开时，即使尝试解锁光标也确保光标处于正常状态
     */
    @Inject(method = "unlockCursor", at = @At("HEAD"), cancellable = true)
    private void onUnlockCursor(CallbackInfo ci) {
        if (client.currentScreen instanceof NodecraftScreen) {
            // 确保使用正常模式（可见光标）
            GLFW.glfwSetInputMode(client.getWindow().getHandle(), 
                GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            this.cursorLocked = false;
            NodeCraft.LOGGER.debug("NodecraftScreen打开，保持光标解锁状态");
            
            // 取消原版解锁光标的操作（因为我们已经设置了GLFW状态）
            ci.cancel();
        }
    }
    
    /**
     * 监控鼠标输入模式的更改，确保在NodecraftScreen打开时不会被更改为锁定模式
     */
    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouse(CallbackInfo ci) {
        if (client.currentScreen instanceof NodecraftScreen) {
            // 获取当前GLFW光标模式
            int currentMode = GLFW.glfwGetInputMode(client.getWindow().getHandle(), GLFW.GLFW_CURSOR);
            
            // 如果不是正常模式，强制设回正常模式
            if (currentMode != GLFW.GLFW_CURSOR_NORMAL) {
                GLFW.glfwSetInputMode(client.getWindow().getHandle(), 
                    GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
                this.cursorLocked = false;
                NodeCraft.LOGGER.debug("检测到NodecraftScreen打开时光标模式被改变，强制恢复为正常模式");
            }
        }
    }
} 