package org.betterx.bclib.client.textures;

import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;


public final class SpriteLister {
    private SpriteLister() {
    }

    public static SpriteSource of(String string) {
        return new DirectoryLister(string, string + "/");
    }
}
