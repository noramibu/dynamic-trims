package me.noramibu.dynamictrim.runtime.client.mixin.model;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
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
    private static Supplier<Map<ResourceLocation, Resource>> addDynamicTrimModels(Supplier<Map<ResourceLocation, Resource>> original) {
        return () -> RuntimeTrimsClient.getItemModelLoader().loadModels(original.get());
    }
}
