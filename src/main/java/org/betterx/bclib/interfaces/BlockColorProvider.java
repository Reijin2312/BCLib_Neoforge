package org.betterx.bclib.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface BlockColorProvider {
    int getColor(
            BlockState state,
            @Nullable BlockAndTintGetter level,
            @Nullable BlockPos pos,
            int tintIndex
    );
}
