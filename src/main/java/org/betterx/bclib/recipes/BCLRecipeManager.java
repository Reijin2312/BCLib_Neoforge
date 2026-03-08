package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.wover.config.api.DatapackConfigs;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import net.neoforged.neoforge.registries.RegisterEvent;

public class BCLRecipeManager {
    public static final Identifier RECIPES_CONFIG_FILE = BCLib.C.id("recipes.json");
    private static final Map<Identifier, RecipeSerializer<?>> SERIALIZERS = new LinkedHashMap<>();
    private static final Map<Identifier, RecipeType<?>> TYPES = new LinkedHashMap<>();

    public static <C extends RecipeInput, S extends RecipeSerializer<T>, T extends Recipe<C>> S registerSerializer(
            String modID,
            String id,
            S serializer
    ) {
        Identifier rl = Identifier.fromNamespaceAndPath(modID, id);
        @SuppressWarnings("unchecked") S existing = (S) SERIALIZERS.get(rl);
        if (existing != null) return existing;
        SERIALIZERS.put(rl, serializer);
        return serializer;
    }

    public static <C extends RecipeInput, T extends Recipe<C>> RecipeType<T> registerType(String modID, String type) {
        Identifier recipeTypeId = Identifier.fromNamespaceAndPath(modID, type);
        @SuppressWarnings("unchecked") RecipeType<T> existing = (RecipeType<T>) TYPES.get(recipeTypeId);
        if (existing != null) return existing;

        RecipeType<T> res = new RecipeType<T>() {
            public String toString() {
                return type;
            }
        };
        TYPES.put(recipeTypeId, res);
        return res;
    }

    public static boolean exists(ItemLike item) {
        if (item instanceof Block) {
            return BuiltInRegistries.BLOCK.getKey((Block) item) != BuiltInRegistries.BLOCK.getDefaultKey();
        } else {
            return item != Items.AIR && BuiltInRegistries.ITEM.getKey(item.asItem()) != BuiltInRegistries.ITEM.getDefaultKey();
        }
    }

    private final static HashSet<Identifier> disabledRecipes = new HashSet<>();

    private static void clearRecipeConfig() {
        disabledRecipes.clear();
    }

    private static void processRecipeConfig(@NotNull Identifier sourceId, @NotNull JsonObject root) {
        if (root.has("disable")) {
            root
                    .getAsJsonArray("disable")
                    .asList()
                    .stream()
                    .map(el -> Identifier.tryParse(el.getAsString()))
                    .filter(id -> id != null)
                    .forEach(disabledRecipes::add);
        }
    }

    @ApiStatus.Internal
    public static void removeDisabledRecipes(ResourceManager manager, Map<Identifier, JsonElement> map) {
        clearRecipeConfig();
        DatapackConfigs
                .instance()
                .runForResource(manager, RECIPES_CONFIG_FILE, BCLRecipeManager::processRecipeConfig);

        for (Identifier id : disabledRecipes) {
            BCLib.LOGGER.verbose("Disabling Recipe: {}", id);

            map.remove(id);
        }
    }

    @ApiStatus.Internal
    public static RecipeMap removeDisabledRecipes(ResourceManager manager, RecipeMap recipeMap) {
        clearRecipeConfig();
        DatapackConfigs
                .instance()
                .runForResource(manager, RECIPES_CONFIG_FILE, BCLRecipeManager::processRecipeConfig);

        if (disabledRecipes.isEmpty()) {
            return recipeMap;
        }

        for (Identifier id : disabledRecipes) {
            BCLib.LOGGER.verbose("Disabling Recipe: {}", id);
        }

        ArrayList<RecipeHolder<?>> filtered = new ArrayList<>();
        for (RecipeHolder<?> holder : recipeMap.values()) {
            if (!disabledRecipes.contains(holder.id().identifier())) {
                filtered.add(holder);
            }
        }

        return RecipeMap.create(filtered);
    }

    public static void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
            event.register(Registries.RECIPE_SERIALIZER, helper -> SERIALIZERS.forEach(helper::register));
        } else if (event.getRegistryKey().equals(Registries.RECIPE_TYPE)) {
            event.register(Registries.RECIPE_TYPE, helper -> TYPES.forEach(helper::register));
        }
    }
}
