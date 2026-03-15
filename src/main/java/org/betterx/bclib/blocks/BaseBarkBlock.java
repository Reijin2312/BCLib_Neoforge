package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


public abstract class BaseBarkBlock extends BaseRotatedPillarBlock {
    protected BaseBarkBlock(Properties settings) {
        super(settings);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void provideBlockModels(Object modelGenerator) {
    WoverBlockModelGenerators generator = (WoverBlockModelGenerators) modelGenerator;
        var res = TextureMapping.getBlockTexture(this);
        var log = Identifier.fromNamespaceAndPath(res.getNamespace(), res
                .getPath()
                .replace("_bark", "_log"));
        generator.createRotatedPillar(this, new TextureMapping()
                .put(TextureSlot.SIDE, log.withSuffix("_side"))
                .put(TextureSlot.END, log.withSuffix("_side")));

    }

    public static class Wood extends BaseBarkBlock implements BehaviourWood, BlockTagProvider, ItemTagProvider {
        private final boolean flammable;

        public Wood(Properties settings, boolean flammable) {
            super(flammable ? settings.ignitedByLava() : settings);
            this.flammable = flammable;
        }

        @Override
        public void registerBlockTags(Identifier location, TagBootstrapContext<Block> context) {
            context.add(BlockTags.LOGS, this);
            if (flammable) {
                context.add(BlockTags.LOGS_THAT_BURN, this);
            }
        }

        @Override
        public void registerItemTags(Identifier location, ItemTagBootstrapContext context) {
            context.add(ItemTags.LOGS, this);
            if (flammable) {
                context.add(ItemTags.LOGS_THAT_BURN, this);
            }
        }
    }
}
