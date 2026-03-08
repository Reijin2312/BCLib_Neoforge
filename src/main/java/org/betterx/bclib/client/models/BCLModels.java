package org.betterx.bclib.client.models;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.furniture.block.BaseChair;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import static org.betterx.bclib.furniture.block.AbstractChair.FACING;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class BCLModels {
    public static final TextureSlot CLOTH = TextureSlot.create("cloth");
    public static final TextureSlot TEXTURE1 = TextureSlot.create("texture1");
    public static final TextureSlot GLOW = TextureSlot.create("glow");
    public static final TextureSlot METAL = TextureSlot.create("metal");
    public static final TextureSlot GLASS = TextureSlot.create("glass");
    public static final TextureSlot PANEL = TextureSlot.create("panel");

    public static final Identifier BAR_STOOL_MODEL_LOCATION = BCLib.C.mk("block/bar_stool");
    public static final ModelTemplate BAR_STOOL = new ModelTemplate(
            Optional.of(BAR_STOOL_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TEXTURE, CLOTH
    );

    public static final Identifier CHAIR_MODEL_LOCATION = BCLib.C.mk("block/chair");
    public static final ModelTemplate CHAIR = new ModelTemplate(
            Optional.of(CHAIR_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TEXTURE
    );

    public static final ModelTemplate CHAIR_TOP = new ModelTemplate(
            Optional.empty(),
            Optional.of("_top"),
            TextureSlot.PARTICLE
    );

    public static final Identifier TABURET_MODEL_LOCATION = BCLib.C.mk("block/taburet");
    public static final ModelTemplate TABURET = new ModelTemplate(
            Optional.of(TABURET_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TEXTURE
    );

    public static final Identifier CHEST_MODEL_LOCATION = BCLib.C.mk("block/chest_item");
    public static final ModelTemplate CHEST_ITEM = new ModelTemplate(
            Optional.of(CHEST_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TEXTURE,
            TextureSlot.PARTICLE
    );

    public static final Identifier PATH_MODEL_LOCATION = BCLib.C.mk("block/path");
    public static final ModelTemplate PATH = new ModelTemplate(
            Optional.of(PATH_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE
    );

    public static final Identifier LADDER_MODEL_LOCATION = BCLib.C.mk("block/ladder");
    public static final ModelTemplate LADDER = new ModelTemplate(
            Optional.of(LADDER_MODEL_LOCATION),
            Optional.empty(),
            TextureSlot.TEXTURE
    );

    public static final ModelTemplate BULB_LANTERN_FLOOR = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/bulb_lantern_floor")),
            Optional.empty(),
            GLOW, METAL
    );

    public static final ModelTemplate BULB_LANTERN_CEIL = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/bulb_lantern_ceil")),
            Optional.empty(),
            GLOW, METAL
    );

    public static final ModelTemplate STONE_LANTERN_FLOOR = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/stone_lantern_floor")),
            Optional.empty(),
            TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE, GLASS
    );

    public static final ModelTemplate STONE_LANTERN_CEIL = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/stone_lantern_ceil")),
            Optional.empty(),
            TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE, GLASS
    );

    public static final ModelTemplate CROSS_SHADED = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/cross_shaded")),
            Optional.empty(),
            TextureSlot.CROSS
    );

    public static final ModelTemplate ANVIL = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/anvil")),
            Optional.empty(),
            TextureSlot.FRONT, TextureSlot.BACK, TextureSlot.TOP, TextureSlot.BOTTOM, PANEL
    );

    public static final ModelTemplate TRAPDOOR = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/sided_trapdoor")),
            Optional.empty(),
            TextureSlot.TEXTURE, TextureSlot.SIDE
    );

    public static final ModelTemplate FURNACE_GLOW = new ModelTemplate(
            Optional.of(BCLib.C.mk("block/furnace_glow")),
            Optional.empty(),
            TextureSlot.TOP, TextureSlot.SIDE, TextureSlot.FRONT, GLOW
    );

    public static void createBarStoolBlockModel(
            WoverBlockModelGenerators generators,
            Block block,
            Block woodType,
            Block clothType
    ) {
        TextureMapping mapping = WoverBlockModelGenerators.textureMappingOf(
                TextureSlot.TEXTURE,
                TextureMapping.getBlockTexture(woodType),
                CLOTH,
                TextureMapping.getBlockTexture(clothType)
        );
        Identifier modelLocation = BAR_STOOL.create(block, mapping, generators.vanillaGenerator.modelOutput());

        var blockStateGenerator = MultiVariantGenerator
                .dispatch(block, BlockModelGenerators.plainVariant(modelLocation))
                .with(getChairFacingPropertyDispatch(modelLocation));
        generators.acceptBlockState(blockStateGenerator);
        generators.delegateItemModel(block, modelLocation);
    }

    public static void createTaburetBlockModel(
            WoverBlockModelGenerators generators,
            Block block,
            Block woodType
    ) {
        TextureMapping mapping = WoverBlockModelGenerators.textureMappingOf(
                TextureSlot.TEXTURE,
                TextureMapping.getBlockTexture(woodType)
        );
        Identifier modelLocation = TABURET.create(block, mapping, generators.vanillaGenerator.modelOutput());

        var blockStateGenerator = MultiVariantGenerator
                .dispatch(block, BlockModelGenerators.plainVariant(modelLocation))
                .with(getChairFacingPropertyDispatch(modelLocation));
        generators.acceptBlockState(blockStateGenerator);
        generators.delegateItemModel(block, modelLocation);
    }

    public static void createChairBlockModel(
            WoverBlockModelGenerators generators,
            Block block,
            Block woodType,
            Block cloth
    ) {
        TextureMapping mapping = WoverBlockModelGenerators.textureMappingOf(
                TextureSlot.TEXTURE,
                TextureMapping.getBlockTexture(woodType),
                TextureSlot.PARTICLE,
                TextureMapping.getBlockTexture(woodType)
        );
        Identifier modelLocation = CHAIR.create(block, mapping, generators.vanillaGenerator.modelOutput());
        Identifier topLocation = generators.particleOnlyModel(woodType);//CHAIR_TOP.create(block, mapping, generators.vanillaGenerator.modelOutput);


        var blockStateGenerator = MultiVariantGenerator
                .dispatch(block)
                .with(
                        PropertyDispatch
                                .initial(FACING, BaseChair.TOP)
                                .select(
                                        Direction.EAST,
                                        false,
                                        BlockModelGenerators
                                                .plainVariant(modelLocation)
                                                .with(VariantMutator.Y_ROT.withValue(Quadrant.R90))
                                )
                                .select(
                                        Direction.SOUTH,
                                        false,
                                        BlockModelGenerators
                                                .plainVariant(modelLocation)
                                                .with(VariantMutator.Y_ROT.withValue(Quadrant.R180))
                                )
                                .select(
                                        Direction.WEST,
                                        false,
                                        BlockModelGenerators
                                                .plainVariant(modelLocation)
                                                .with(VariantMutator.Y_ROT.withValue(Quadrant.R270))
                                )
                                .select(
                                        Direction.NORTH,
                                        false,
                                        BlockModelGenerators.plainVariant(modelLocation)
                                )
                                .select(
                                        Direction.NORTH,
                                        true,
                                        BlockModelGenerators.plainVariant(topLocation)
                                )
                                .select(
                                        Direction.EAST,
                                        true,
                                        BlockModelGenerators.plainVariant(topLocation)
                                )
                                .select(
                                        Direction.SOUTH,
                                        true,
                                        BlockModelGenerators.plainVariant(topLocation)
                                )
                                .select(
                                        Direction.WEST,
                                        true,
                                        BlockModelGenerators.plainVariant(topLocation)
                                )
                );
        generators.acceptBlockState(blockStateGenerator);
        generators.delegateItemModel(block, modelLocation);
    }

    private static PropertyDispatch.@NotNull C1<VariantMutator, Direction> getChairFacingPropertyDispatch(Identifier modelLocation) {
        return PropertyDispatch
                .modify(FACING)
                .select(
                        Direction.NORTH,
                        VariantMutator
                                .MODEL
                                .withValue(modelLocation)
                                .then(VariantMutator.Y_ROT.withValue(Quadrant.R270))
                )
                .select(
                        Direction.EAST,
                        VariantMutator.MODEL.withValue(modelLocation)
                )
                .select(
                        Direction.SOUTH,
                        VariantMutator
                                .MODEL
                                .withValue(modelLocation)
                                .then(VariantMutator.Y_ROT.withValue(Quadrant.R90))
                )
                .select(
                        Direction.WEST,
                        VariantMutator
                                .MODEL
                                .withValue(modelLocation)
                                .then(VariantMutator.Y_ROT.withValue(Quadrant.R180))
                );
    }

}
