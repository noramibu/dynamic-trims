package me.noramibu.dynamictrim.runtime.client.mixin.resource;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import me.noramibu.dynamictrim.runtime.client.resource.DynamicTrimClasspathResources;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPackResourceManager.class)
public abstract class MultiPackResourceManagerMixin {
    @Inject(method = "getResource", at = @At("RETURN"), cancellable = true)
    private void dynamictrim$getBundledResource(
            Identifier id, CallbackInfoReturnable<Optional<Resource>> cir) {
        if (cir.getReturnValue().isEmpty()) {
            DynamicTrimClasspathResources.getResource(id)
                    .ifPresent(resource -> cir.setReturnValue(Optional.of(resource)));
        }
    }

    @Inject(method = "listResources", at = @At("RETURN"), cancellable = true)
    private void dynamictrim$listBundledResources(
            String path,
            Predicate<Identifier> filter,
            CallbackInfoReturnable<Map<Identifier, Resource>> cir) {
        Map<Identifier, Resource> bundled =
                DynamicTrimClasspathResources.listResources(path, filter);
        if (bundled.isEmpty()) {
            return;
        }
        Map<Identifier, Resource> merged = new TreeMap<>(cir.getReturnValue());
        merged.putAll(bundled);
        cir.setReturnValue(merged);
    }
}
