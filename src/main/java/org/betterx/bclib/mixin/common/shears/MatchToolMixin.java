package org.betterx.bclib.mixin.common.shears;

import org.betterx.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = MatchTool.class)
public class MatchToolMixin {
    @Unique
    private boolean bcl_isShears;

    @Inject(method = "<init>", at = @At(value = "TAIL"), remap = false)
    private void bcl_initShears(Optional<?> optional, CallbackInfo ci) {
        if (optional.isPresent()) {
            // Conservative fallback: custom tag-based shears support is only needed when vanilla MatchTool
            // targets shears. We detect by string to avoid hard-linking moved criterion APIs.
            bcl_isShears = optional.get().toString().contains("minecraft:shears");
        }
    }

    @Inject(
            method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z",
            at = @At(value = "HEAD"),
            cancellable = true,
            remap = false
    )
    private void bcl_isShears(LootContext lootContext, CallbackInfoReturnable<Boolean> cir) {
        if (bcl_isShears) {
            ItemStack itemStack = lootContext.getOptionalParameter(LootContextParams.TOOL);
            cir.setReturnValue(itemStack != null && itemStack.is(CommonItemTags.SHEARS));
        }
    }
}
