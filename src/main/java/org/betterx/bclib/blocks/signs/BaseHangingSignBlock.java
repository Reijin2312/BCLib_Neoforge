package org.betterx.bclib.blocks.signs;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.complexmaterials.BCLWoodTypeWrapper;
import org.betterx.wover.block.api.BlockRegistry;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.CustomBlockItemProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;


import java.util.function.Supplier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class BaseHangingSignBlock extends CeilingHangingSignBlock implements BlockModelProvider, CustomBlockItemProvider, BlockTagProvider, ItemTagProvider {
    protected final Supplier<BaseWallHangingSignBlock> wallSign;
    private BlockItem customItem;
    private BaseWallHangingSignBlock wallSignBlock;
    private final Block parent;

    @FunctionalInterface
    public interface WallSignProvider {
        BaseWallHangingSignBlock create(Properties properties, WoodType woodType);
    }

    protected BaseHangingSignBlock(
            Block parent,
            WoodType type,
            MapColor color,
            boolean flammable,
            WallSignProvider provider
    ) {
        super(type, BehaviourBuilders.createSign(color, flammable));
        this.parent = parent;
        this.wallSign = () -> provider.create(BehaviourBuilders.createWallSign(color, this, flammable), type);
    }

    public BaseWallHangingSignBlock getWallSignBlock() {
        if (wallSignBlock == null) {
            wallSignBlock = wallSign.get();
        }
        return wallSignBlock;
    }

    @Override
    public float getYRotationDegrees(BlockState blockState) {
        return RotationSegment.convertToDegrees(blockState.getValue(StandingSignBlock.ROTATION));
    }

    @Override
    public BlockItem getCustomBlockItem(Identifier blockID, Item.Properties settings) {
        if (customItem == null) {
            customItem = new HangingSignItem(
                    this,
                    BlockRegistry.withConstructionId(getWallSignId(blockID), this::getWallSignBlock),
                    settings.stacksTo(16)
            );
        }
        return customItem;
    }

    private static Identifier getWallSignId(Identifier blockID) {
        final String path = blockID.getPath();
        final String wallPath = path.endsWith("hanging_sign")
                ? path.substring(0, path.length() - "hanging_sign".length()) + "wall_hanging_sign"
                : path + "_wall_hanging_sign";
        return Identifier.fromNamespaceAndPath(blockID.getNamespace(), wallPath);
    }

    @Override
    public void registerBlockTags(Identifier location, TagBootstrapContext<Block> context) {
        context.add(this, BlockTags.CEILING_HANGING_SIGNS);
    }

    @Override
    public void registerItemTags(Identifier location, ItemTagBootstrapContext context) {
        context.add(this, ItemTags.HANGING_SIGNS);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void provideBlockModels(Object modelGenerator) {
    WoverBlockModelGenerators generator = (WoverBlockModelGenerators) modelGenerator;
        generator.createHangingSign(parent, this, getWallSignBlock());
    }

    public static class Wood extends BaseHangingSignBlock implements BehaviourWood {
        public Wood(Block parent, WoodType type) {
            this(parent, type, MapColor.WOOD, true);
        }

        public Wood(Block parent, BCLWoodTypeWrapper type) {
            this(parent, type.type, type.color, type.flammable);
        }

        public Wood(Block parent, WoodType type, MapColor color, boolean flammable) {
            super(parent, type, color, flammable, BaseWallHangingSignBlock.Wood::new);
        }
    }

    public static class Stone extends BaseHangingSignBlock implements BehaviourStone {
        public Stone(Block parent, WoodType type) {
            this(parent, type, MapColor.WOOD, true);
        }

        public Stone(Block parent, BCLWoodTypeWrapper type) {
            this(parent, type.type, type.color, type.flammable);
        }

        public Stone(Block parent, WoodType type, MapColor color, boolean flammable) {
            super(parent, type, color, flammable, BaseWallHangingSignBlock.Stone::new);
        }
    }

    public static class Metal extends BaseHangingSignBlock implements BehaviourMetal {
        public Metal(Block parent, WoodType type) {
            this(parent, type, MapColor.WOOD, true);
        }

        public Metal(Block parent, BCLWoodTypeWrapper type) {
            this(parent, type.type, type.color, type.flammable);
        }

        public Metal(Block parent, WoodType type, MapColor color, boolean flammable) {
            super(parent, type, color, flammable, BaseWallHangingSignBlock.Stone::new);
        }
    }

    public static BaseHangingSignBlock from(Block parent, WoodType type) {
        return new BaseHangingSignBlock.Wood(parent, type);
    }
}
