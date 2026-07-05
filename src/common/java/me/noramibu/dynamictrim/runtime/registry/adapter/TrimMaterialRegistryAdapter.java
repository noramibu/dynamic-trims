package me.noramibu.dynamictrim.runtime.registry.adapter;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public abstract class TrimMaterialRegistryAdapter {
    public abstract Map<Identifier, Holder<Item>> getNewMaterials(MappedRegistry<TrimMaterial> trimMaterialRegistry);
}
