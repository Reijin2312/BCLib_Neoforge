package org.betterx.bclib.api.v2.dataexchange.handler;

import org.betterx.bclib.api.v2.dataexchange.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = org.betterx.bclib.BCLib.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
abstract public class DataExchange {


    private static DataExchangeAPI instance;

    protected static DataExchangeAPI getInstance() {
        if (instance == null) {
            instance = new DataExchangeAPI();
        }
        return instance;
    }

    protected ConnectorServerside server;
    protected ConnectorClientside client;
    protected final Set<DataHandlerDescriptor> descriptors;


    private final boolean didLoadSyncFolder = false;

    abstract protected ConnectorClientside clientSupplier(DataExchange api);

    abstract protected ConnectorServerside serverSupplier(DataExchange api);

    protected DataExchange() {
        descriptors = new HashSet<>();
    }

    public Set<DataHandlerDescriptor> getDescriptors() {
        return descriptors;
    }

    public static DataHandlerDescriptor getDescriptor(ResourceLocation identifier) {
        return getInstance().descriptors.stream().filter(d -> d.equals(identifier)).findFirst().orElse(null);
    }

    public static void registerDescriptor(DataHandlerDescriptor descriptor) {
        getInstance().descriptors.add(descriptor);
    }

    @OnlyIn(Dist.CLIENT)
    protected void initClientside() {
        if (client != null) return;
        client = clientSupplier(this);
    }

    protected void initServerSide() {
        if (server != null) return;
        server = serverSupplier(this);
    }

    /**
     * Initializes all datastructures that need to exist in the client component.
     * <p>
     * This is automatically called by BCLib. You can register {@link DataHandler}-Objects before this Method is called
     */
    @OnlyIn(Dist.CLIENT)
    public static void prepareClientside() {
        DataExchange api = DataExchange.getInstance();
        BCLibNetwork.init();
        api.initClientside();

    }

    /**
     * Initializes all datastructures that need to exist in the server component.
     * <p>
     * This is automatically called by BCLib. You can register {@link DataHandler}-Objects before this Method is called
     */
    public static void prepareServerside() {
        DataExchange api = DataExchange.getInstance();
        BCLibNetwork.init();
        api.initServerSide();
    }


    /**
     * Automatically called before the player enters the world.
     * <p>
     * This is automatically called by BCLib. It will send all {@link DataHandler}-Objects that have {@link DataHandlerDescriptor#sendBeforeEnter} set to*
     * {@code true},
     */
    @OnlyIn(Dist.CLIENT)
    public static void sendOnEnter() {
        getInstance().descriptors.forEach((desc) -> {
            if (desc.sendBeforeEnter) {
                BaseDataHandler<?> h = (BaseDataHandler<?>) desc.JOIN_INSTANCE.get();
                if (!h.getOriginatesOnServer()) {
                    getInstance().client.sendToServer(h);
                }
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static ClientPacketListener lastClientConnection;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        ClientPacketListener connection = client.getConnection();
        if (connection == lastClientConnection) {
            return;
        }
        DataExchange api = getInstance();
        if (connection != null) {
            api.initClientside();
            api.client.onPlayInit(connection, client);
            api.client.onPlayReady(connection, client);
        } else if (lastClientConnection != null) {
            api.client.onPlayDisconnect(lastClientConnection, client);
        }
        lastClientConnection = connection;
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        DataExchange api = getInstance();
        api.initServerSide();
        ServerPlayer player = (ServerPlayer) event.getEntity();
        api.server.onPlayInit(player.connection, player.server);
        api.server.onPlayReady(player.connection, player.server);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        DataExchange api = getInstance();
        ServerPlayer player = (ServerPlayer) event.getEntity();
        api.server.onPlayDisconnect(player.connection, player.server);
    }


}

