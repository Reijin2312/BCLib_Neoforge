package org.betterx.bclib.furniture.renderer;

import org.betterx.bclib.furniture.entity.EntityChair;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderChair extends EntityRenderer<EntityChair> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/stone.png");

    public RenderChair(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityChair entity) {
        return TEXTURE;
    }
}



