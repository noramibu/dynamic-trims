package me.noramibu.dynamictrim.runtime;

import me.noramibu.dynamictrim.DynamicTrim;
import me.noramibu.dynamictrim.runtime.registry.TrimMaterialRegistryInjector;
import me.noramibu.dynamictrim.runtime.tag.TrimTagInjector;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RuntimeTrims {
    public static final String MOD_ID = DynamicTrim.MOD_ID;
    public static final String DYNAMIC = "dynamic";
    public static final float MATERIAL_MODEL_INDEX = 0.6632484f;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final TrimMaterialRegistryInjector trimMaterialRegistryInjector = new TrimMaterialRegistryInjector();
    private static final TrimTagInjector trimTagInjector = new TrimTagInjector();

    public static void init() {
        LOGGER.debug("{} Initialized", MOD_ID);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static TrimMaterialRegistryInjector getTrimMaterialRegistryInjector() {
        return trimMaterialRegistryInjector;
    }

    public static TrimTagInjector getTrimTagInjector() {
        return trimTagInjector;
    }
}
