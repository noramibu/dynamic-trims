package me.noramibu.dynamictrim.runtime.client.model.item;

import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;

public record TrimmableResource(Item item, ResourceLocation resourceId, Resource resource) {
    public ResourceLocation modelId() {
        return ModelLocationUtils.getModelLocation(item);
    }
}
