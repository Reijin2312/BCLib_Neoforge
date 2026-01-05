package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;

import net.neoforged.neoforge.registries.RegisterEvent;

import org.jetbrains.annotations.ApiStatus;

public class BCLDataComponents {
    public static DataComponentType<CustomData> ANVIL_ENTITY_DATA;
    private static final ResourceLocation ANVIL_ENTITY_DATA_ID = BCLib.makeID("anvil_entity_data");

    public static void register(RegisterEvent event) {
        event.register(Registries.DATA_COMPONENT_TYPE, helper -> {
            DataComponentType<CustomData> type = DataComponentType.<CustomData>builder()
                    .persistent(CustomData.CODEC)
                    .networkSynchronized(CustomData.STREAM_CODEC)
                    .build();
            helper.register(ANVIL_ENTITY_DATA_ID, type);
            ANVIL_ENTITY_DATA = type;
        });
    }

    @ApiStatus.Internal
    public static void ensureStaticInitialization() {
    }
}
