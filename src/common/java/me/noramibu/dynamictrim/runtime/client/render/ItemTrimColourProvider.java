package me.noramibu.dynamictrim.runtime.client.render;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.colour.ARGBColourHelper;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalettes;
import java.util.Map;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;


public final class ItemTrimColourProvider implements ItemColor {
    private final TrimPalettes palettes;
    private final LayerData layerData;
    private final Object existingProviders;

    public ItemTrimColourProvider(TrimPalettes palettes, LayerData layerData, Object existingProviders) {
        this.palettes = palettes;
        this.layerData = layerData;
        this.existingProviders = existingProviders;
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        ArmorTrim trim = stack.getComponents().get(DataComponents.TRIM);
        if(trim == null) return getExistingColor(stack, tintIndex);

        TrimMaterial material = trim.material().value();
        if(!(material.assetName().equals(RuntimeTrims.DYNAMIC) || RuntimeTrimsClient.overrideExisting)) return -1;

        int startLayer = layerData.getTrimStartLayer(stack.getItem());
        if(tintIndex < startLayer) return getExistingColor(stack, tintIndex);

        Item materialItem = material.ingredient().value();
        TrimPalette palette = palettes.getOrGeneratePalette(materialItem);

        return ARGBColourHelper.fullAlpha(palette.getColours().get(tintIndex - startLayer));
    }

    private int getExistingColor(ItemStack stack, int tintIndex) {
        ItemColor existingProvider = getExistingProvider(stack.getItem());
        if (existingProvider == null)
            return -1;

        return existingProvider.getColor(stack, tintIndex);
    }

    @SuppressWarnings("unchecked")
    private ItemColor getExistingProvider(Item item) {
        if (existingProviders instanceof IdMapper<?> providers) {
            return (ItemColor) providers.byId(BuiltInRegistries.ITEM.getId(item));
        }
        if (existingProviders instanceof Map<?, ?> providers) {
            return ((Map<Item, ItemColor>) providers).get(item);
        }
        return null;
    }

    public Item[] getApplicableItems() {
        return BuiltInRegistries.ITEM.stream().filter(item -> {
            if(item instanceof Equipable equipment) {
                if(item instanceof AnimalArmorItem) return false;
                return equipment.getEquipmentSlot().isArmor();
            }
            return false;
        }).toArray(Item[]::new);
    }
}
