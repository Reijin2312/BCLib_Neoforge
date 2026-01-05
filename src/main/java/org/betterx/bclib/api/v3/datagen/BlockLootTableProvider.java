package org.betterx.bclib.api.v3.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class BlockLootTableProvider extends LootTableProvider {
    protected final List<String> modIDs;

    public BlockLootTableProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registryLookup,
            List<String> modIDs
    ) {
        super(
                output,
                Set.of(),
                List.of(new SubProviderEntry((lookup) -> new BlockLootSubProvider(modIDs), LootContextParamSets.BLOCK)),
                registryLookup
        );
        this.modIDs = modIDs;
    }

    private static class BlockLootSubProvider implements LootTableSubProvider {
        private final List<String> modIDs;

        private BlockLootSubProvider(List<String> modIDs) {
            this.modIDs = modIDs;
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
            for (Block block : BuiltInRegistries.BLOCK) {
                if (block instanceof LootDropProvider dropper) {
                    ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
                    if (id != null && shouldInclude(id)) {
                        LootTable.Builder builder = LootTable.lootTable();
                        dropper.getDroppedItemsBCL(builder);
                        biConsumer.accept(ResourceKey.create(Registries.LOOT_TABLE, id.withPrefix("block/")), builder);
                    }
                }
            }
        }

        private boolean shouldInclude(ResourceLocation id) {
            return modIDs == null || modIDs.isEmpty() || modIDs.contains(id.getNamespace());
        }
    }
}
