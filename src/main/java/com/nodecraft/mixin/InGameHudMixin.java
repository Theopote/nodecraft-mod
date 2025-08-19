package com.nodecraft.mixin;

import com.nodecraft.minecraft.client.MinecraftClientController;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InGameHud的Mixin
 * 用于控制十字星的显示
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {
    
    /**
     * 拦截十字星渲染
     * 当NodeCraft模式激活时隐藏十字星
     */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClientController controller = MinecraftClientController.getInstance();
        if (controller.shouldHideCrosshair()) {
            // 取消十字星渲染
            ci.cancel();
        }
    }
} 