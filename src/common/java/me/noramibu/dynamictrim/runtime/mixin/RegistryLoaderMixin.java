package me.noramibu.dynamictrim.runtime.mixin;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Map;
import java.util.Optional;

@Mixin(RegistryDataLoader.class)
public abstract class RegistryLoaderMixin {
    @SuppressWarnings("unchecked")
    @Inject(
            method = "loadContentsFromManager",
            at = @At("TAIL")
    )
    private static <E> void addAllTrimMaterialsToRegistry(
            ResourceManager resourceManager,
            RegistryOps.RegistryInfoLookup infoGetter,
            WritableRegistry<E> registry,
            Decoder<E> elementDecoder,
            Map<ResourceKey<?>, Exception> errors,
            CallbackInfo ci) {
        if (registry.key().equals(Registries.TRIM_MATERIAL)) {
            if(!(registry instanceof MappedRegistry<?>)) {
                RuntimeTrims.LOGGER.error("Could not add materials to registry. RuntimeTrims will not work, expected \"{} for {}\" but found \"{} for {}\".",
                        MappedRegistry.class.getSimpleName(),
                        TrimMaterial.class.getSimpleName(),
                        registry.getClass().getSimpleName(),
                        "<unknown>"
                );
                return;
            }

            MappedRegistry<TrimMaterial> trimMaterialRegistry = (MappedRegistry<TrimMaterial>) registry;

            RegistrationInfo info = new RegistrationInfo(
                    Optional.of(new KnownPack(
                            RuntimeTrims.MOD_ID,
                            "runtime_trim_materials",
                            SharedConstants.getCurrentVersion().getId()
                    )),
                    Lifecycle.stable()
            );

            Map<ResourceLocation, Holder<Item>> newMaterials = RuntimeTrims.getTrimMaterialRegistryInjector().getNewMaterials(trimMaterialRegistry);


            for (Map.Entry<ResourceLocation, Holder<Item>> newMaterial : newMaterials.entrySet()) {
                ResourceKey<TrimMaterial> trimRegKey = ResourceKey.create(trimMaterialRegistry.key(), newMaterial.getKey());
                TrimMaterial itemMaterial = new TrimMaterial(
                        RuntimeTrims.DYNAMIC,
                        newMaterial.getValue(),
                        RuntimeTrims.MATERIAL_MODEL_INDEX,
                        Map.of(),
                        Component.translatable("dynamictrim.material", newMaterial.getValue().value().getDescription().getString())
                );
                trimMaterialRegistry.register(trimRegKey, itemMaterial, info);
            }

            RuntimeTrims.LOGGER.info("Added {} new trim materials!", newMaterials.size());
        }
    }
}
