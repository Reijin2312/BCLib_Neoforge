package org.betterx.bclib.api.v2.dataexchange.handler;

import org.betterx.bclib.api.v2.dataexchange.ConnectorClientside;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = org.betterx.bclib.BCLib.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class DataExchangeClientEvents {
    private static ClientPacketListener lastClientConnection;

    private DataExchangeClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        ClientPacketListener connection = client.getConnection();
        if (connection == lastClientConnection) {
            return;
        }
        DataExchange api = DataExchange.getInstance();
        if (connection != null) {
            api.initClientside();
            ConnectorClientside connector = (ConnectorClientside) api.client;
            connector.onPlayInit(connection, client);
            connector.onPlayReady(connection, client);
        } else if (lastClientConnection != null) {
            ConnectorClientside connector = (ConnectorClientside) api.client;
            if (connector != null) {
                connector.onPlayDisconnect(lastClientConnection, client);
            }
        }
        lastClientConnection = connection;
    }
}
