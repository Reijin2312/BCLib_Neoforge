package org.betterx.bclib.mixin.client;

import org.betterx.bclib.client.textures.AtlasSetManager;

import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(value = net.minecraft.client.renderer.texture.atlas.SpriteSourceList.class)
public class AtlasSetMixin {
    @ModifyVariable(
            remap = false,
            method = "load",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/atlas/SpriteSourceList;<init>(Ljava/util/List;)V"),
            require = 0
    )
    private static List<SpriteSource> bcl_load(
            List<SpriteSource> list,
            ResourceManager resourceManager,
            Identifier type
    ) {
        AtlasSetManager.onLoadResources(type, list);
        return list;
    }
}
