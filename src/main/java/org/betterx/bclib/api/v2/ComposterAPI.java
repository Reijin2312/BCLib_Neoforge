package org.betterx.bclib.api.v2;

import org.betterx.bclib.mixin.common.ComposterBlockAccessor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.ItemLike;

public class ComposterAPI {
    public static Block allowCompost(float chance, Block block) {
        if (block != null) {
            allowCompost(chance, block.asItem());
        }
        return block;
    }

    public static Item allowCompost(float chance, Item item) {
        if (item != null && item != Items.AIR) {
            // Vanilla now exposes the compostable registry map directly; populate it here.
            ComposterBlockAccessor.bclib_getCompostables().put((ItemLike) item, chance);
        }
        return item;
    }
}
