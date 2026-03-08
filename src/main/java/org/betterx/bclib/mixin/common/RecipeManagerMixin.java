package org.betterx.bclib.mixin.common;

import org.betterx.bclib.recipes.BCLRecipeManager;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow
    private RecipeMap recipes;

    @ModifyVariable(
            remap = false,
            method = "apply",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private RecipeMap bcl_filterDisabledRecipes(
            RecipeMap recipeMap,
            RecipeMap originalRecipeMap,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        return BCLRecipeManager.removeDisabledRecipes(resourceManager, recipeMap);
    }

    @Inject(
            remap = false,
            method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true
    )
    private <I extends RecipeInput, T extends Recipe<I>> void bcl_sort(
            RecipeType<T> recipeType,
            I recipeInput,
            Level level,
            CallbackInfoReturnable<Optional<RecipeHolder<T>>> cir
    ) {
        var all = this.recipes.byType(recipeType)
                              .stream()
                              .filter(recipe -> recipe.value().matches(recipeInput, level))
                              .sorted((a, b) -> {
                                  var aId = a.id().identifier();
                                  var bId = b.id().identifier();
                                  if (aId.getNamespace().equals(bId.getNamespace())) {
                                      return aId.getPath().compareTo(bId.getPath());
                                  }
                                  if (aId.getNamespace().equals("minecraft") && !bId.getNamespace().equals("minecraft")) {
                                      return 1;
                                  } else if (!aId.getNamespace().equals("minecraft") && bId.getNamespace().equals("minecraft")) {
                                      return -1;
                                  } else {
                                      return aId.getNamespace().compareTo(bId.getNamespace());
                                  }
                              })
                              .toList();

        if (all.size() > 1) {
            cir.setReturnValue(Optional.of(all.getFirst()));
        }
    }
}
