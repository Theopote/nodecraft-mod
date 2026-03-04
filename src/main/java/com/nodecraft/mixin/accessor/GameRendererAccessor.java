package com.nodecraft.mixin.accessor;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * GameRenderer Accessor
 * 用于调用 GameRenderer.getFov() 方法获取包含动态效果的真实 FOV
 * 这是实现精确鼠标射线检测的关键（避免屏幕边缘偏移）
 */
@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Invoker("getFov")
    float nodecraft$invokeGetFov(Camera camera, float tickDelta, boolean changingFov);
}
