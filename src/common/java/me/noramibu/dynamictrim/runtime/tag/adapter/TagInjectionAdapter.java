package me.noramibu.dynamictrim.runtime.tag.adapter;

import java.util.Set;
import net.minecraft.world.item.Item;

public abstract class TagInjectionAdapter {
    public abstract Set<Item> getTrimmableArmour();
    public abstract Set<Item> getTrimMaterials();
}
