package me.noramibu.dynamictrim.runtime.client.mixin.accessor;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.IdMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemColors.class)
public interface ItemColorsAccessor {
    @Accessor("itemColors")
    IdMapper<ItemColor> getItemColors();
}
