package me.noramibu.dynamictrim.runtime.client.mixin.model;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientItemInfoLoader.class)
public abstract class ClientItemInfoLoaderMixin {
    @ModifyArg(
            method = "scheduleLoad",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 0
    )
    private static Supplier<Map<Identifier, Resource>> addDynamicTrimItemDefinitions(
            Supplier<Map<Identifier, Resource>> original) {
        return () -> RuntimeTrimsClient.getItemModelLoader().loadItemDefinitions(original.get());
    }
}
