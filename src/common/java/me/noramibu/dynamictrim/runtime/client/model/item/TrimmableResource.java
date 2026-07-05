package me.noramibu.dynamictrim.runtime.client.model.item;

import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;

public record TrimmableResource(Item item, Identifier resourceId, Resource resource) {
    public Identifier modelId() {
        return ModelLocationUtils.getModelLocation(item);
    }
}
