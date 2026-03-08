package org.betterx.bclib.mixin.client.boat;

import org.betterx.bclib.client.render.BoatRenderer;
import org.betterx.bclib.client.render.CustomBoatRenderState;
import org.betterx.bclib.items.boat.CustomBoatTypeOverride;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractBoatRenderer.class)
public abstract class AbstractBoatRendererMixin extends EntityRenderer<AbstractBoat, BoatRenderState> {
    protected AbstractBoatRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(remap = false, method = "extractRenderState", at = @At("TAIL"), require = 0)
    private void bclib_extractCustomBoatType(AbstractBoat boat, BoatRenderState state, float partialTick, CallbackInfo ci) {
        if (state instanceof CustomBoatRenderState ext) {
            if (boat instanceof CustomBoatTypeOverride customBoat) {
                ext.bclib_setCustomType(customBoat.bcl_getCustomType());
            } else {
                ext.bclib_setCustomType(null);
            }
            ext.bclib_setChest(boat instanceof AbstractChestBoat);
        }
    }

    @Inject(remap = false, method = "submit", at = @At("HEAD"), cancellable = true, require = 0)
    private void bclib_renderCustomBoat(
            BoatRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState,
            CallbackInfo ci
    ) {
        if (BoatRenderer.submitCustom(state, poseStack, submitNodeCollector)) {
            super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
            ci.cancel();
        }
    }
}
