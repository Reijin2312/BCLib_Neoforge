package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;


import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class BaseWeightedPlateBlock extends WeightedPressurePlateBlock implements RuntimeBlockModelProvider, DropSelfLootProvider<BaseWeightedPlateBlock> {
    private final Block parent;

    public BaseWeightedPlateBlock(Block source, BlockSetType type) {
        super(
                15,
                type,
                Properties.ofFullCopy(source)
                          .noCollision()
                          .noOcclusion()
                          .requiresCorrectToolForDrops()
                          .strength(0.5F)
        );
        this.parent = source;
    }

    @Override
    public Object getItemModel(Identifier resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    public @Nullable Object getBlockModel(Identifier resourceLocation, BlockState blockState) {
        Identifier parentId = BuiltInRegistries.BLOCK.getKey(parent);
        Optional<String> pattern;
        if (blockState.getValue(POWER) > 0) {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_PLATE_DOWN, parentId);
        } else {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_PLATE_UP, parentId);
        }
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getModelVariant(
            Identifier stateId,
            BlockState blockState,
            Map modelCache
    ) {
        String state = blockState.getValue(POWER) > 0 ? "_down" : "_up";
        Identifier modelId = RuntimeBlockModelProvider.remapModelIdentifier(stateId, blockState, state);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        return ModelsHelper.createBlockSimple(modelId);
    }
}
