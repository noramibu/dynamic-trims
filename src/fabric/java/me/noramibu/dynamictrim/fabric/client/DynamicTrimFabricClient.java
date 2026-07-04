package me.noramibu.dynamictrim.fabric.client;

import me.noramibu.dynamictrim.client.DynamicTrimClient;
import net.fabricmc.api.ClientModInitializer;

public final class DynamicTrimFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DynamicTrimClient.init();
    }
}
