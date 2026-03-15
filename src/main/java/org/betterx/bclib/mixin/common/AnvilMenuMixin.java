package org.betterx.bclib.mixin.common;

import org.betterx.bclib.blocks.BaseAnvilBlock;
import org.betterx.bclib.blocks.LeveledAnvilBlock;
import org.betterx.bclib.interfaces.AnvilScreenHandlerExtended;
import org.betterx.bclib.recipes.AnvilRecipe;
import org.betterx.bclib.recipes.AnvilRecipeInput;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(value = AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu implements AnvilScreenHandlerExtended {
    @Unique
    private List<RecipeHolder<AnvilRecipe>> bcl_recipes = Collections.emptyList();
    @Unique
    private @Nullable RecipeHolder<AnvilRecipe> bcl_currentRecipe;
    @Unique
    private DataSlot bcl_anvilLevel = DataSlot.standalone();
    @Unique
    private DataSlot bcl_recipeCount = DataSlot.standalone();
    @Unique
    private DataSlot bcl_hasActiveRecipe = DataSlot.standalone();

    public AnvilMenuMixin(
            @Nullable MenuType<?> menuType,
            int i,
            Inventory inventory,
            ContainerLevelAccess containerLevelAccess
    ) {
        super(menuType, i, inventory, containerLevelAccess, bclib_createInputSlotDefinitions());
    }

    @Unique
    private static ItemCombinerMenuSlotDefinition bclib_createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
                                             .withSlot(0, 27, 47, stack -> true)
                                             .withSlot(1, 76, 47, stack -> true)
                                             .withResultSlot(2, 134, 47)
                                             .build();
    }

    @Unique
    private AnvilRecipeInput bcl_AnvilRecipeInput(@Nullable TagKey<Item> allowedTools) {
        return new AnvilRecipeInput(this.inputSlots.getItem(0), this.inputSlots.getItem(1), allowedTools);
    }

    @Inject(
            remap = false,
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL"),
            require = 0
    )
    public void be_initAnvilLevel(int syncId, Inventory inventory, ContainerLevelAccess context, CallbackInfo info) {
        this.bcl_anvilLevel = addDataSlot(DataSlot.standalone());
        this.bcl_recipeCount = addDataSlot(DataSlot.standalone());
        this.bcl_hasActiveRecipe = addDataSlot(DataSlot.standalone());
        if (context != ContainerLevelAccess.NULL) {
            int level = context.evaluate((world, blockPos) -> {
                Block anvilBlock = world.getBlockState(blockPos).getBlock();
                return LeveledAnvilBlock.getAnvilCraftingLevel(anvilBlock);
            }, 0);
            bcl_anvilLevel.set(level);
        } else {
            bcl_anvilLevel.set(0);
        }
        bcl_recipeCount.set(0);
        bcl_hasActiveRecipe.set(0);
    }

    @Inject(remap = false, method = "mayPickup", at = @At("HEAD"), cancellable = true, require = 0)
    protected void bcl_canTakeOutput(Player player, boolean present, CallbackInfoReturnable<Boolean> info) {
        if (bcl_currentRecipe != null) {
            AnvilRecipeInput recipeInput = this.bcl_AnvilRecipeInput(bcl_currentRecipe.value().getAllowedTools());
            info.setReturnValue(bcl_currentRecipe.value().checkHammerDurability(recipeInput, player));
        }
    }

    @Inject(remap = false, method = "onTake", at = @At("HEAD"), cancellable = true, require = 0)
    protected void bcl_onTakeAnvilOutput(Player player, ItemStack stack, CallbackInfo info) {
        if (bcl_currentRecipe != null) {
            AnvilRecipeInput recipeInput = this.bcl_AnvilRecipeInput(bcl_currentRecipe.value().getAllowedTools());
            recipeInput.getIngredient().shrink(bcl_currentRecipe.value().getInputCount());
            bcl_currentRecipe.value().craft(recipeInput, player);
            slotsChanged(inputSlots);

            access.execute((level, blockPos) -> {
                final BlockState anvilState = level.getBlockState(blockPos);
                final Block anvilBlock = anvilState.getBlock();
                if (anvilBlock instanceof BaseAnvilBlock anvil) {
                    if (!player.hasInfiniteMaterials()
                            && anvilState.is(BlockTags.ANVIL)
                            && player.getRandom().nextDouble() < 0.1) {
                        BlockState damagedState = anvil.damageAnvilUse(anvilState, player.getRandom());
                        BaseAnvilBlock.destroyWhenNull(level, blockPos, damagedState);
                    } else {
                        level.levelEvent(LevelEvent.SOUND_ANVIL_USED, blockPos, 0);
                    }
                }
            });
            info.cancel();
        }
    }

    @Inject(remap = false, method = "onTake", at = @At("TAIL"), require = 0)
    private void bcl_afterOnTake(Player player, ItemStack stack, CallbackInfo info) {
        this.access.execute((level, blockPos) -> {
            BlockState state = level.getBlockState(blockPos);
            if (!player.hasInfiniteMaterials() && state.getBlock() instanceof BaseAnvilBlock anvil) {
                if (player.getRandom().nextDouble() < 0.12) {
                    BlockState damaged = anvil.damageAnvilUse(state, player.getRandom());
                    BaseAnvilBlock.destroyWhenNull(level, blockPos, damaged);
                }
            }
        });
    }

    @Inject(remap = false, method = "createResult", at = @At("HEAD"), cancellable = true, require = 0)
    public void bcl_updateOutput(CallbackInfo info) {
        AnvilRecipeInput recipeInput = this.bcl_AnvilRecipeInput(null);
        if (!(this.player.level().recipeAccess() instanceof RecipeManager recipeManager)) {
            // Client level uses ClientRecipeContainer, custom anvil recipes are resolved on server side.
            this.bcl_recipes = Collections.emptyList();
            this.bcl_currentRecipe = null;
            return;
        }

        bcl_recipes = recipeManager.recipeMap().getRecipesFor(AnvilRecipe.TYPE, recipeInput, player.level()).toList();

        if (!bcl_recipes.isEmpty()) {
            int anvilLevel = this.bcl_anvilLevel.get();
            bcl_recipes = bcl_recipes.stream()
                                     .filter(recipe -> anvilLevel >= recipe.value().getAnvilLevel())
                                     .collect(Collectors.toList());
            if (!bcl_recipes.isEmpty()) {
                if (bcl_currentRecipe == null || !bcl_recipes.contains(bcl_currentRecipe)) {
                    bcl_currentRecipe = bcl_recipes.get(0);
                }
                bcl_recipeCount.set(bcl_recipes.size());
                bcl_hasActiveRecipe.set(1);
                bcl_updateResult();
                info.cancel();
            } else {
                bcl_currentRecipe = null;
                bcl_recipeCount.set(0);
                bcl_hasActiveRecipe.set(0);
            }
        } else {
            bcl_currentRecipe = null;
            bcl_recipeCount.set(0);
            bcl_hasActiveRecipe.set(0);
        }
    }

    @Inject(remap = false, method = "setItemName", at = @At("HEAD"), cancellable = true, require = 0)
    public void bcl_setNewItemName(String string, CallbackInfoReturnable<Boolean> cir) {
        if (bcl_currentRecipe != null) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            be_previousRecipe();
            return true;
        } else if (id == 1) {
            be_nextRecipe();
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Unique
    private void bcl_updateResult() {
        if (bcl_currentRecipe == null) return;
        AnvilRecipeInput recipeInput = this.bcl_AnvilRecipeInput(bcl_currentRecipe.value().getAllowedTools());
        resultSlots.setItem(0, bcl_currentRecipe.value().assemble(recipeInput, this.player.level().registryAccess()));
        broadcastChanges();
    }

    @Override
    public void bcl_updateCurrentRecipe(RecipeHolder<AnvilRecipe> recipe) {
        this.bcl_currentRecipe = recipe;
        bcl_updateResult();
    }

    @Override
    public @Nullable RecipeHolder<AnvilRecipe> bcl_getCurrentRecipe() {
        return bcl_currentRecipe;
    }

    @Override
    public List<RecipeHolder<AnvilRecipe>> bcl_getRecipes() {
        return bcl_recipes;
    }

    @Override
    public boolean bcl_hasActiveRecipe() {
        return bcl_currentRecipe != null || bcl_hasActiveRecipe.get() > 0;
    }

    @Override
    public int bcl_getSyncedRecipeCount() {
        return Math.max(bcl_recipes.size(), bcl_recipeCount.get());
    }
}
