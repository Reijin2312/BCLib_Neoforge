package org.betterx.bclib.registry;

import org.betterx.bclib.BCLib;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * NeoForge fuel registry helper.
 */
@EventBusSubscriber(modid = BCLib.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class FuelRegistry {
    public static final FuelRegistry INSTANCE = new FuelRegistry();

    private final Map<Item, Integer> fuels = new IdentityHashMap<>();

    private FuelRegistry() {
    }

    public void add(ItemLike item, int burnTime) {
        fuels.put(item.asItem(), burnTime);
    }

    @SubscribeEvent
    public static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        Integer burnTime = INSTANCE.fuels.get(stack.getItem());
        if (burnTime != null) {
            event.setBurnTime(burnTime);
        }
    }
}
