package me.noramibu.dynamictrim.runtime.client.mixin.model;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;

@Mixin(ModelManager.class)
public abstract class BakedModelManagerMixin {
    @ModifyArg(
            method = "loadBlockModels",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 0
    )
    private static Supplier<Map<Identifier, Resource>> addDynamicTrimModels(Supplier<Map<Identifier, Resource>> original) {
        return () -> RuntimeTrimsClient.getItemModelLoader().loadModels(original.get());
    }

    @Inject(
            method = "reload",
            at = @At("HEAD")
    )
    private void preLoadAtlases(PreparableReloadListener.SharedState sharedState, Executor prepareExecutor, PreparableReloadListener.PreparationBarrier synchronizer, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        sharedState.get(AtlasManager.PENDING_STITCH).get(AtlasIds.BLOCKS).join();
    }
}
