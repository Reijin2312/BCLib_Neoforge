package org.betterx.bclib.items;

import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;


public class ModelProviderItem extends Item implements ItemModelProvider {
    public ModelProviderItem(Properties settings) {
        super(settings);
    }

    @Override
    public BlockModel getItemModel(Identifier resourceLocation) {
        return ModelsHelper.createItemModel(resourceLocation);
    }
}
