package me.noramibu.dynamictrim.runtime.client.model.item.adapter;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.model.item.JsonParser;
import me.noramibu.dynamictrim.runtime.client.model.item.TrimmableResource;
import me.noramibu.dynamictrim.runtime.client.model.item.json.TrimmableItemModel;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;

public class DefaultTrimModelLoaderAdapter extends TrimModelLoaderAdapter {
    @Override
    public boolean canTrim(Item item) {
        Equippable equipment = getEquippable(item);
        return equipment != null
                && item != Items.ELYTRA
                && equipment.slot().isArmor();
    }

    @Override
    public Integer getLayerCount(Item item) {
        return 4;
    }

    @Override
    public String getLayerName(Item item, int layerIndex) {
        return "minecraft:trims/items/%s_trim_%d_%s".formatted(
                getEquipmentType(item),
                layerIndex,
                RuntimeTrims.DYNAMIC
        );
    }

    @Override
    public Map<ResourceLocation, TrimmableItemModel> supplyOverrides(JsonParser jsonParser, TrimmableItemModel itemModel, TrimmableResource resource, BiFunction<TrimmableItemModel, TrimmableResource, TrimmableItemModel> overrideCreator) {
        return Map.of();
    }

    protected Equippable getEquippable(Item item) {
        return item.components().get(DataComponents.EQUIPPABLE);
    }

    protected String getEquipmentType(Item item) {
        Equippable equipment = getEquippable(item);
        return equipment == null ? null : getEquipmentType(equipment);
    }

    protected String getEquipmentType(Equippable equipment) {
        return switch (equipment.slot()) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> null;
        };
    }
}
