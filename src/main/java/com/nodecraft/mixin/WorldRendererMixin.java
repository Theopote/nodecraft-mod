package com.nodecraft.mixin;

import com.nodecraft.gui.screens.NodecraftScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WorldRendererMixin
 * 当 NodecraftScreen 打开时，禁用 Minecraft 原版的方块高亮边框渲染
 * 由自定义的 BlockHighlightRenderer 替代
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    /**
     * 拦截原版方块边框渲染（renderTargetBlockOutline）
     * 当 NodecraftScreen 打开时完全取消原版蓝色边框
     */
    @Inject(method = "renderTargetBlockOutline", at = @At("HEAD"), cancellable = true)
    private void onRenderTargetBlockOutline(VertexConsumerProvider.Immediate vertexConsumers,
                                           MatrixStack matrices,
                                           boolean translucent,
                                           WorldRenderState renderState,
                                           CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof NodecraftScreen) {
            ci.cancel();
        }
    }
}
