package org.betterx.bclib;

import org.betterx.bclib.api.v2.dataexchange.BCLibNetwork;
import org.betterx.bclib.api.v2.levelgen.LevelGenEvents;
import org.betterx.bclib.api.v2.levelgen.structures.TemplatePiece;
import org.betterx.bclib.api.v3.tag.BCLBlockTags;
import org.betterx.bclib.commands.CommandRegistry;
import org.betterx.bclib.commands.arguments.BCLibArguments;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.recipes.AlloyingRecipe;
import org.betterx.bclib.recipes.AnvilRecipe;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.bclib.util.BCLDataComponents;
import org.betterx.datagen.bclib.worldgen.BCLAutoBlockTagProvider;
import org.betterx.datagen.bclib.worldgen.BCLAutoItemTagProvider;
import org.betterx.wover.core.api.Logger;
import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.WoverDataGenEntryPoint;
import org.betterx.wover.state.api.WorldConfig;
import org.betterx.wover.ui.api.VersionChecker;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

@Mod(BCLib.MOD_ID)
public class BCLib {
    public static final String MOD_ID = "bclib";
    public static final ModCore C = ModCore.create(MOD_ID);
    public static final Logger LOGGER = C.LOG;

    public static final boolean RUNS_NULLSCAPE = ModList.get().isLoaded("nullscape");

    public BCLib(IEventBus modBus) {
        initialize(modBus);
    }

    private void onDatagen() {

    }


    private void initialize(IEventBus modBus) {
        modBus.addListener(BCLibNetwork::registerPayloadHandlers);
        modBus.addListener(BCLibArguments::register);
        modBus.addListener(net.neoforged.neoforge.registries.RegisterEvent.class, BCLDataComponents::register);
        modBus.addListener(net.neoforged.neoforge.registries.RegisterEvent.class, org.betterx.bclib.registry.BaseBlockEntities::register);
        modBus.addListener(net.neoforged.neoforge.registries.RegisterEvent.class, org.betterx.bclib.recipes.BCLRecipeManager::register);
        modBus.addListener(net.neoforged.neoforge.registries.RegisterEvent.class, org.betterx.bclib.api.v2.levelgen.structures.TemplatePiece::register);
        org.betterx.wover.block.api.BlockRegistry.hook(modBus);
        org.betterx.wover.item.api.ItemRegistry.hook(modBus);
        LevelGenEvents.register();
        BCLDataComponents.ensureStaticInitialization();
        BaseBlockEntities.register();
        WorldConfig.registerMod(C);
        AnvilRecipe.register();
        AlloyingRecipe.register();
        CommandRegistry.register();
        BCLBlockTags.ensureStaticallyLoaded();

        BCLibPatch.register();
        TemplatePiece.ensureStaticInitialization();
        Configs.save();

        VersionChecker.registerMod(C);

        if (isDatagen()) {
            WoverDataGenEntryPoint.registerAutoProvider(BCLAutoBlockTagProvider::new);
            WoverDataGenEntryPoint.registerAutoProvider(BCLAutoItemTagProvider::new);
            onDatagen();

        }
    }

    public static boolean isDevEnvironment() {
        return !FMLEnvironment.production;
    }

    public static boolean isDatagen() {
        return DatagenModLoader.isRunningDataGen();
    }

    public static boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    public static ResourceLocation makeID(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

}


