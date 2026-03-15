package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourLeaves;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public abstract class BaseLeavesBlock extends LeavesBlock implements RuntimeBlockModelProvider, RenderLayerProvider, BehaviourLeaves, BlockLootProvider {
    private static final float DEFAULT_LEAF_PARTICLE_CHANCE = 0.01F;
    protected final Block sapling;

    public BaseLeavesBlock(
            Block sapling,
            BlockBehaviour.Properties properties
    ) {
        super(DEFAULT_LEAF_PARTICLE_CHANCE, properties);
        this.sapling = sapling;
    }

    @Deprecated(forRemoval = true)
    public BaseLeavesBlock(
            Block sapling,
            MapColor color,
            Consumer<BlockBehaviour.Properties> customizeProperties
    ) {
        super(DEFAULT_LEAF_PARTICLE_CHANCE, BaseBlock.acceptAndReturn(customizeProperties, BehaviourBuilders.createLeaves(color, true)));
        this.sapling = sapling;
    }

    @Deprecated(forRemoval = true)
    public BaseLeavesBlock(
            Block sapling,
            MapColor color,
            int light,
            Consumer<BlockBehaviour.Properties> customizeProperties
    ) {
        super(
                DEFAULT_LEAF_PARTICLE_CHANCE,
                BaseBlock.acceptAndReturn(
                        customizeProperties,
                        BehaviourBuilders.createLeaves(color, true).lightLevel(state -> light)
                )
        );
        this.sapling = sapling;
    }

    @Deprecated(forRemoval = true)
    public BaseLeavesBlock(Block sapling, MapColor color) {
        super(DEFAULT_LEAF_PARTICLE_CHANCE, BehaviourBuilders.createLeaves(color, true));
        this.sapling = sapling;
    }

    @Deprecated(forRemoval = true)
    public BaseLeavesBlock(Block sapling, MapColor color, int light) {
        super(DEFAULT_LEAF_PARTICLE_CHANCE, BehaviourBuilders.createLeaves(color, true).lightLevel(state -> light));
        this.sapling = sapling;
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Override
    public Object getItemModel(Identifier resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    public float compostingChance() {
        return 0.3f;
    }

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull Identifier location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.dropLeaves(this, this.sapling);
    }
}
