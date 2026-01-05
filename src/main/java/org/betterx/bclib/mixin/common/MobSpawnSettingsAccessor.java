package org.betterx.bclib.mixin.common;

import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = MobSpawnSettings.class, remap = false)
public interface MobSpawnSettingsAccessor {
    @Accessor(value = "spawners", remap = false)
    Map<MobCategory, WeightedRandomList<SpawnerData>> bcl_getSpawners();

    @Accessor(value = "spawners", remap = false)
    @Mutable
    void bcl_setSpawners(Map<MobCategory, WeightedRandomList<SpawnerData>> spawners);
}
