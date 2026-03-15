package org.betterx.bclib.mixin.client;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.render.CustomFogRenderer;
import org.betterx.bclib.util.BackgroundInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.util.Mth;
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
    private static final float LEGACY_END_FOG_COLOR_SCALE = 0.6F;

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

        if (BCLib.RUNS_DISTANT_HORIZONS) {
            float fogRed = Mth.clamp(color.x, 0.0F, 1.0F);
            float fogGreen = Mth.clamp(color.y, 0.0F, 1.0F);
            float fogBlue = Mth.clamp(color.z, 0.0F, 1.0F);

            BackgroundInfo.fogColorRed = fogRed;
            BackgroundInfo.fogColorGreen = fogGreen;
            BackgroundInfo.fogColorBlue = fogBlue;
            return;
        }

        if (world.dimension().equals(Level.END) && camera.getFluidInCamera() != FogType.WATER) {
            Entity entity = camera.entity();
            boolean hasNightVision = false;
            if (entity instanceof LivingEntity livingEntity) {
                MobEffectInstance effect = livingEntity.getEffect(MobEffects.NIGHT_VISION);
                hasNightVision = effect != null && effect.getDuration() > 0;
            }

            if (!hasNightVision) {
                color.set(
                        color.x * LEGACY_END_FOG_COLOR_SCALE,
                        color.y * LEGACY_END_FOG_COLOR_SCALE,
                        color.z * LEGACY_END_FOG_COLOR_SCALE,
                        color.w
                );
                cir.setReturnValue(color);
            }
        }

        float fogRed = Mth.clamp(color.x, 0.0F, 1.0F);
        float fogGreen = Mth.clamp(color.y, 0.0F, 1.0F);
        float fogBlue = Mth.clamp(color.z, 0.0F, 1.0F);

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
        if (BCLib.RUNS_DISTANT_HORIZONS) {
            return;
        }

        // NeoForge passes fog render distance in chunks; custom fog math expects blocks.
        CustomFogRenderer.applyFogDensity(camera, renderDistance * 16.0F, fogData);
    }
}
