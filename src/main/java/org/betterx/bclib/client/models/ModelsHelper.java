package org.betterx.bclib.client.models;

import com.mojang.math.Quadrant;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.MultiPartModel;
import net.minecraft.client.resources.model.WeightedVariants;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;


import com.google.common.collect.Lists;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ModelsHelper {
    public static BlockModel fromPattern(Optional<String> pattern) {
        return pattern.map(json -> BlockModel.fromStream(new StringReader(json))).orElse(null);
    }

    public static BlockModel createItemModel(Identifier resourceLocation) {
        return fromPattern(PatternsHelper.createItemGenerated(resourceLocation));
    }

    public static BlockModel createHandheldItem(Identifier resourceLocation) {
        return fromPattern(PatternsHelper.createItemHandheld(resourceLocation));
    }

    public static BlockModel createBlockItem(Identifier resourceLocation) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.ITEM_BLOCK, resourceLocation);
        return fromPattern(pattern);
    }

    public static BlockModel createBlockEmpty(Identifier resourceLocation) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_EMPTY, resourceLocation);
        return fromPattern(pattern);
    }

    private static Variant createVariant(
            Identifier resourceLocation,
            Quadrant xRot,
            Quadrant yRot,
            boolean uvLock
    ) {
        Variant variant = new Variant(resourceLocation);
        if (xRot != Quadrant.R0) {
            variant = variant.withXRot(xRot);
        }
        if (yRot != Quadrant.R0) {
            variant = variant.withYRot(yRot);
        }
        if (uvLock) {
            variant = variant.withUvLock(true);
        }
        return variant;
    }

    public static BlockStateModel.UnbakedRoot createVariants(List<Variant> variants) {
        if (variants.isEmpty()) {
            throw new IllegalArgumentException("At least one variant required");
        }
        if (variants.size() == 1) {
            return new SingleVariant.Unbaked(variants.get(0)).asRoot();
        }

        WeightedList.Builder<BlockStateModel.Unbaked> builder = WeightedList.builder();
        variants.forEach(variant -> builder.add(new SingleVariant.Unbaked(variant)));
        return new WeightedVariants.Unbaked(builder.build()).asRoot();
    }

    private static BlockStateModel.UnbakedRoot createVariantRoot(
            Identifier resourceLocation,
            Quadrant xRot,
            Quadrant yRot,
            boolean uvLock
    ) {
        return new SingleVariant.Unbaked(createVariant(resourceLocation, xRot, yRot, uvLock)).asRoot();
    }

    public static BlockStateModel.UnbakedRoot createBlockSimple(Identifier resourceLocation) {
        return createVariantRoot(resourceLocation, Quadrant.R0, Quadrant.R0, false);
    }

    public static BlockStateModel.UnbakedRoot createFacingModel(
            Identifier resourceLocation,
            Direction facing,
            boolean uvLock,
            boolean inverted
    ) {
        if (inverted) {
            facing = facing.getOpposite();
        }
        return createVariantRoot(resourceLocation, Quadrant.R0, quadrantFromFacingYRot(facing), uvLock);
    }

    public static BlockStateModel.UnbakedRoot createRotatedModel(Identifier resourceLocation, Direction.Axis axis) {
        return switch (axis) {
            case X -> createVariantRoot(resourceLocation, Quadrant.R90, Quadrant.R90, false);
            case Z -> createVariantRoot(resourceLocation, Quadrant.R90, Quadrant.R0, false);
            case Y -> createVariantRoot(resourceLocation, Quadrant.R0, Quadrant.R0, false);
        };
    }

    public static BlockStateModel.UnbakedRoot createRandomTopModel(Identifier resourceLocation) {
        return createVariants(List.of(
                createVariant(resourceLocation, Quadrant.R0, Quadrant.R0, false),
                createVariant(resourceLocation, Quadrant.R0, Quadrant.R90, false),
                createVariant(resourceLocation, Quadrant.R0, Quadrant.R180, false),
                createVariant(resourceLocation, Quadrant.R0, Quadrant.R270, false)
        ));
    }

    private static Quadrant quadrantFromFacingYRot(Direction facing) {
        return switch (facing) {
            case SOUTH -> Quadrant.R0;
            case WEST -> Quadrant.R90;
            case NORTH -> Quadrant.R180;
            case EAST -> Quadrant.R270;
            default -> Quadrant.R0;
        };
    }

    public static class MultiPartBuilder {
        public static MultiPartBuilder create(StateDefinition<Block, BlockState> stateDefinition) {
            return new MultiPartBuilder(stateDefinition);
        }

        private final List<ModelPart> modelParts = Lists.newArrayList();
        private final StateDefinition<Block, BlockState> stateDefinition;

        private MultiPartBuilder(StateDefinition<Block, BlockState> stateDefinition) {
            this.stateDefinition = stateDefinition;
        }

        public ModelPart part(Identifier modelId) {
            return new ModelPart(modelId);
        }

        public MultiPartModel.Unbaked build() {
            if (modelParts.isEmpty()) {
                throw new IllegalStateException("At least one model part need to be created.");
            }

            List<MultiPartModel.Selector<BlockStateModel.Unbaked>> selectors = Lists.newArrayList();
            modelParts.forEach(modelPart -> {
                Variant variant = createVariant(modelPart.modelId, modelPart.xRot, modelPart.yRot, modelPart.uvLock);
                selectors.add(new MultiPartModel.Selector<>(modelPart.condition, new SingleVariant.Unbaked(variant)));
            });
            modelParts.clear();
            return new MultiPartModel.Unbaked(selectors);
        }

        public class ModelPart {
            private final Identifier modelId;
            private Predicate<BlockState> condition = state -> true;
            private Quadrant xRot = Quadrant.R0;
            private Quadrant yRot = Quadrant.R0;
            private boolean uvLock = false;

            private ModelPart(Identifier modelId) {
                this.modelId = modelId;
            }

            public ModelPart setCondition(Function<BlockState, Boolean> condition) {
                this.condition = condition::apply;
                return this;
            }

            public ModelPart setXRotation(Quadrant rotation) {
                this.xRot = rotation;
                return this;
            }

            public ModelPart setYRotation(Quadrant rotation) {
                this.yRot = rotation;
                return this;
            }

            public ModelPart setUVLock(boolean value) {
                this.uvLock = value;
                return this;
            }

            public void add() {
                modelParts.add(this);
            }
        }
    }
}
