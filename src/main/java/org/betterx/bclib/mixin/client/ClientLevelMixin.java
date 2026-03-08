package org.betterx.bclib.mixin.client;

import org.betterx.bclib.interfaces.ClientLevelAccess;
import org.betterx.bclib.interfaces.LevelRendererAccess;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;


import org.spongepowered.asm.mixin.Mixin;

import org.jetbrains.annotations.Nullable;

@Mixin(value = ClientLevel.class)
public class ClientLevelMixin implements ClientLevelAccess {
    @Override
    @Nullable
    public LevelRendererAccess bcl_getLevelRenderer() {
        return null;
    }

    @Override
    @Nullable
    public Particle bcl_addParticle(
            ParticleOptions particleOptions,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz
    ) {
        return Minecraft.getInstance().particleEngine.createParticle(particleOptions, x, y, z, vx, vy, vz);
    }
}
