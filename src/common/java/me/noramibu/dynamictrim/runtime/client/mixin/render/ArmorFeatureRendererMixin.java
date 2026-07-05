package me.noramibu.dynamictrim.runtime.client.mixin.render;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
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
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
            )
    )
    private void renderDynamicTrim(
            Model instance,
            PoseStack matrixStack,
            VertexConsumer vertexConsumer,
            int light,
            int overlay,
            Operation<Void> original,
            @Local(argsOnly = true) EquipmentClientInfo.LayerType layerType,
            @Local(argsOnly = true) ResourceKey<EquipmentAsset> equipmentAsset,
            @Local(argsOnly = true) ItemStack stack,
            @Local(argsOnly = true) MultiBufferSource vertexConsumers,
            @Local ArmorTrim trim,
            @Local TextureAtlasSprite sprite) {
        RuntimeTrimsClient.getTrimRenderer().setContext(stack.getItem());
        RuntimeTrimsClient.getTrimRenderer().renderTrim(
                trim,
                layerType,
                equipmentAsset,
                sprite,
                matrixStack,
                vertexConsumers,
                light,
                overlay,
                -1,
                dynamictrim$armorTrimAtlas,
                instance::renderToBuffer
        );
    }
}
