package org.betterx.bclib.client.textures;

import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class AtlasSetManager {
    public static final ResourceLocation VANILLA_BLOCKS = ResourceLocation.withDefaultNamespace("blocks");
    private static Map<ResourceLocation, List<SpriteSource>> additionalSets = new HashMap<>();

    public static void addSource(ResourceLocation type, SpriteSource source) {
        additionalSets.computeIfAbsent(type, (t) -> new LinkedList<>()).add(source);
    }

    public static void onLoadResources(ResourceLocation type, List<SpriteSource> sources) {
        List<SpriteSource> additionalSources = additionalSets.get(type);
        if (additionalSources != null) {
            sources.addAll(additionalSources);
        }
    }
}



