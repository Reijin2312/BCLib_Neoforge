package org.betterx.bclib.items.tool;

import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BaseSwordItem extends SwordItem implements ItemModelProvider {
    public BaseSwordItem(Tier material, Properties settings) {
        super(material, settings);
    }

    public BaseSwordItem(Tier material, int attackDamage, float attackSpeed, Properties settings) {
        this(material, settings.attributes(SwordItem.createAttributes(material, attackDamage, attackSpeed)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return ModelsHelper.createHandheldItem(resourceLocation);
    }
}

