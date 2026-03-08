package org.betterx.bclib.client.render;

import org.betterx.bclib.items.boat.BoatTypeOverride;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;


import org.joml.Quaternionf;
import org.jetbrains.annotations.Nullable;

public class BoatRenderer {
    private static @Nullable Model.Simple waterPatchModel;

    public static void initialize(EntityRendererProvider.Context context) {
        if (waterPatchModel == null) {
            waterPatchModel = new Model.Simple(context.bakeLayer(ModelLayers.BOAT_WATER_PATCH), p -> RenderTypes.waterMask());
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean submitCustom(
            BoatRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector
    ) {
        if (!(state instanceof CustomBoatRenderState ext)) {
            return false;
        }

        BoatTypeOverride type = ext.bclib_getCustomType();
        if (type == null) {
            return false;
        }

        boolean hasChest = ext.bclib_isChest();
        Object modelObj = type.getBoatModel(hasChest);
        if (!(modelObj instanceof EntityModel<?> rawModel)) {
            return false;
        }

        EntityModel<BoatRenderState> model = (EntityModel<BoatRenderState>) rawModel;
        Identifier texture = hasChest ? type.chestBoatTexture : type.boatTexture;
        if (texture == null) {
            return false;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));

        float hurtTime = state.hurtTime;
        if (hurtTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * state.damageTime / 10.0F * state.hurtDir));
        }

        if (!state.isUnderWater && !Mth.equal(state.bubbleAngle, 0.0F)) {
            poseStack.mulPose(new Quaternionf().setAngleAxis(state.bubbleAngle * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        submitNodeCollector.submitModel(
                model,
                state,
                poseStack,
                model.renderType(texture),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null
        );

        if (!state.isUnderWater && waterPatchModel != null) {
            submitNodeCollector.submitModel(
                    waterPatchModel,
                    Unit.INSTANCE,
                    poseStack,
                    waterPatchModel.renderType(texture),
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    state.outlineColor,
                    null
            );
        }

        poseStack.popPose();
        return true;
    }
}
