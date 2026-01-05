package org.betterx.bclib.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(value = SurfaceRules.Context.class, remap = false)
public interface SurfaceRulesContextAccessor {
    @Accessor(value = "blockX", remap = false)
    int getBlockX();

    @Accessor(value = "blockY", remap = false)
    int getBlockY();

    @Accessor(value = "blockZ", remap = false)
    int getBlockZ();

    @Accessor(value = "surfaceDepth", remap = false)
    int getSurfaceDepth();

    @Accessor(value = "biome", remap = false)
    Supplier<Holder<Biome>> getBiome();

    @Accessor(value = "chunk", remap = false)
    ChunkAccess getChunk();

    @Accessor(value = "noiseChunk", remap = false)
    NoiseChunk getNoiseChunk();

    @Accessor(value = "stoneDepthAbove", remap = false)
    int getStoneDepthAbove();

    @Accessor(value = "stoneDepthBelow", remap = false)
    int getStoneDepthBelow();

    @Accessor(value = "lastUpdateY", remap = false)
    long getLastUpdateY();

    @Accessor(value = "lastUpdateXZ", remap = false)
    long getLastUpdateXZ();

    @Accessor(value = "randomState", remap = false)
    RandomState getRandomState();
}
