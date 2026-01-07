package org.betterx.bclib.mixin.common;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ComposterBlock.class)
public interface ComposterBlockAccessor {
    @Accessor(value = "COMPOSTABLES")
    static Object2FloatMap<ItemLike> bclib_getCompostables() {
        throw new AssertionError("@Accessor dummy body called");
    }
}



