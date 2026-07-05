package me.noramibu.dynamictrim.runtime.client.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class LayerData {
    private final Map<Identifier, Integer> maxSupportedLayers = new HashMap<>();

    public void setMaxSupportedLayer(Identifier trimPattern, int layer) {
        int existingLayer = maxSupportedLayers.getOrDefault(trimPattern, -1);
        if (layer > existingLayer) {
            maxSupportedLayers.put(trimPattern, layer);
        }
    }

    public int getMaxSupportedLayer(Identifier trimPattern) {
        return maxSupportedLayers.getOrDefault(trimPattern, -1) + 1;
    }
}
