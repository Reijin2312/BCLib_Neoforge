package org.betterx.bclib.interfaces;

import org.betterx.bclib.client.models.ModelsHelper;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.Identifier;


public interface ItemModelProvider {
    default BlockModel getItemModel(Identifier resourceLocation) {
        return ModelsHelper.createItemModel(resourceLocation);
    }
}
