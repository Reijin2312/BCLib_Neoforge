package org.betterx.bclib.api.v2.dataexchange;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.handler.DataExchange;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


/**
 * This is an internal class that handles a Clienetside players Connection to a Server
 */
@OnlyIn(Dist.CLIENT)
public class ConnectorClientside extends Connector {
    private Minecraft client;

    ConnectorClientside(DataExchange api) {
        super(api);
        this.client = null;
    }


    @Override
    public boolean onClient() {
        return true;
    }

    public void onPlayInit(ClientPacketListener handler, Minecraft client) {
        if (this.client != null && this.client != client) {
            BCLib.LOGGER.warn("Client changed!");
        }
        this.client = client;
    }

    public void onPlayReady(ClientPacketListener handler, Minecraft client) {
        for (DataHandlerDescriptor<?> desc : getDescriptors()) {
            if (desc.sendOnJoin) {
                BaseDataHandler h = desc.JOIN_INSTANCE.get();
                if (!h.getOriginatesOnServer()) {
                    h.sendToServer(client);
                }
            }
        }
    }

    public void onPlayDisconnect(ClientPacketListener handler, Minecraft client) {
        this.client = null;
    }

    @Override
    public void sendToServer(BaseDataHandler<?> h) {
        if (client == null) {
            throw new RuntimeException("[internal error] Client not initialized yet!");
        }
        h.sendToServer(this.client);
    }
}

