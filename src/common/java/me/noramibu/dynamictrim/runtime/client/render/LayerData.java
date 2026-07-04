package me.noramibu.dynamictrim.runtime.client.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class LayerData {
    private final Map<ResourceLocation, Integer> maxSupportedLayers = new HashMap<>();
    private final Map<Item, Integer> trimStartLayers = new HashMap<>();

    public void setMaxSupportedLayer(ResourceLocation trimPattern, int layer) {
        int existingLayer = maxSupportedLayers.getOrDefault(trimPattern, -1);
        if (layer > existingLayer) {
            maxSupportedLayers.put(trimPattern, layer);
        }
    }

    public void setTrimStartLayer(Item item, int layer) {
        trimStartLayers.put(item, layer);
    }

    public int getMaxSupportedLayer(ResourceLocation trimPattern) {
        return maxSupportedLayers.getOrDefault(trimPattern, -1) + 1;
    }

    public int getTrimStartLayer(Item item) {
        return trimStartLayers.getOrDefault(item, -1);
    }
}
