package org.betterx.bclib.mixin.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @Inject(method = "getShaderFogShape", at = @At("RETURN"), cancellable = true)
    private static void bclib_defaultFogShape(CallbackInfoReturnable<FogShape> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(FogShape.SPHERE);
        }
    }
}
