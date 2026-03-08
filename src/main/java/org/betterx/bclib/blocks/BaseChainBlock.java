package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;


import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseChainBlock extends ChainBlock implements RuntimeBlockModelProvider, RenderLayerProvider, DropSelfLootProvider<BaseChainBlock> {
    public BaseChainBlock(MapColor color) {
        this(Properties.ofFullCopy(Blocks.IRON_CHAIN).mapColor(color));
    }

    public BaseChainBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }


    @Override
    public BlockModel getItemModel(Identifier blockId) {
        return ModelsHelper.createItemModel(blockId);
    }

    @Override
    public @Nullable BlockModel getBlockModel(Identifier blockId, BlockState blockState) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_CHAIN, blockId);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    public BlockStateModel.UnbakedRoot getModelVariant(
            Identifier stateId,
            BlockState blockState,
            Map<Identifier, UnbakedModel> modelCache
    ) {
        Direction.Axis axis = blockState.getValue(AXIS);
        Identifier modelId = RuntimeBlockModelProvider.remapModelIdentifier(stateId, blockState);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        return ModelsHelper.createRotatedModel(modelId, axis);
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    public static class Metal extends BaseChainBlock implements BehaviourMetal {

        public Metal(MapColor color) {
            super(color);
        }

        public Metal(Properties properties) {
            super(properties);
        }
    }
}
