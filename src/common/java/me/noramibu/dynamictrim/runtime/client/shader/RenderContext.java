package me.noramibu.dynamictrim.runtime.client.shader;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;

public record RenderContext(Entity entity, Item trimmed) {
}