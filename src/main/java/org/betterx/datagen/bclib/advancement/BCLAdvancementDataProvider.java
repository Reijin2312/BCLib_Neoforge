package org.betterx.datagen.bclib.advancement;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v3.datagen.AdvancementDataProvider;

import net.minecraft.core.HolderLookup;

import net.minecraft.data.PackOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BCLAdvancementDataProvider extends AdvancementDataProvider {
    public BCLAdvancementDataProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registryLookup
    ) {
        super(List.of(BCLib.MOD_ID), output, registryLookup);
    }

    @Override
    protected void bootstrap(HolderLookup.Provider registryLookup) {

    }
}
