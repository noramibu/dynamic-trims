package me.noramibu.dynamictrim.runtime.client.model.item.adapter;

import me.noramibu.dynamictrim.runtime.client.model.item.JsonParser;
import me.noramibu.dynamictrim.runtime.client.model.item.TrimmableResource;
import me.noramibu.dynamictrim.runtime.client.model.item.json.TrimmableItemModel;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public abstract class TrimModelLoaderAdapter {
    /**
     * Whether dynamic trims should be generated for the item
     */
    public abstract boolean canTrim(Item item);

    /**
     * How many colours does the blank trim texture for the item use.
     */
    public abstract Integer getLayerCount(Item item);

    /**
     * Where to find the <b>dynamic</b> trim texture for a given item and index.
     */
    public abstract String getLayerName(Item item, int layerIndex);

    /**
     * Add, modify or remove existing overrides
     */
    public abstract Map<Identifier, TrimmableItemModel> supplyOverrides(JsonParser jsonParser, TrimmableItemModel itemModel, TrimmableResource resource, BiFunction<TrimmableItemModel, TrimmableResource, TrimmableItemModel> overrideCreator);
}
