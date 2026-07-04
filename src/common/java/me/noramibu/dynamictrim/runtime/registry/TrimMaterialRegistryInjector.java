package me.noramibu.dynamictrim.runtime.registry;

import me.noramibu.dynamictrim.runtime.registry.adapter.TrimMaterialRegistryAdapter;
import me.noramibu.dynamictrim.runtime.util.KeylessAdaptable;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public class TrimMaterialRegistryInjector extends KeylessAdaptable<TrimMaterialRegistryAdapter> {
    public Map<ResourceLocation, Holder<Item>> getNewMaterials(MappedRegistry<TrimMaterial> trimMaterialRegistry) {
        Map<ResourceLocation, Holder<Item>> newMaterials = new HashMap<>();
        getAdapters().forEach(adapter -> newMaterials.putAll(adapter.getNewMaterials(trimMaterialRegistry)));
        return newMaterials;
    }
}
