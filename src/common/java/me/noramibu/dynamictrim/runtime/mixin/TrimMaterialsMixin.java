package me.noramibu.dynamictrim.runtime.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.Optional;
import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TrimMaterials.class)
public abstract class TrimMaterialsMixin {
    @ModifyReturnValue(
            method = "getFromIngredient",
            at = @At("RETURN")
    )
    private static Optional<Holder<TrimMaterial>> getDynamicTrimMaterial(
            Optional<Holder<TrimMaterial>> original,
            HolderLookup.Provider registries,
            ItemStack ingredient) {
        if (original.isPresent()) {
            return original;
        }

        Item item = ingredient.getItem();
        if (!RuntimeTrims.getTrimTagInjector().getTrimMaterials().contains(item)) {
            return original;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        ResourceKey<TrimMaterial> materialKey = ResourceKey.create(Registries.TRIM_MATERIAL, itemId);
        return registries.lookupOrThrow(Registries.TRIM_MATERIAL)
                .get(materialKey)
                .map(reference -> reference);
    }
}
