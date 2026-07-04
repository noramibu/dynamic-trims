package me.noramibu.dynamictrim.runtime.client.mixin.render;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.mixin.accessor.ItemColorsAccessor;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalettes;
import me.noramibu.dynamictrim.runtime.client.render.ItemTrimColourProvider;
import me.noramibu.dynamictrim.runtime.client.render.LayerData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.HashMap;
import java.util.Map;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Shadow public ClientLevel level;

    @ModifyExpressionValue(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/color/item/ItemColors;createDefault(Lnet/minecraft/client/color/block/BlockColors;)Lnet/minecraft/client/color/item/ItemColors;"
            )
    )
    private ItemColors registerTrimColourProvider(ItemColors original) {
        TrimPalettes trimPalettes = RuntimeTrimsClient.getTrimPalettes();
        LayerData layerData = RuntimeTrimsClient.getLayerData();
        Object providers = copyProviders(((ItemColorsAccessor) original).getItemColors());
        ItemTrimColourProvider colourRenderer = new ItemTrimColourProvider(trimPalettes, layerData, providers);
        original.register(colourRenderer, colourRenderer.getApplicableItems());
        return original;
    }

    @SuppressWarnings("unchecked")
    private Object copyProviders(Object originalProviders) {
        if (originalProviders instanceof IdMapper<?> existingProviders) {
            IdMapper<ItemColor> providers = new IdMapper<>();
            BuiltInRegistries.ITEM.stream()
                    .forEach(item -> {
                        int rawId = BuiltInRegistries.ITEM.getId(item);
                        ItemColor existingProvider = (ItemColor) existingProviders.byId(rawId);
                        if (existingProvider != null) {
                            providers.addMapping(existingProvider, rawId);
                        }
                    });
            return providers;
        }

        if (originalProviders instanceof Map<?, ?> existingProviders) {
            return new HashMap<>((Map<Item, ItemColor>) existingProviders);
        }

        return originalProviders;
    }

    @Inject(
            method = "runTick",
            at = @At("TAIL")
    )
    private void cycleAnimatedTrims(boolean renderLevel, CallbackInfo ci) {
        if(RuntimeTrimsClient.animate && level != null) {
            RuntimeTrimsClient.getTrimPalettes().forEach(TrimPalette::cycleAnimatedColours);
        }
    }
}
