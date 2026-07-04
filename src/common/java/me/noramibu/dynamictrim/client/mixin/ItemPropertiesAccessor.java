package me.noramibu.dynamictrim.client.mixin;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemProperties.class)
public interface ItemPropertiesAccessor {
    @Invoker("registerGeneric")
    static ClampedItemPropertyFunction dynamictrim$registerGeneric(
            ResourceLocation id, ClampedItemPropertyFunction propertyFunction) {
        throw new AssertionError();
    }
}
