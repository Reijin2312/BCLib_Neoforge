package org.betterx.bclib.mixin.client.boat;

import org.betterx.bclib.items.boat.BoatTypeOverride;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BoatRenderer.class)
public abstract class BoatRendererMixin {
    @Inject(remap = false, method = "<init>", at = @At("TAIL"), require = 0)
    private void bcl_init(EntityRendererProvider.Context context, ModelLayerLocation layer, CallbackInfo ci) {
        // Keep custom boat model layers baked and ready even while the custom submit/render hook is still disabled.
        org.betterx.bclib.client.render.BoatRenderer.initialize(context);
        BoatTypeOverride.values().forEach(type -> type.createBoatModels(context));
    }
}
