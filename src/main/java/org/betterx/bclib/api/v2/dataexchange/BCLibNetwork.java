package org.betterx.bclib.api.v2.dataexchange;

import org.betterx.bclib.BCLib;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BCLibNetwork {
    private BCLibNetwork() {
    }

    public static void init() {
        // payloads are registered via RegisterPayloadHandlersEvent
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(BCLib.MOD_ID);
        registrar.playBidirectional(BCLibPayload.TYPE, BCLibPayload.STREAM_CODEC, BCLibNetwork::handleBidirectional);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(BCLibPayload.from(payload));
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, BCLibPayload.from(payload));
    }

    private static void handleFromClient(BCLibPayload message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        context.enqueueWork(() -> {
            CustomPacketPayload payload = message.toPayload();
            if (payload == null) {
                return;
            }
            DataHandlerDescriptor<?> descriptor = DataExchangeAPI.getDescriptor(message.id());
            if (descriptor == null) {
                return;
            }
            descriptor.receiveFromClient(payload, new ServerPacketSender(player), player);
        });
    }

    private static void handleFromServer(BCLibPayload message, IPayloadContext context) {
        context.enqueueWork(() -> {
            CustomPacketPayload payload = message.toPayload();
            if (payload == null) {
                return;
            }
            DataHandlerDescriptor<?> descriptor = DataExchangeAPI.getDescriptor(message.id());
            if (descriptor == null) {
                return;
            }
            descriptor.receiveFromServer(payload, ClientPacketSender.INSTANCE, Minecraft.getInstance());
        });
    }

    private static void handleBidirectional(BCLibPayload message, IPayloadContext context) {
        if (context.flow() == PacketFlow.SERVERBOUND) {
            handleFromClient(message, context);
        } else {
            handleFromServer(message, context);
        }
    }

    private static final class ClientPacketSender implements PacketSender {
        private static final ClientPacketSender INSTANCE = new ClientPacketSender();

        @Override
        public void send(CustomPacketPayload payload) {
            sendToServer(payload);
        }
    }

    private static final class ServerPacketSender implements PacketSender {
        private final ServerPlayer player;

        private ServerPacketSender(ServerPlayer player) {
            this.player = player;
        }

        @Override
        public void send(CustomPacketPayload payload) {
            sendToPlayer(player, payload);
        }
    }
}
