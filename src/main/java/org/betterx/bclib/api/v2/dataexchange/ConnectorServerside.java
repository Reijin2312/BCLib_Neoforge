package org.betterx.bclib.api.v2.dataexchange;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.handler.DataExchange;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;


/**
 * This is an internal class that handles a Serverside Connection to a Client-Player
 */
public class ConnectorServerside extends Connector {
    private MinecraftServer server;

    ConnectorServerside(DataExchange api) {
        super(api);
        server = null;
    }

    @Override
    public boolean onClient() {
        return false;
    }

    public void onPlayInit(ServerGamePacketListenerImpl handler, MinecraftServer server) {
        if (this.server != null && this.server != server) {
            BCLib.LOGGER.warn("Server changed!");
        }
        this.server = server;
    }

    public void onPlayReady(ServerGamePacketListenerImpl handler, MinecraftServer server) {
        for (DataHandlerDescriptor<?> desc : getDescriptors()) {
            if (desc.sendOnJoin) {
                BaseDataHandler<?> h = desc.JOIN_INSTANCE.get();
                if (h.getOriginatesOnServer()) {
                    h.sendToClient(server, handler.player);
                }
            }
        }
    }

    public void onPlayDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server) {
    }

    public void sendToClient(BaseDataHandler<?> h) {
        if (server == null) {
            throw new RuntimeException("[internal error] Server not initialized yet!");
        }
        h.sendToClient(this.server);
    }
}
