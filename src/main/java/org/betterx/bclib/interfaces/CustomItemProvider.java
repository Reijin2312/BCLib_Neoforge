package org.betterx.bclib.interfaces;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

@Deprecated(forRemoval = true)
public interface CustomItemProvider {
    /**
     * Used to replace default Block Item when block is registered.
     *
     * @return {@link BlockItem}
     */
    BlockItem getCustomItem(Identifier blockID, Item.Properties settings);
}
