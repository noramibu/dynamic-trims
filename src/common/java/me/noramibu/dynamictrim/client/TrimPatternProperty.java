package me.noramibu.dynamictrim.client;

import com.mojang.serialization.MapCodec;
import me.noramibu.dynamictrim.DynamicTrim;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.jetbrains.annotations.Nullable;

public record TrimPatternProperty()
        implements SelectItemModelProperty<ResourceKey<TrimPattern>> {
    public static final SelectItemModelProperty.Type<TrimPatternProperty, ResourceKey<TrimPattern>>
            TYPE = SelectItemModelProperty.Type.create(
                    MapCodec.unit(new TrimPatternProperty()),
                    ResourceKey.codec(Registries.TRIM_PATTERN)
            );

    @Nullable
    @Override
    public ResourceKey<TrimPattern> get(
            ItemStack stack,
            @Nullable ClientLevel level,
            @Nullable LivingEntity entity,
            int seed,
            ItemDisplayContext displayContext) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        return trim == null ? null : trim.pattern().unwrapKey().orElse(null);
    }

    @Override
    public SelectItemModelProperty.Type<TrimPatternProperty, ResourceKey<TrimPattern>> type() {
        return TYPE;
    }

    public static String propertyId() {
        return DynamicTrim.TRIM_PATTERN.toString();
    }
}
