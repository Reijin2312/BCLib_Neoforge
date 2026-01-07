package org.betterx.bclib.mixin.common;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import org.betterx.bclib.BCLib;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = IdMapper.class)
public class IdMapperDebugMixin<T> {
    private static final int MAX_LOGS = 20;
    private static final AtomicInteger DUP_LOGS = new AtomicInteger();

    @Shadow
    private Reference2IntMap<T> tToId;

    @Shadow
    private List<T> idToT;

    @Inject(method = "addMapping", at = @At("HEAD"), cancellable = true)
    private void bclib_logDuplicate(T value, int id, CallbackInfo ci) {
        if (!(value instanceof BlockState state)) {
            return;
        }

        int existingId = tToId.getInt(value);
        if (existingId != -1 && existingId != id) {
            if (shouldLog()) {
                logRemap(state, existingId, id);
            }
            ci.cancel();
            return;
        }
        if (existingId != -1) {
            ci.cancel();
            return;
        }

        if (id < idToT.size()) {
            T existingAtId = idToT.get(id);
            if (existingAtId != null && existingAtId != value && existingAtId instanceof BlockState other) {
                if (shouldLog()) {
                    logOverwrite(state, other, id);
                }
                ci.cancel();
            }
        }
    }

    private static boolean shouldLog() {
        int logged = DUP_LOGS.getAndIncrement();
        if (logged < MAX_LOGS) {
            return true;
        }
        if (logged == MAX_LOGS) {
            BCLib.LOGGER.error("BlockState IdMapper log limit reached (" + MAX_LOGS + ")");
        }
        return false;
    }

    private static void logRemap(BlockState state, int oldId, int newId) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String name = key == null ? "unknown" : key.toString();
        BCLib.LOGGER.error(
                "BlockState remapped: " + name + " oldId=" + oldId + " newId=" + newId,
                new RuntimeException("BlockState IdMapper duplicate")
        );
    }

    private static void logOverwrite(BlockState state, BlockState other, int id) {
        ResourceLocation currentKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        ResourceLocation otherKey = BuiltInRegistries.BLOCK.getKey(other.getBlock());
        String currentName = currentKey == null ? "unknown" : currentKey.toString();
        String otherName = otherKey == null ? "unknown" : otherKey.toString();
        BCLib.LOGGER.error(
                "BlockState id reused: id=" + id + " new=" + currentName + " old=" + otherName,
                new RuntimeException("BlockState IdMapper overwrite")
        );
    }
}
