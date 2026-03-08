package org.betterx.bclib.registry;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.render.BaseChestBlockEntityRenderer;
import org.betterx.bclib.furniture.renderer.RenderChair;
import org.betterx.bclib.items.boat.BoatTypeOverride;

import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.object.boat.RaftModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = BCLib.MOD_ID, value = Dist.CLIENT)
public class BaseBlockEntityRenders {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Boat renderer override still rides through mixins; chest/chair can use direct registrations.
        if (BaseBlockEntities.CHEST != null) {
            event.registerBlockEntityRenderer(BaseBlockEntities.CHEST, BaseChestBlockEntityRenderer::new);
        }
        if (BaseBlockEntities.CHAIR != null) {
            event.registerEntityRenderer(BaseBlockEntities.CHAIR, RenderChair::new);
        }
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        LayerDefinition boatModel = BoatModel.createBoatModel();
        LayerDefinition chestBoatModel = BoatModel.createChestBoatModel();
        LayerDefinition raftModel = RaftModel.createRaftModel();
        LayerDefinition chestRaftModel = RaftModel.createChestRaftModel();

        BoatTypeOverride.values().forEach(type -> {
            event.registerLayerDefinition(type.boatModelName, () -> type.isRaft ? raftModel : boatModel);
            event.registerLayerDefinition(type.chestBoatModelName, () -> type.isRaft ? chestRaftModel : chestBoatModel);
        });
    }
}
