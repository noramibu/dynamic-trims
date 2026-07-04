package me.noramibu.dynamictrim.runtime.util;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public abstract class ItemAdaptable<T> extends DefaultedAdaptable<ResourceLocation, T> {
    public void registerAdapter(T adapter, List<Item> items) {
        registerAdapter(adapter, items.stream().map(BuiltInRegistries.ITEM::getKey).collect(Collectors.toSet()));
    }

    protected boolean hasAdapter(Item item) {
        return hasAdapter(BuiltInRegistries.ITEM.getKey(item));
    }

    protected T getAdapter(Item item) {
        return getAdapter(BuiltInRegistries.ITEM.getKey(item));
    }
}
