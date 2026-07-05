package me.noramibu.dynamictrim.client;

import com.mojang.serialization.MapCodec;
import me.noramibu.dynamictrim.DynamicTrim;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jetbrains.annotations.Nullable;

public record TrimPatternProperty()
        implements SelectItemModelProperty<Identifier> {
    public static final SelectItemModelProperty.Type<TrimPatternProperty, Identifier>
            TYPE = SelectItemModelProperty.Type.create(
                    MapCodec.unit(new TrimPatternProperty()),
                    Identifier.CODEC
            );

    @Nullable
    @Override
    public Identifier get(
            ItemStack stack,
            @Nullable ClientLevel level,
            @Nullable LivingEntity entity,
            int seed,
            ItemDisplayContext displayContext) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        return trim == null ? null : trim.pattern().value().assetId();
    }

    @Override
    public SelectItemModelProperty.Type<TrimPatternProperty, Identifier> type() {
        return TYPE;
    }

    @Override
    public com.mojang.serialization.Codec<Identifier> valueCodec() {
        return Identifier.CODEC;
    }

    public static String propertyId() {
        return DynamicTrim.TRIM_PATTERN.toString();
    }
}
