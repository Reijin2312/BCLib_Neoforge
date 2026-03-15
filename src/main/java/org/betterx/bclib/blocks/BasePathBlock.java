package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


import org.jetbrains.annotations.NotNull;

public abstract class BasePathBlock extends BaseBlockNotFull implements BlockLootProvider, BlockModelProvider {
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 15, 16);

    private Block baseBlock;

    public BasePathBlock(Block source) {
        super(Properties.ofFullCopy(source).isValidSpawn((state, world, pos, type) -> false));
        this.baseBlock = source;
        if (source instanceof BaseTerrainBlock terrain) {
            this.baseBlock = terrain.getBaseBlock();
            terrain.setPathBlock(this);
        }
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter view,
            BlockPos pos,
            CollisionContext ePos
    ) {
        return SHAPE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void provideBlockModels(Object modelGenerator) {
        try {
            Class<?> bridge = Class.forName("org.betterx.bclib.client.models.BlockDatagenBridge");
            bridge.getMethod("provideBasePathBlockModels", Object.class, BasePathBlock.class, Block.class)
                  .invoke(null, modelGenerator, this, this.baseBlock);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to provide models for BasePathBlock", ex);
        }
    }

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull Identifier location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.dropWithSilkTouch(this, this.baseBlock, ConstantValue.exactly(1));
    }

    public static class Stone extends BasePathBlock implements BehaviourStone {
        public Stone(Block source) {
            super(source);
        }
    }
}
