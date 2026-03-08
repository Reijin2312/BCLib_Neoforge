package org.betterx.bclib.furniture.renderer;

import org.betterx.bclib.furniture.entity.EntityChair;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;


public class RenderChair extends EntityRenderer<EntityChair, EntityRenderState> {
    public RenderChair(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
