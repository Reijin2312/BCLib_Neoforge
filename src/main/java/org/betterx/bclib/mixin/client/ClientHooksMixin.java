package org.betterx.bclib.mixin.client;

import org.betterx.bclib.client.render.CustomFogRenderer;
import org.betterx.bclib.util.BackgroundInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.client.ClientHooks;

import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientHooks.class)
public class ClientHooksMixin {
    @Inject(remap = false, method = "getFogColor", at = @At("RETURN"), cancellable = true, require = 0)
    private static void bclib_captureFogColor(
            Camera camera,
            float partialTick,
            ClientLevel world,
            int i,
            float f,
            float red,
            float green,
            float blue,
            CallbackInfoReturnable<Vector4f> cir
    ) {
        Vector4f color = cir.getReturnValue();
        if (color == null) {
            return;
        }

        float fogRed = color.x;
        float fogGreen = color.y;
        float fogBlue = color.z;
        FogType fogType = camera.getFluidInCamera();
        if (fogType != FogType.WATER && world != null && world.dimension().equals(Level.END)) {
            Entity entity = camera.entity();
            boolean skip = false;
            if (entity instanceof LivingEntity livingEntity) {
                MobEffectInstance effect = livingEntity.getEffect(MobEffects.NIGHT_VISION);
                skip = effect != null && effect.getDuration() > 0;
            }
            if (!skip) {
                fogRed *= 4.0F;
                fogGreen *= 4.0F;
                fogBlue *= 4.0F;
                color = new Vector4f(fogRed, fogGreen, fogBlue, color.w);
                cir.setReturnValue(color);
            }
        }

        BackgroundInfo.fogColorRed = fogRed;
        BackgroundInfo.fogColorGreen = fogGreen;
        BackgroundInfo.fogColorBlue = fogBlue;
    }

    @Inject(remap = false, method = "onSetupFog", at = @At("TAIL"), require = 0)
    private static void bclib_customFog(
            FogEnvironment environment,
            FogType type,
            Camera camera,
            float partialTick,
            float renderDistance,
            FogData fogData,
            CallbackInfo ci
    ) {
        // NeoForge passes fog render distance in chunks; custom fog math expects blocks.
        CustomFogRenderer.applyFogDensity(camera, renderDistance * 16.0F, fogData);
    }
}
