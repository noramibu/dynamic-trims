package me.noramibu.dynamictrim.runtime.client.mixin;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TrimMaterial.class)
public abstract class ArmorTrimMaterialMixin {
    @Shadow @Final private Holder<Item> ingredient;

    @ModifyReturnValue(
            method = "description",
            at = @At("RETURN")
    )
    private Component colouriseDescription(Component original) {
        if(!(original instanceof MutableComponent mutableText)) return original;

        Style style = mutableText.getStyle();
        if(style == null) style = Style.EMPTY;

        TextColor colour = style.getColor();
        if(colour != null) return original;

        TrimPalette palette = RuntimeTrimsClient.getTrimPalettes().getPalette(ingredient.value());
        if(palette == null) return original;

        return mutableText.withStyle(s -> s.withColor(palette.getAverageColour()));
    }
}
