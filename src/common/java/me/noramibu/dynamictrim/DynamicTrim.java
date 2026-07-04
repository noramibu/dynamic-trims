package me.noramibu.dynamictrim;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DynamicTrim {
    public static final String MOD_ID = "dynamictrim";
    public static final ResourceLocation TRIM_PATTERN = id("trim_pattern");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        RuntimeTrims.init();
        LOGGER.debug("{} Initialized", MOD_ID);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
