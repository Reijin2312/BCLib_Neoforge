package org.betterx.bclib.interfaces;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemColorProvider {
    int getColor(ItemStack stack, int tintIndex);
}
