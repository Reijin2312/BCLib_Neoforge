package org.betterx.bclib.integration.modmenu;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.gui.modmenu.MainScreen;

import net.minecraft.client.gui.screens.Screen;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.function.Function;

/**
 * NeoForge mod list config screen integration.
 */
public final class ModMenuEntryPoint {
    private ModMenuEntryPoint() {
    }

    public static void register() {
        registerFactory(BCLib.MOD_ID, parent -> new MainScreen(parent));
        ModMenu.screen.forEach(ModMenuEntryPoint::registerFactory);
    }

    private static void registerFactory(String modId, Function<Screen, Screen> factory) {
        IConfigScreenFactory screenFactory = (modContainer, parent) -> factory.apply(parent);
        ModList.get()
               .getModContainerById(modId)
               .ifPresent(container -> container.registerExtensionPoint(
                       IConfigScreenFactory.class,
                       screenFactory
               ));
    }
}
