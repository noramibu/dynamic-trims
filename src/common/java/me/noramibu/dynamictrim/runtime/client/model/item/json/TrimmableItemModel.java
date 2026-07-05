package me.noramibu.dynamictrim.runtime.client.model.item.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class TrimmableItemModel {
    public String parent;
    public TextureLayers textures;

    public String credit;
    public JsonArray textureSize;
    public JsonObject display;
    public String guiLight;
    public JsonArray elements;

    public TrimmableItemModel copy() {
        TrimmableItemModel newModel = new TrimmableItemModel();
        newModel.parent = parent;
        newModel.textures = textures;
        newModel.credit = credit;
        newModel.textureSize = textureSize;
        newModel.display = display;
        newModel.guiLight = guiLight;
        newModel.elements = elements;
        return newModel;
    }
}
