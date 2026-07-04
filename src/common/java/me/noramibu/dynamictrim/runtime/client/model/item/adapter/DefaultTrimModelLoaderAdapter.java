package me.noramibu.dynamictrim.runtime.client.model.item.adapter;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.model.item.JsonParser;
import me.noramibu.dynamictrim.runtime.client.model.item.TrimModelPredicate;
import me.noramibu.dynamictrim.runtime.client.model.item.TrimmableResource;
import me.noramibu.dynamictrim.runtime.client.model.item.json.ModelOverride;
import me.noramibu.dynamictrim.runtime.client.model.item.json.TrimmableItemModel;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;

public class DefaultTrimModelLoaderAdapter extends TrimModelLoaderAdapter {
    @Override
    public boolean canTrim(Item item) {
        return item instanceof Equipable equipment && !(equipment instanceof AnimalArmorItem || equipment instanceof ElytraItem) && equipment.getEquipmentSlot().isArmor();
    }

    @Override
    public Integer getLayerCount(Item item) {
        return 4;
    }

    @Override
    public String getLayerName(Item item, int layerIndex) {
        return "minecraft:trims/items/%s_trim_%d_%s".formatted(
                getEquipmentType((Equipable) item),
                layerIndex,
                RuntimeTrims.DYNAMIC
        );
    }

    @Override
    public Map<ResourceLocation, TrimmableItemModel> supplyOverrides(JsonParser jsonParser, TrimmableItemModel itemModel, TrimmableResource resource, BiFunction<TrimmableItemModel, TrimmableResource, TrimmableItemModel> overrideCreator) {
        ResourceLocation modelId = resource.modelId().withSuffix("_%s_trim".formatted(RuntimeTrims.DYNAMIC));

        if(RuntimeTrimsClient.overrideExisting) {
            itemModel.overrides.forEach(modelOverride -> modelOverride.model = modelId.toString());
        }

        itemModel.addOverride(ModelOverride.builder()
                .withModel(modelId.toString())
                .withPredicate(jsonParser.toJsonObject(TrimModelPredicate.of(RuntimeTrims.MATERIAL_MODEL_INDEX)))
                .build());

        return Map.of(modelId, overrideCreator.apply(itemModel, resource));
    }

    protected String getEquipmentType(Equipable equipment) {
        return switch (equipment.getEquipmentSlot()) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> null;
        };
    }
}
