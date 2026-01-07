package org.betterx.bclib.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = LevelRenderer.class)
@OnlyIn(Dist.CLIENT)
public interface LevelRendererAccessor {
    @Invoker(value = "renderShape")
    public static void bclib_renderShape(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            VoxelShape voxelShape,
            double d,
            double e,
            double f,
            float g,
            float h,
            float i,
            float j
    ) {
        throw new AssertionError();
    }
}




