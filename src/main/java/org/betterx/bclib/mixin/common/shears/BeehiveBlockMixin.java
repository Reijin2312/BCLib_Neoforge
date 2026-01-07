package org.betterx.bclib.mixin.common.shears;

import org.betterx.bclib.items.tool.BaseShearsItem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BeehiveBlock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.common.ItemAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BeehiveBlock.class)
public class BeehiveBlockMixin {
    @WrapOperation(
            method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;canPerformAction(Lnet/neoforged/neoforge/common/ItemAbility;)Z"
            )
    )
    private boolean bclib_isShears(ItemStack stack, ItemAbility ability, Operation<Boolean> original) {
        return original.call(stack, ability) || BaseShearsItem.isShear(stack);
    }
}



