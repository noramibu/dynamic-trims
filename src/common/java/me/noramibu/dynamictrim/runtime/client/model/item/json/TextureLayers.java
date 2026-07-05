package me.noramibu.dynamictrim.runtime.client.model.item.json;

import java.util.Map;

public final class TextureLayers {
    public Map<String, String> layers;

    private TextureLayers(Map<String, String> layers) {
        this.layers = layers;
    }

    public static TextureLayers of(Map<String, String> layers) {
        return new TextureLayers(layers);
    }
}
