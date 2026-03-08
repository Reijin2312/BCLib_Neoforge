package org.betterx.bclib.mixin.client;

import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = net.minecraft.client.multiplayer.ClientPacketListener.class)
public class ClientPacketListenerMixin {
    // Less brittle than the old telemetry invoke target and sufficient for the client enter handshake.
    @Inject(remap = false, method = "handleLogin", at = @At("TAIL"), require = 0)
    public void bclib_onStart(CallbackInfo ci) {
        DataExchangeAPI.sendOnEnter();
    }
}
