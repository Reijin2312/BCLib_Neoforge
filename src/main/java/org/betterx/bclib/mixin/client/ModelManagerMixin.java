package org.betterx.bclib.mixin.client;

import org.betterx.bclib.client.BCLibClient;

import net.minecraft.server.packs.resources.PreparableReloadListener;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = net.minecraft.client.resources.model.ModelManager.class)
public class ModelManagerMixin {
    // Stable hook; custom runtime models only need the resource manager snapshot.
    @Inject(remap = false, method = "reload", at = @At("HEAD"), require = 0)
    private void bclib_loadCustomModels(
            PreparableReloadListener.SharedState sharedState,
            Executor executor,
            PreparableReloadListener.PreparationBarrier preparationBarrier,
            Executor executor2,
            CallbackInfoReturnable<CompletableFuture<Void>> cir
    ) {
        BCLibClient.lazyModelbakery().loadCustomModels(sharedState.resourceManager());
    }
}
