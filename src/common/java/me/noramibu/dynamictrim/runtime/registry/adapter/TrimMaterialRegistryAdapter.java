package me.noramibu.dynamictrim.runtime.registry.adapter;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;

public abstract class TrimMaterialRegistryAdapter {
    public abstract Map<ResourceLocation, Holder<Item>> getNewMaterials(MappedRegistry<TrimMaterial> trimMaterialRegistry);
}
