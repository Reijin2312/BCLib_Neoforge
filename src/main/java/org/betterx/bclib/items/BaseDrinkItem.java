package org.betterx.bclib.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;

public class BaseDrinkItem extends ModelProviderItem {
    public BaseDrinkItem(Properties settings) {
        super(settings);
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 32;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(world, user, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        final FoodProperties foodProperties = stack.get(DataComponents.FOOD);
        boolean consumedViaComponent = false;
        if (foodProperties != null) {
            final Consumable consumable = stack.get(DataComponents.CONSUMABLE);
            int count = stack.getCount();
            if (consumable != null) {
                consumable.onConsume(level, user, stack);
                consumedViaComponent = true;
            }
            stack.setCount(count);
        }

        if (!consumedViaComponent && user instanceof ServerPlayer serverPlayerEntity) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
            serverPlayerEntity.awardStat(Stats.ITEM_USED.get(this));
        }

        if (user instanceof Player && !((Player) user).getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (!level.isClientSide()) {
            user.removeAllEffects();
        }

        return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
    }
}
