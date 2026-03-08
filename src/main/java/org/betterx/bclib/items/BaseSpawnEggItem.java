package org.betterx.bclib.items;

import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.ItemModelProvider;
import org.betterx.wover.item.api.ItemRegistry;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;


import java.util.Optional;

public class BaseSpawnEggItem extends SpawnEggItem implements ItemModelProvider {
    public BaseSpawnEggItem(EntityType<? extends Mob> type, int primaryColor, int secondaryColor, Properties settings) {
        super(withSpawnEggId(type, settings));
    }

    private static Properties withSpawnEggId(EntityType<? extends Mob> type, Properties settings) {
        Properties props = settings.spawnEgg(type);
        ResourceKey<Item> itemId = ItemRegistry.peekConstructionId();
        if (itemId != null) {
            props.setId(itemId);
        }
        return props;
    }

    @Override
    public BlockModel getItemModel(Identifier resourceLocation) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.ITEM_SPAWN_EGG, resourceLocation);
        return ModelsHelper.fromPattern(pattern);
    }
}
