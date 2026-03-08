package org.betterx.bclib.interfaces;


import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;


import org.jetbrains.annotations.Nullable;

public interface LevelRendererAccess {
    @Nullable Particle bcl_addParticle(
            ParticleOptions particleOptions,
            double x, double y, double z,
            double vx, double vy, double vz
    );
}
