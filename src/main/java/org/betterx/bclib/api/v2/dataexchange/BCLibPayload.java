package org.betterx.bclib.api.v2.dataexchange;

import org.betterx.bclib.BCLib;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class BCLibPayload implements CustomPacketPayload {
    public static final Type<BCLibPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BCLib.MOD_ID, "dataexchange"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BCLibPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> encode(payload, buf),
            buf -> decode(buf)
    );
    private final ResourceLocation id;
    private final byte[] data;

    public BCLibPayload(ResourceLocation id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public ResourceLocation id() {
        return id;
    }

    public byte[] data() {
        return data;
    }

    @Override
    public Type<BCLibPayload> type() {
        return TYPE;
    }

    public static BCLibPayload from(CustomPacketPayload payload) {
        DataHandlerDescriptor<?> descriptor = DataExchangeAPI.getDescriptor(payload.type().id());
        if (descriptor == null) {
            throw new IllegalStateException("Missing payload descriptor for " + payload.type().id());
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        @SuppressWarnings("unchecked")
        StreamCodec<FriendlyByteBuf, DataHandlerDescriptor.PacketPayload<?>> codec =
                (StreamCodec<FriendlyByteBuf, DataHandlerDescriptor.PacketPayload<?>>) descriptor.STREAM_CODEC;
        codec.encode(buffer, (DataHandlerDescriptor.PacketPayload<?>) payload);
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        return new BCLibPayload(descriptor.IDENTIFIER.id(), data);
    }

    public CustomPacketPayload toPayload() {
        DataHandlerDescriptor<?> descriptor = DataExchangeAPI.getDescriptor(id);
        if (descriptor == null) {
            return null;
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        return descriptor.STREAM_CODEC.decode(buffer);
    }

    public static void encode(BCLibPayload payload, FriendlyByteBuf buf) {
        buf.writeResourceLocation(payload.id);
        buf.writeByteArray(payload.data);
    }

    public static BCLibPayload decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        byte[] data = buf.readByteArray();
        return new BCLibPayload(id, data);
    }
}
