package me.noramibu.dynamictrim.runtime.client.mixin.model;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Mixin(ModelManager.class)
public abstract class BakedModelManagerMixin {
    @Shadow @Final private AtlasSet atlases;

    @Shadow private int maxMipmapLevels;

    @ModifyExpressionValue(
            method = "method_45895",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/FileToIdConverter;listMatchingResources(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;"
            )
    )
    private static Map<ResourceLocation, Resource> addDynamicTrimModels(Map<ResourceLocation, Resource> original) {
        return RuntimeTrimsClient.getItemModelLoader().loadModels(original);
    }

    @Inject(
            method = "reload",
            at = @At("HEAD")
    )
    private void preLoadAtlases(PreparableReloadListener.PreparationBarrier synchronizer, ResourceManager manager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        atlases.scheduleLoad(manager, maxMipmapLevels, prepareExecutor).values().forEach(CompletableFuture::join);
    }
}
