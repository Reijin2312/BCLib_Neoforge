package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import com.mojang.math.Quadrant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;


import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class BaseFenceBlock extends FenceBlock implements RuntimeBlockModelProvider, BlockModelProvider, BlockTagProvider, ItemTagProvider, DropSelfLootProvider<BaseFenceBlock> {
    private final Block parent;

    protected BaseFenceBlock(Block source) {
        super(Properties.ofFullCopy(source).noOcclusion());
        this.parent = source;
    }


    @Override
    public Object getItemModel(Identifier blockId) {
        Identifier parentId = BuiltInRegistries.BLOCK.getKey(parent);
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.ITEM_FENCE, parentId);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    public @Nullable Object getBlockModel(Identifier blockId, BlockState blockState) {
        Identifier parentId = BuiltInRegistries.BLOCK.getKey(parent);
        String path = blockId.getPath();
        Optional<String> pattern = Optional.empty();
        if (path.endsWith("_post")) {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_FENCE_POST, parentId);
        }
        if (path.endsWith("_side")) {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_FENCE_SIDE, parentId);
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
        Identifier postId = RuntimeBlockModelProvider.remapModelIdentifier(stateId, blockState, "_post");
        Identifier sideId = RuntimeBlockModelProvider.remapModelIdentifier(stateId, blockState, "_side");
        registerBlockModel(postId, postId, blockState, modelCache);
        registerBlockModel(sideId, sideId, blockState, modelCache);

        ModelsHelper.MultiPartBuilder builder = ModelsHelper.MultiPartBuilder.create(stateDefinition);
        builder.part(sideId).setCondition(state -> state.getValue(NORTH)).setUVLock(true).add();
        builder.part(sideId)
               .setCondition(state -> state.getValue(EAST))
               .setYRotation(Quadrant.R90)
               .setUVLock(true)
               .add();
        builder.part(sideId)
               .setCondition(state -> state.getValue(SOUTH))
               .setYRotation(Quadrant.R180)
               .setUVLock(true)
               .add();
        builder.part(sideId)
               .setCondition(state -> state.getValue(WEST))
               .setYRotation(Quadrant.R270)
               .setUVLock(true)
               .add();
        builder.part(postId).add();

        return builder.build();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void provideBlockModels(Object modelGenerator) {
        try {
            modelGenerator
                    .getClass()
                    .getMethod("createFence", Block.class, Block.class)
                    .invoke(modelGenerator, parent, this);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to provide models for BaseFenceBlock", ex);
        }
    }

    @Override
    public void registerBlockTags(Identifier location, TagBootstrapContext<Block> context) {
        context.add(this, BlockTags.FENCES);
    }

    @Override
    public void registerItemTags(Identifier location, ItemTagBootstrapContext context) {
        context.add(this, ItemTags.FENCES);
    }

    public static class Wood extends BaseFenceBlock implements BehaviourWood {
        public Wood(Block source, BlockSetType type) {
            super(source);
        }

        @Override
        public void registerBlockTags(Identifier location, TagBootstrapContext<Block> context) {
            context.add(this, BlockTags.FENCES, BlockTags.WOODEN_FENCES);
        }

        @Override
        public void registerItemTags(Identifier location, ItemTagBootstrapContext context) {
            context.add(this, ItemTags.FENCES, ItemTags.WOODEN_FENCES);
        }
    }

    public static BaseFenceBlock from(Block source, BlockSetType type) {
        return new BaseFenceBlock.Wood(source, type);
    }
}
