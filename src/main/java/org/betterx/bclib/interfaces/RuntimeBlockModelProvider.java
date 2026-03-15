package org.betterx.bclib.interfaces;

import org.betterx.bclib.BCLib;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


import java.util.Map;
import org.jetbrains.annotations.Nullable;

public interface RuntimeBlockModelProvider extends ItemModelProvider {
    @OnlyIn(Dist.CLIENT)
    default @Nullable Object getBlockModel(Identifier resourceLocation, BlockState blockState) {
        try {
            Class<?> patternsHelper = Class.forName("org.betterx.bclib.client.models.PatternsHelper");
            Class<?> modelsHelper = Class.forName("org.betterx.bclib.client.models.ModelsHelper");
            Object pattern = patternsHelper
                    .getMethod("createBlockSimple", Identifier.class)
                    .invoke(null, resourceLocation);
            return modelsHelper
                    .getMethod("fromPattern", java.util.Optional.class)
                    .invoke(null, pattern);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to create block model for " + resourceLocation, ex);
        }
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

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("rawtypes")
    default Object getModelVariant(
            Identifier stateId,
            BlockState blockState,
            Map modelCache
    ) {
        Identifier modelId = remapModelIdentifier(stateId, blockState);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        try {
            Class<?> modelsHelper = Class.forName("org.betterx.bclib.client.models.ModelsHelper");
            return modelsHelper
                    .getMethod("createBlockSimple", Identifier.class)
                    .invoke(null, modelId);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to create block variant for " + modelId, ex);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings({"rawtypes", "unchecked"})
    default void registerBlockModel(
            Identifier stateId,
            Identifier modelId,
            BlockState blockState,
            Map modelCache
    ) {
        if (!modelCache.containsKey(modelId)) {
            Object model = getBlockModel(stateId, blockState);
            if (model != null) {
                modelCache.put(modelId, model);
            } else {
                BCLib.LOGGER.warn("Error loading model: {}", modelId);
            }
        }
    }
}
