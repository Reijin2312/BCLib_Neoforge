package org.betterx.bclib.registry;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.blockentities.BaseBarrelBlockEntity;
import org.betterx.bclib.blockentities.BaseChestBlockEntity;
import org.betterx.bclib.blockentities.BaseFurnaceBlockEntity;
import org.betterx.bclib.blockentities.DynamicBlockEntityType;
import org.betterx.bclib.blockentities.DynamicBlockEntityType.BlockEntitySupplier;
import org.betterx.bclib.blocks.BaseBarrelBlock;
import org.betterx.bclib.blocks.BaseChestBlock;
import org.betterx.bclib.blocks.BaseFurnaceBlock;
import org.betterx.bclib.blocks.signs.BaseSignBlock;
import org.betterx.bclib.furniture.entity.EntityChair;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.registries.RegisterEvent;

public class BaseBlockEntities {
    private static final ResourceLocation CHEST_ID = BCLib.makeID("chest");
    private static final ResourceLocation BARREL_ID = BCLib.makeID("barrel");
    private static final ResourceLocation FURNACE_ID = BCLib.makeID("furnace");
    private static final ResourceLocation CHAIR_ID = BCLib.makeID("chair");

    public static DynamicBlockEntityType<BaseChestBlockEntity> CHEST;
    public static DynamicBlockEntityType<BaseBarrelBlockEntity> BARREL;
    public static DynamicBlockEntityType<BaseFurnaceBlockEntity> FURNACE;

    public static EntityType<EntityChair> CHAIR;

    private static final java.util.List<Block> PENDING_CHESTS = new java.util.ArrayList<>();
    private static final java.util.List<Block> PENDING_BARRELS = new java.util.ArrayList<>();
    private static final java.util.List<Block> PENDING_FURNACES = new java.util.ArrayList<>();

    public static void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
            event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
                CHEST = new DynamicBlockEntityType<>(BaseChestBlockEntity::new);
                BARREL = new DynamicBlockEntityType<>(BaseBarrelBlockEntity::new);
                FURNACE = new DynamicBlockEntityType<>(BaseFurnaceBlockEntity::new);

                PENDING_CHESTS.forEach(CHEST::registerBlock);
                PENDING_BARRELS.forEach(BARREL::registerBlock);
                PENDING_FURNACES.forEach(FURNACE::registerBlock);

                helper.register(CHEST_ID, CHEST);
                helper.register(BARREL_ID, BARREL);
                helper.register(FURNACE_ID, FURNACE);
            });
        } else if (event.getRegistryKey().equals(Registries.ENTITY_TYPE)) {
            event.register(Registries.ENTITY_TYPE, helper -> {
                CHAIR = EntityType.Builder.of(EntityChair::new, MobCategory.MISC)
                        .sized(0.5F, 0.8F)
                        .fireImmune()
                        .noSummon()
                        .build(CHAIR_ID.toString());
                helper.register(CHAIR_ID, CHAIR);
            });
        }
    }

    public static void register() {
    }

    public static void registerChestBlock(Block block) {
        if (CHEST != null) {
            CHEST.registerBlock(block);
        } else {
            PENDING_CHESTS.add(block);
        }
    }

    public static void registerBarrelBlock(Block block) {
        if (BARREL != null) {
            BARREL.registerBlock(block);
        } else {
            PENDING_BARRELS.add(block);
        }
    }

    public static void registerFurnaceBlock(Block block) {
        if (FURNACE != null) {
            FURNACE.registerBlock(block);
        } else {
            PENDING_FURNACES.add(block);
        }
    }

    public static Block[] getChests() {
        return BuiltInRegistries.BLOCK
                .stream()
                .filter(block -> block instanceof BaseChestBlock)
                .toArray(Block[]::new);
    }

    public static Block[] getBarrels() {
        return BuiltInRegistries.BLOCK
                .stream()
                .filter(block -> block instanceof BaseBarrelBlock)
                .toArray(Block[]::new);
    }

    public static Block[] getSigns() {
        return BuiltInRegistries.BLOCK
                .stream()
                .filter(block -> block instanceof BaseSignBlock)
                .toArray(Block[]::new);
    }

    public static Block[] getFurnaces() {
        return BuiltInRegistries.BLOCK
                .stream()
                .filter(block -> block instanceof BaseFurnaceBlock)
                .toArray(Block[]::new);
    }
}
