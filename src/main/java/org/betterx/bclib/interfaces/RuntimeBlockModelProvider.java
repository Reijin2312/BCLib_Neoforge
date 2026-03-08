package org.betterx.bclib.interfaces;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;


import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public interface RuntimeBlockModelProvider extends ItemModelProvider {
    default @Nullable BlockModel getBlockModel(Identifier resourceLocation, BlockState blockState) {
        Optional<String> pattern = PatternsHelper.createBlockSimple(resourceLocation);
        return ModelsHelper.fromPattern(pattern);
    }
    static Identifier remapModelIdentifier(
            Identifier stateId,
            BlockState blockState
    ) {
        return remapModelIdentifier(stateId, blockState, "");
    }

    static Identifier remapModelIdentifier(
            Identifier stateId,
            BlockState blockState,
            String pathAddOn
    ) {
        return Identifier.fromNamespaceAndPath(
                stateId.getNamespace(),
                "block/" + stateId.getPath() + pathAddOn
        );
    }

    default BlockStateModel.UnbakedRoot getModelVariant(
            Identifier stateId,
            BlockState blockState,
            Map<Identifier, UnbakedModel> modelCache
    ) {
        Identifier modelId = remapModelIdentifier(stateId, blockState);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        return ModelsHelper.createBlockSimple(modelId);
    }

    default void registerBlockModel(
            Identifier stateId,
            Identifier modelId,
            BlockState blockState,
            Map<Identifier, UnbakedModel> modelCache
    ) {
        if (!modelCache.containsKey(modelId)) {
            BlockModel model = getBlockModel(stateId, blockState);
            if (model != null) {
                modelCache.put(modelId, model);
            } else {
                BCLib.LOGGER.warn("Error loading model: {}", modelId);
            }
        }
    }
}
