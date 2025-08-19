package com.nodecraft.mixin;

import com.nodecraft.core.NodeCraft;
import com.nodecraft.gui.screens.NodecraftScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NodecraftScreen的Mixin
 * 用于使NodeCraft编辑器界面不暂停游戏
 */
@Mixin(NodecraftScreen.class)
public class NodecraftScreenMixin {
    
    /**
     * 覆盖shouldPause方法，使NodecraftScreen不暂停游戏
     */
    @Inject(method = "shouldPause", at = @At("HEAD"), cancellable = true)
    private void onShouldPause(CallbackInfoReturnable<Boolean> cir) {
        NodeCraft.LOGGER.debug("NodecraftScreen不暂停游戏");
        cir.setReturnValue(false);
    }
} 