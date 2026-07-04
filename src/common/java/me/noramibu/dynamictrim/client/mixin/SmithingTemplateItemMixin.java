package me.noramibu.dynamictrim.client.mixin;

import me.noramibu.dynamictrim.client.extend.SmithingTemplateItemExtender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.armortrim.TrimPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingTemplateItem.class)
public abstract class SmithingTemplateItemMixin extends Item implements SmithingTemplateItemExtender {
    @Unique
    private static final ThreadLocal<ResourceLocation> runtimetrims$ASSET_ID_CAPTURE = new ThreadLocal<>();

    @Unique
    private ResourceLocation runtimetrims$assetId;

    protected SmithingTemplateItemMixin(Properties properties) {
        super(properties);
    }

    @ModifyVariable(
            method = "createArmorTrimTemplate(Lnet/minecraft/resources/ResourceKey;[Lnet/minecraft/world/flag/FeatureFlag;)Lnet/minecraft/world/item/SmithingTemplateItem;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static ResourceKey<TrimPattern> capturePatternKey(ResourceKey<TrimPattern> value) {
        runtimetrims$ASSET_ID_CAPTURE.set(value.location());
        return value;
    }

    @ModifyVariable(
            method = "createArmorTrimTemplate(Lnet/minecraft/resources/ResourceLocation;[Lnet/minecraft/world/flag/FeatureFlag;)Lnet/minecraft/world/item/SmithingTemplateItem;",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static ResourceLocation capturePatternLocation(ResourceLocation value) {
        runtimetrims$ASSET_ID_CAPTURE.set(value);
        return value;
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void onInit(CallbackInfo ci) {
        runtimetrims$assetId = runtimetrims$ASSET_ID_CAPTURE.get();
        runtimetrims$ASSET_ID_CAPTURE.remove();
    }

    @Override
    public ResourceLocation runtimetrims$getPatternAssetId() {
        return runtimetrims$assetId;
    }
}
