package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import org.jetbrains.annotations.ApiStatus;

public class BCLAttachments {
    private static final Identifier BOAT_CUSTOM_TYPE_ID = BCLib.makeID("boat_custom_type");
    private static final String VALUE_KEY = "value";
    private static final int MAX_TYPE_NAME_LENGTH = 128;

    public static AttachmentType<String> BOAT_CUSTOM_TYPE;

    private static final IAttachmentSerializer<String> STRING_SERIALIZER = new IAttachmentSerializer<>() {
        @Override
        public String read(IAttachmentHolder holder, ValueInput valueInput) {
            return valueInput.getString(VALUE_KEY).orElse("");
        }

        @Override
        public boolean write(String value, ValueOutput valueOutput) {
            if (value == null || value.isEmpty()) {
                return false;
            }
            valueOutput.putString(VALUE_KEY, value);
            return true;
        }
    };

    private static final StreamCodec<RegistryFriendlyByteBuf, String> STRING_STREAM_CODEC = StreamCodec.of(
            (buf, value) -> buf.writeUtf(value, MAX_TYPE_NAME_LENGTH),
            buf -> buf.readUtf(MAX_TYPE_NAME_LENGTH)
    );

    public static void register(RegisterEvent event) {
        event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, helper -> {
            AttachmentType<String> type = AttachmentType.builder(() -> "")
                    .serialize(STRING_SERIALIZER)
                    .sync(STRING_STREAM_CODEC)
                    .build();
            helper.register(BOAT_CUSTOM_TYPE_ID, type);
            BOAT_CUSTOM_TYPE = type;
        });
    }

    @ApiStatus.Internal
    public static void ensureStaticInitialization() {
    }
}
