package org.betterx.bclib.api.v2.dataexchange;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.level.ServerPlayer;

import org.betterx.bclib.api.v2.dataexchange.PacketSender;
import org.betterx.bclib.api.v2.dataexchange.handler.DataExchange;

import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class DataHandlerDescriptor<T extends DataHandlerDescriptor.PacketPayload<T>> {
    public enum Direction {
        CLIENT_TO_SERVER,
        SERVER_TO_CLIENT
    }

    public interface PayloadFactory<T extends PacketPayload<T>> {
        T create(FriendlyByteBuf buf);
    }

    public abstract static class PacketPayload<T extends PacketPayload<T>> implements CustomPacketPayload {
        protected final DataHandlerDescriptor<T> descriptor;

        protected PacketPayload(DataHandlerDescriptor<T> desc) {
            this.descriptor = desc;
        }

        protected abstract void write(FriendlyByteBuf buf);

        @Override
        public final @NotNull Type<T> type() {
            return this.descriptor.IDENTIFIER;
        }
    }

    public DataHandlerDescriptor(
            @NotNull Direction direction,
            @NotNull ResourceLocation identifier,
            @NotNull PayloadFactory<T> factory,
            @NotNull Supplier<BaseDataHandler<T>> instancer
    ) {
        this(direction, identifier, factory, instancer, instancer, false, false);
    }

    public DataHandlerDescriptor(
            @NotNull Direction direction,
            @NotNull ResourceLocation identifier,
            @NotNull PayloadFactory<T> factory,
            @NotNull Supplier<BaseDataHandler<T>> instancer,
            boolean sendOnJoin,
            boolean sendBeforeEnter
    ) {
        this(direction, identifier, factory, instancer, instancer, sendOnJoin, sendBeforeEnter);
    }

    public DataHandlerDescriptor(
            @NotNull Direction direction,
            @NotNull ResourceLocation identifier,
            @NotNull PayloadFactory<T> factory,
            @NotNull Supplier<BaseDataHandler<T>> receiv_instancer,
            @NotNull Supplier<BaseDataHandler<T>> join_instancer,
            boolean sendOnJoin,
            boolean sendBeforeEnter
    ) {
        this.DIRECTION = direction;
        this.INSTANCE = receiv_instancer;
        this.JOIN_INSTANCE = join_instancer;
        this.IDENTIFIER = new CustomPacketPayload.Type<>(identifier);

        this.sendOnJoin = sendOnJoin;
        this.sendBeforeEnter = sendBeforeEnter;

        this.PAYLOAD_FACTORY = factory;
        this.STREAM_CODEC = CustomPacketPayload.codec(
                PacketPayload::write,
                factory::create
        );

        DataExchange.registerDescriptor(this);
    }

    public final Direction DIRECTION;
    @NotNull
    public final StreamCodec<FriendlyByteBuf, T> STREAM_CODEC;
    public final boolean sendOnJoin;
    public final boolean sendBeforeEnter;
    @NotNull
    public final PayloadFactory<T> PAYLOAD_FACTORY;
    @NotNull
    public final CustomPacketPayload.Type<T> IDENTIFIER;
    @NotNull
    public final Supplier<BaseDataHandler<T>> INSTANCE;
    @NotNull
    public final Supplier<BaseDataHandler<T>> JOIN_INSTANCE;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ResourceLocation) {
            return o.equals(IDENTIFIER);
        }
        if (!(o instanceof DataHandlerDescriptor that)) return false;
        return IDENTIFIER.equals(that.IDENTIFIER);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IDENTIFIER);
    }

    void receiveFromServer(
            Object payload,
            PacketSender responseSender,
            Minecraft client
    ) {
        BaseDataHandler<T> h = this.INSTANCE.get();
        //noinspection unchecked
        h.receiveFromServer(
                client,
                client.getConnection(),
                (T) payload,
                responseSender
        );
    }

    void receiveFromClient(
            Object payload,
            PacketSender responseSender,
            ServerPlayer player
    ) {
        BaseDataHandler<T> h = this.INSTANCE.get();
        //noinspection unchecked
        h.receiveFromClient(
                player.server,
                player,
                player.connection,
                (T) payload,
                responseSender
        );
    }
}
