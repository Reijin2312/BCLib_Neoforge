package org.betterx.bclib.api.v3.datagen;

import org.betterx.bclib.api.v2.advancement.AdvancementManager;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class AdvancementDataProvider implements DataProvider {
    protected final List<String> modIDs;
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;

    protected AdvancementDataProvider(
            List<String> modIDs,
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registryLookup
    ) {
        this.modIDs = modIDs;
        this.pathProvider = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
        this.registries = registryLookup;
    }

    protected abstract void bootstrap(HolderLookup.Provider registryLookup);

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return registries.thenCompose(lookup -> {
            bootstrap(lookup);
            Set<ResourceLocation> seen = new HashSet<>();
            List<CompletableFuture<?>> futures = new ArrayList<>();
            Consumer<AdvancementHolder> consumer = holder -> {
                if (!seen.add(holder.id())) {
                    throw new IllegalStateException("Duplicate advancement " + holder.id());
                }
                futures.add(DataProvider.saveStable(
                        output, lookup, Advancement.CODEC, holder.value(), pathProvider.json(holder.id())
                ));
            };
            AdvancementManager.registerAllDataGen(modIDs, consumer);
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Advancements";
    }
}
