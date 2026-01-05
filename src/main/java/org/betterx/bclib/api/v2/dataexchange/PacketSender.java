package org.betterx.bclib.api.v2.dataexchange;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface PacketSender {
    void send(CustomPacketPayload payload);
}
