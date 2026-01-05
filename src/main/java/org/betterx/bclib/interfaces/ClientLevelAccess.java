package org.betterx.bclib.interfaces;

import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public interface ClientLevelAccess {
    @Nullable
    LevelRendererAccess bcl_getLevelRenderer();
    @Nullable
    Particle bcl_addParticle(
            ParticleOptions particleOptions,
            double x, double y, double z,
            double vx, double vy, double vz
    );
}

