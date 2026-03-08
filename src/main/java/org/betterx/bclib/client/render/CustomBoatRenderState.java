package org.betterx.bclib.client.render;

import org.betterx.bclib.items.boat.BoatTypeOverride;

import org.jetbrains.annotations.Nullable;

public interface CustomBoatRenderState {
    void bclib_setCustomType(@Nullable BoatTypeOverride type);

    @Nullable
    BoatTypeOverride bclib_getCustomType();

    void bclib_setChest(boolean chest);

    boolean bclib_isChest();
}
