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

@Mixin(value = SurfaceRules.Context.class)
public interface SurfaceRulesContextAccessor {
    @Accessor(value = "blockX")
    int getBlockX();

    @Accessor(value = "blockY")
    int getBlockY();

    @Accessor(value = "blockZ")
    int getBlockZ();

    @Accessor(value = "surfaceDepth")
    int getSurfaceDepth();

    @Accessor(value = "biome")
    Supplier<Holder<Biome>> getBiome();

    @Accessor(value = "chunk")
    ChunkAccess getChunk();

    @Accessor(value = "noiseChunk")
    NoiseChunk getNoiseChunk();

    @Accessor(value = "stoneDepthAbove")
    int getStoneDepthAbove();

    @Accessor(value = "stoneDepthBelow")
    int getStoneDepthBelow();

    @Accessor(value = "lastUpdateY")
    long getLastUpdateY();

    @Accessor(value = "lastUpdateXZ")
    long getLastUpdateXZ();

    @Accessor(value = "randomState")
    RandomState getRandomState();
}



