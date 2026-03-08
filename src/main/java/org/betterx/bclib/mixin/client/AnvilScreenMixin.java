package org.betterx.bclib.mixin.client;

import org.betterx.bclib.interfaces.AnvilScreenHandlerExtended;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

import com.google.common.collect.Lists;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = AnvilScreen.class)
public abstract class AnvilScreenMixin extends ItemCombinerScreen<AnvilMenu> {
    @Shadow
    private EditBox name;

    @Shadow
    @Final
    private static Identifier ANVIL_LOCATION;

    @Unique
    private final List<Button> bcl_buttons = Lists.newArrayList();

    public AnvilScreenMixin(AnvilMenu handler, Inventory playerInventory, Component title) {
        super(handler, playerInventory, title, ANVIL_LOCATION);
    }

    @Inject(remap = false, method = "subInit", at = @At("TAIL"), require = 0)
    protected void be_subInit(CallbackInfo info) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        bcl_buttons.clear();

        bcl_buttons.add(Button.builder(Component.literal("<"), b -> be_clickRecipeButton(0))
                              .bounds(x + 8, y + 45, 15, 20)
                              .build());
        bcl_buttons.add(Button.builder(Component.literal(">"), b -> be_clickRecipeButton(1))
                              .bounds(x + 154, y + 45, 15, 20)
                              .build());

        for (Button widget : bcl_buttons) {
            widget.visible = false;
            addRenderableWidget(widget);
        }
    }

    @Inject(remap = false, method = "slotChanged", at = @At("HEAD"), cancellable = true, require = 0)
    public void be_onSlotUpdate(AbstractContainerMenu handler, int slotId, ItemStack stack, CallbackInfo info) {
        AnvilScreenHandlerExtended anvilHandler = (AnvilScreenHandlerExtended) handler;
        if (anvilHandler.bcl_getCurrentRecipe() != null) {
            boolean visible = anvilHandler.bcl_getRecipes().size() > 1;
            bcl_buttons.forEach(button -> button.visible = visible);
            name.setValue("");
            info.cancel();
        } else {
            bcl_buttons.forEach(button -> button.visible = false);
        }
    }

    @Unique
    private void be_clickRecipeButton(int id) {
        if (id == 0) {
            ((AnvilScreenHandlerExtended) menu).be_previousRecipe();
        } else if (id == 1) {
            ((AnvilScreenHandlerExtended) menu).be_nextRecipe();
        }

        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }
}
