package org.betterx.bclib.items.tool;

import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;


public class BasePickaxeItem extends Item implements ItemModelProvider {
    public BasePickaxeItem(ToolMaterial material, int attackDamage, float attackSpeed, Properties settings) {
        super(settings.pickaxe(material, attackDamage, attackSpeed));
    }

    public BasePickaxeItem(ToolMaterial material, Properties settings) {
        this(material, 1, -2.8F, settings);
    }

    @Override
    public BlockModel getItemModel(Identifier resourceLocation) {
        return ModelsHelper.createHandheldItem(resourceLocation);
    }
}
