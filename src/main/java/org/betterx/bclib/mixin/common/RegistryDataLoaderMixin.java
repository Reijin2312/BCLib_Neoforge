package org.betterx.bclib.mixin.common;

import net.minecraft.resources.RegistryDataLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = RegistryDataLoader.class, priority = 500, remap = false)
public class RegistryDataLoaderMixin {
    @Accessor(value = "WORLDGEN_REGISTRIES", remap = false)
    @Mutable
    static void wt_set_WORLDGEN_REGISTRIES(List<RegistryDataLoader.RegistryData<?>> list) {
        //SHADOWED
    }

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void bcl_init(CallbackInfo ci) {
        //we need this to ensure, that the BCL-Biome Registry is loaded at the correct time
        //We use WoVer for biom handling now...
//        List<RegistryDataLoader.RegistryData<?>> enhanced = new ArrayList(RegistryDataLoader.WORLDGEN_REGISTRIES.size() + 1);
//        enhanced.add(new RegistryDataLoader.RegistryData<>(
//                BCLBiomeRegistry.BCL_BIOMES_REGISTRY, BiomeData.CODEC
//        ));
//        enhanced.addAll(RegistryDataLoader.WORLDGEN_REGISTRIES);
//        wt_set_WORLDGEN_REGISTRIES(enhanced);
    }

}
