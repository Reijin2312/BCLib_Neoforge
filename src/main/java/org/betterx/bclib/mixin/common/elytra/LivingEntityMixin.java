package org.betterx.bclib.mixin.common.elytra;

import org.betterx.bclib.items.elytra.BCLElytraItem;
import org.betterx.bclib.items.elytra.BCLElytraUtils;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = LivingEntity.class, priority = 199, remap = false)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot equipmentSlot);

    // require=0 makes this optional if the signature shifts; prevents hard crash in runtime mapping drift
    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true, require = 0, remap = false)
    private Vec3 bclib_adjustTravelInput(Vec3 moveDelta) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.isFallFlying()) return moveDelta;

        ItemStack itemStack = BCLElytraUtils.slotProvider == null
                ? getItemBySlot(EquipmentSlot.CHEST)
                : BCLElytraUtils.slotProvider.getElytra(self, this::getItemBySlot);

        if (itemStack != null && itemStack.getItem() instanceof BCLElytraItem elytra) {
            double movementFactor = elytra.getMovementFactor();
            return moveDelta.multiply(movementFactor, 1.0D, movementFactor);
        }

        return moveDelta;
    }
}
