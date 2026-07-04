package me.noramibu.dynamictrim.client;

import me.noramibu.dynamictrim.DynamicTrim;
import me.noramibu.dynamictrim.client.adapters.DynamicTrimsTrimModelLoaderAdapter;
import me.noramibu.dynamictrim.client.mixin.ItemPropertiesAccessor;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public final class DynamicTrimClient {
    private static boolean modelPredicatesRegistered;

    public static void init() {
        RuntimeTrimsClient.init();
        registerModelPredicates();
        RuntimeTrimsClient.getItemModelLoader().setDefaultAdapter(new DynamicTrimsTrimModelLoaderAdapter());
        DynamicTrim.LOGGER.debug("{} Client Initialized", DynamicTrim.MOD_ID);
    }

    private static void registerModelPredicates() {
        if (modelPredicatesRegistered) {
            return;
        }
        ItemPropertiesAccessor.dynamictrim$registerGeneric(
                DynamicTrim.TRIM_PATTERN,
                (stack, world, entity, seed) -> {
                    ArmorTrim trim = stack.get(DataComponents.TRIM);
                    if (trim == null) {
                        return Float.NEGATIVE_INFINITY;
                    }
                    return DynamicTrimsTrimModelLoaderAdapter.getPatternIndex(trim);
                });
        modelPredicatesRegistered = true;
    }
}
