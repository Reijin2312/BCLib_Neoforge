package org.betterx.bclib.mixin.common;

import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = ShovelItem.class, remap = false)
public interface ShovelItemAccessor {
    @Accessor(value = "FLATTENABLES", remap = false)
    static Map<Block, BlockState> bclib_getFlattenables() {
        throw new AssertionError("@Accessor dummy body called");
    }
}
