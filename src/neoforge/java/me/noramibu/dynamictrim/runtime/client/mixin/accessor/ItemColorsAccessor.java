package me.noramibu.dynamictrim.runtime.client.mixin.accessor;

import java.util.Map;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemColors.class)
public interface ItemColorsAccessor {
    @Accessor("itemColors")
    Map<Item, ItemColor> getItemColors();
}
