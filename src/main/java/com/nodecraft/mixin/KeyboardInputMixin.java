package com.nodecraft.mixin;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.gui.screens.NodecraftScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.util.InputUtil;

import java.nio.DoubleBuffer;
import org.lwjgl.BufferUtils;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onMovementTick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.currentScreen instanceof NodecraftScreen screen) {
            // 使用GLFW直接获取鼠标位置
            DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
            GLFW.glfwGetCursorPos(client.getWindow().getHandle(), xBuffer, yBuffer);
            double mouseX = xBuffer.get(0);
            double mouseY = yBuffer.get(0);
            
            // 检查鼠标是否在nodecraft窗口范围内
            boolean mouseInWindow = screen.isMouseOverNodecraftGui(mouseX, mouseY);
            
            if (mouseInWindow) {
                // 鼠标在nodecraft窗口内：禁用所有移动输入
                this.movementForward = 0.0F;
                this.movementSideways = 0.0F;
                this.playerInput = new PlayerInput(false, false, false, false, false, false, false);
                
                ci.cancel(); // 阻止原版处理
            } else {
                // 鼠标在nodecraft窗口外：允许正常的键盘移动输入
                long window = client.getWindow().getHandle();

                // 手动处理键盘输入以确保正常工作
                this.movementForward = 0.0F;
                this.movementSideways = 0.0F;

                // 检查按键并更新移动
                if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_W)) this.movementForward += 1.0F;
                if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_S)) this.movementForward -= 1.0F;
                if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_D)) this.movementSideways -= 1.0F;
                if (InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_A)) this.movementSideways += 1.0F;

                // 更新玩家输入状态
                boolean forward = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_W);
                boolean backward = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_S);
                boolean left = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_A);
                boolean right = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_D);
                boolean jump = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_SPACE);
                boolean sneak = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT);
                boolean sprint = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL);

                // 更新 PlayerInput
                this.playerInput = new PlayerInput(forward, backward, left, right, jump, sneak, sprint);

                // 处理慢速移动
                if (sneak) {
                    this.movementSideways *= 0.3F;
                    this.movementForward *= 0.3F;
                }

                ci.cancel(); // 阻止原版处理，使用我们的自定义处理
            }
        }
        // 如果不在NodecraftScreen中，让原版处理
    }
} 