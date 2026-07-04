package me.noramibu.dynamictrim.runtime.tag;

import me.noramibu.dynamictrim.runtime.tag.adapter.TagInjectionAdapter;
import me.noramibu.dynamictrim.runtime.util.KeylessAdaptable;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.item.Item;

public class TrimTagInjector extends KeylessAdaptable<TagInjectionAdapter> {
    public Set<Item> getTrimmableArmour() {
        Set<Item> newEquipment = new HashSet<>();
        getAdapters().forEach(adapter -> newEquipment.addAll(adapter.getTrimmableArmour()));
        return newEquipment;
    }

    public Set<Item> getTrimMaterials() {
        Set<Item> newMaterials = new HashSet<>();
        getAdapters().forEach(adapter -> newMaterials.addAll(adapter.getTrimMaterials()));
        return newMaterials;
    }
}
