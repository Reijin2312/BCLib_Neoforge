package org.betterx.bclib.items.elytra;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

import net.neoforged.neoforge.common.extensions.IItemExtension;

public interface BCLElytraItem extends IItemExtension {
    Identifier getModelTexture();

    double getMovementFactor();

    default boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return !stack.isDamaged() || stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    default boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        doVanillaElytraTick(entity, stack);
        return !stack.isDamaged() || stack.getDamageValue() < stack.getMaxDamage() - 1;
    }


    default void doVanillaElytraTick(LivingEntity entity, ItemStack chestStack) {
        vanillaElytraTick(entity, chestStack);
    }

    static void vanillaElytraTick(LivingEntity entity, ItemStack chestStack) {
        int nextRoll = entity.getFallFlyingTicks() + 1;

        if (!entity.level().isClientSide() && nextRoll % 10 == 0) {
            if ((nextRoll / 10) % 2 == 0) {
                BCLElytraUtils.onBreak.accept(entity, chestStack);
                return;
            }

            entity.gameEvent(GameEvent.ELYTRA_GLIDE);
        }
    }
}
