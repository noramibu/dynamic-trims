package me.noramibu.dynamictrim.runtime.client.mixin.model;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(ModelManager.class)
public abstract class BakedModelManagerMixin {
    @Shadow @Final private AtlasSet atlases;

    @Shadow private int maxMipmapLevels;

    @ModifyArg(
            method = "loadBlockModels",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 0
    )
    private static Supplier<Map<ResourceLocation, Resource>> addDynamicTrimModels(Supplier<Map<ResourceLocation, Resource>> original) {
        return () -> RuntimeTrimsClient.getItemModelLoader().loadModels(original.get());
    }

    @Inject(
            method = "reload",
            at = @At("HEAD")
    )
    private void preLoadAtlases(PreparableReloadListener.PreparationBarrier synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        atlases.scheduleLoad(manager, maxMipmapLevels, prepareExecutor).values().forEach(CompletableFuture::join);
    }
}
