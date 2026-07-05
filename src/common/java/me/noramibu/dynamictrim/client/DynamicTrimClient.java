package me.noramibu.dynamictrim.client;

import me.noramibu.dynamictrim.DynamicTrim;
import me.noramibu.dynamictrim.client.adapters.DynamicTrimsTrimModelLoaderAdapter;
import me.noramibu.dynamictrim.client.mixin.SelectItemModelPropertiesAccessor;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;

public final class DynamicTrimClient {
    private static boolean modelPropertiesRegistered;

    public static void init() {
        RuntimeTrimsClient.init();
        registerModelProperties();
        RuntimeTrimsClient.getItemModelLoader().setDefaultAdapter(new DynamicTrimsTrimModelLoaderAdapter());
        DynamicTrim.LOGGER.debug("{} Client Initialized", DynamicTrim.MOD_ID);
    }

    private static void registerModelProperties() {
        if (modelPropertiesRegistered) {
            return;
        }
        SelectItemModelPropertiesAccessor.dynamictrim$getIdMapper()
                .put(DynamicTrim.TRIM_PATTERN, TrimPatternProperty.TYPE);
        modelPropertiesRegistered = true;
    }
}
