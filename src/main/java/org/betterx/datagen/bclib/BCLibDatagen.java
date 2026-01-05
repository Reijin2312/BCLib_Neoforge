package org.betterx.datagen.bclib;

import org.betterx.bclib.BCLib;
import org.betterx.datagen.bclib.advancement.BCLAdvancementDataProvider;
import org.betterx.datagen.bclib.worldgen.BlockTagProvider;
import org.betterx.datagen.bclib.worldgen.BoneMealBlockTagProvider;
import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.PackBuilder;
import org.betterx.wover.datagen.api.WoverDataGenEntryPoint;

import net.minecraft.core.RegistrySetBuilder;

public class BCLibDatagen extends WoverDataGenEntryPoint {
    @Override
    protected void onInitializeProviders(PackBuilder globalPack) {
        BCLib.LOGGER.info("Bootstrap onInitializeDataGenerator");
        globalPack.addProvider(BoneMealBlockTagProvider::new);
        globalPack.addProvider(BlockTagProvider::new);

        globalPack.callOnInitializeDatapack((event, packOutput, location) -> {
            if (location == null) {
                event.addProvider(new BCLAdvancementDataProvider(packOutput, event.getLookupProvider()));
            }
        });
    }

    @Override
    protected ModCore modCore() {
        return BCLib.C;
    }

    @Override
    protected void onBuildRegistry(RegistrySetBuilder registryBuilder) {
        super.onBuildRegistry(registryBuilder);
    }
}
