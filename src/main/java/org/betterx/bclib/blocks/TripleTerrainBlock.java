package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;
import org.betterx.wover.block.api.BlockProperties.TripleShape;

import com.mojang.math.Quadrant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;


import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class TripleTerrainBlock extends BaseTerrainBlock implements RuntimeBlockModelProvider {
    public static final EnumProperty<TripleShape> SHAPE = org.betterx.wover.block.api.BlockProperties.TRIPLE_SHAPE;

    public TripleTerrainBlock(Block baseBlock) {
        super(baseBlock, baseBlock.defaultMapColor());
        this.registerDefaultState(defaultBlockState().setValue(SHAPE, TripleShape.BOTTOM));
    }

    public TripleTerrainBlock(Block baseBlock, MapColor color) {
        super(baseBlock, color);
        this.registerDefaultState(defaultBlockState().setValue(SHAPE, TripleShape.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(SHAPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction dir = ctx.getClickedFace();
        TripleShape shape = dir == Direction.UP
                ? TripleShape.BOTTOM
                : dir == Direction.DOWN ? TripleShape.TOP : TripleShape.MIDDLE;
        return defaultBlockState().setValue(SHAPE, shape);
    }

    @Override
    public InteractionResult useWithoutItem(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        TripleShape shape = state.getValue(SHAPE);
        if (shape == TripleShape.BOTTOM) {
            return super.useWithoutItem(state, world, pos, player, hit);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        TripleShape shape = state.getValue(SHAPE);
        if (shape == TripleShape.BOTTOM) {
            super.randomTick(state, world, pos, random);
        } else if (random.nextInt(16) == 0) {
            boolean bottom = canStayBottom(world, pos);
            if (shape == TripleShape.TOP) {
                if (!bottom) {
                    world.setBlockAndUpdate(pos, Blocks.END_STONE.defaultBlockState());
                }
            } else {
                boolean top = canStay(state, world, pos) || isMiddle(world.getBlockState(pos.above()));
                if (!top && !bottom) {
                    world.setBlockAndUpdate(pos, Blocks.END_STONE.defaultBlockState());
                } else if (top && !bottom) {
                    world.setBlockAndUpdate(pos, state.setValue(SHAPE, TripleShape.BOTTOM));
                } else if (!top) {
                    world.setBlockAndUpdate(pos, state.setValue(SHAPE, TripleShape.TOP));
                }
            }
        }
    }

    protected boolean canStayBottom(LevelReader world, BlockPos pos) {
        BlockPos blockPos = pos.below();
        BlockState blockState = world.getBlockState(blockPos);
        if (isMiddle(blockState)) {
            return true;
        } else if (blockState.getFluidState().getAmount() == 8) {
            return false;
        } else {
            return !blockState.isFaceSturdy(world, blockPos, Direction.UP);
        }
    }

    @Override
    public Object getItemModel(Identifier blockId) {
        return getBlockModel(blockId, defaultBlockState());
    }

    @Override
    public @Nullable Object getBlockModel(Identifier blockId, BlockState blockState) {
        String path = blockId.getPath();
        Optional<String> pattern;
        if (isMiddle(blockState)) {
            Identifier topId = Identifier.fromNamespaceAndPath(blockId.getNamespace(), path + "_top");
            pattern = PatternsHelper.createBlockSimple(topId);
        } else {
            Map<String, String> textures = Maps.newHashMap();
            textures.put("%top%", "betterend:block/" + path + "_top");
            textures.put("%side%", "betterend:block/" + path + "_side");
            textures.put("%bottom%", "minecraft:block/end_stone");
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_TOP_SIDE_BOTTOM, textures);
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
        boolean isMiddle = isMiddle(blockState);
        String middle = isMiddle ? "_middle" : "";
        Identifier modelId = RuntimeBlockModelProvider.remapModelIdentifier(stateId, blockState, middle);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        if (isMiddle) {
            List<Variant> variants = new ArrayList<>(16);
            for (Quadrant xRot : ROTATIONS) {
                for (Quadrant yRot : ROTATIONS) {
                    Variant variant = new Variant(modelId);
                    if (xRot != Quadrant.R0) {
                        variant = variant.withXRot(xRot);
                    }
                    if (yRot != Quadrant.R0) {
                        variant = variant.withYRot(yRot);
                    }
                    variants.add(variant);
                }
            }
            return ModelsHelper.createVariants(variants);
        } else if (blockState.getValue(SHAPE) == TripleShape.TOP) {
            List<Variant> variants = new ArrayList<>(4);
            for (Quadrant yRot : ROTATIONS) {
                Variant variant = new Variant(modelId).withXRot(Quadrant.R180);
                if (yRot != Quadrant.R0) {
                    variant = variant.withYRot(yRot);
                }
                variants.add(variant);
            }
            return ModelsHelper.createVariants(variants);
        }
        return ModelsHelper.createRandomTopModel(modelId);
    }

    private static final Quadrant[] ROTATIONS = {
            Quadrant.R0,
            Quadrant.R90,
            Quadrant.R180,
            Quadrant.R270
    };

    protected boolean isMiddle(BlockState blockState) {
        return blockState.is(this) && blockState.getValue(SHAPE) == TripleShape.MIDDLE;
    }
}
