package me.noramibu.dynamictrim.runtime.client.mixin.render;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EquipmentLayerRenderer.class)
public abstract class ArmorFeatureRendererMixin {
    @Unique
    private TextureAtlas dynamictrim$armorTrimAtlas;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void captureAtlas(EquipmentAssetManager equipmentAssets, TextureAtlas armorTrimAtlas, CallbackInfo ci) {
        dynamictrim$armorTrimAtlas = armorTrimAtlas;
    }

    @WrapOperation(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
                    ordinal = 2
            )
    )
    private <S> void renderDynamicTrim(
            OrderedSubmitNodeCollector collector,
            Model<? super S> model,
            S state,
            PoseStack matrixStack,
            RenderType renderType,
            int light,
            int overlay,
            int colour,
            TextureAtlasSprite sprite,
            int outlineColour,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            Operation<Void> original,
            @Local(argsOnly = true) EquipmentClientInfo.LayerType layerType,
            @Local(argsOnly = true) ResourceKey<EquipmentAsset> equipmentAsset,
            @Local(argsOnly = true) ItemStack stack,
            @Local ArmorTrim trim) {
        RuntimeTrimsClient.getTrimRenderer().setContext(stack.getItem());
        RuntimeTrimsClient.getTrimRenderer().submitTrim(
                trim,
                layerType,
                equipmentAsset,
                sprite,
                model,
                state,
                matrixStack,
                collector,
                light,
                overlay,
                colour,
                outlineColour,
                dynamictrim$armorTrimAtlas,
                crumblingOverlay
        );
    }
}
