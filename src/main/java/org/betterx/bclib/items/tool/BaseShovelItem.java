package org.betterx.bclib.items.tool;

import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;


public class BaseShovelItem extends ShovelItem implements ItemModelProvider {
    public BaseShovelItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    public BaseShovelItem(ToolMaterial material, Properties settings) {
        super(material, 0.0F, 0.0F, settings);
    }

    @Override
    public BlockModel getItemModel(Identifier resourceLocation) {
        return ModelsHelper.createHandheldItem(resourceLocation);
    }
}
