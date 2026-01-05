package org.betterx.bclib.api.v3.datagen;

import org.betterx.bclib.BCLib;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class RecipeDataProvider extends RecipeProvider {
    private static List<DatapackRecipeBuilder> RECIPES;

    @Nullable
    protected final List<String> modIDs;

    public RecipeDataProvider(
            @Nullable List<String> modIDs,
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, registriesFuture);
        this.modIDs = modIDs;
    }

    @Override
    public void buildRecipes(RecipeOutput exporter) {
        if (RECIPES == null) return;

        for (var r : RECIPES) {
            if (modIDs == null || modIDs.isEmpty() || modIDs.contains(r.getNamespace())) {
                r.build(exporter);
            }
        }
    }

    @ApiStatus.Internal
    public static void register(DatapackRecipeBuilder builder) {
        // This is only used with the data generator, so we do not keep this list on a regular run.
        if (!BCLib.isDatagen()) {
            return;
        }
        if (RECIPES == null) RECIPES = new ArrayList<>();
        RECIPES.add(builder);
    }
}
