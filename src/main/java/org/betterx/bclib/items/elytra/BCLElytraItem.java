package org.betterx.bclib.items.elytra;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

import net.neoforged.neoforge.common.extensions.IItemExtension;

public interface BCLElytraItem extends IItemExtension {
    ResourceLocation getModelTexture();

    double getMovementFactor();

    @Override
    default boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return ElytraItem.isFlyEnabled(stack);
    }

    @Override
    default boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        doVanillaElytraTick(entity, stack);
        return ElytraItem.isFlyEnabled(stack);
    }


    default void doVanillaElytraTick(LivingEntity entity, ItemStack chestStack) {
        vanillaElytraTick(entity, chestStack);
    }

    static void vanillaElytraTick(LivingEntity entity, ItemStack chestStack) {
        int nextRoll = entity.getFallFlyingTicks() + 1;

        if (!entity.level().isClientSide && nextRoll % 10 == 0) {
            if ((nextRoll / 10) % 2 == 0) {
                BCLElytraUtils.onBreak.accept(entity, chestStack);
                return;
            }

            entity.gameEvent(GameEvent.ELYTRA_GLIDE);
        }
    }
}
