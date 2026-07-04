package me.noramibu.dynamictrim.fabric;

import me.noramibu.dynamictrim.DynamicTrim;
import net.fabricmc.api.ModInitializer;

public final class DynamicTrimFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DynamicTrim.init();
    }
}
