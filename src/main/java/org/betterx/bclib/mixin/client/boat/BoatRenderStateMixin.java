package org.betterx.bclib.mixin.client.boat;

import org.betterx.bclib.client.render.CustomBoatRenderState;
import org.betterx.bclib.items.boat.BoatTypeOverride;

import net.minecraft.client.renderer.entity.state.BoatRenderState;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BoatRenderState.class)
public class BoatRenderStateMixin implements CustomBoatRenderState {
    @Unique
    private @Nullable BoatTypeOverride bclib_customBoatType;
    @Unique
    private boolean bclib_chestBoat;

    @Override
    public void bclib_setCustomType(@Nullable BoatTypeOverride type) {
        this.bclib_customBoatType = type;
    }

    @Override
    public @Nullable BoatTypeOverride bclib_getCustomType() {
        return this.bclib_customBoatType;
    }

    @Override
    public void bclib_setChest(boolean chest) {
        this.bclib_chestBoat = chest;
    }

    @Override
    public boolean bclib_isChest() {
        return this.bclib_chestBoat;
    }
}
