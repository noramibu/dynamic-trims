package me.noramibu.dynamictrim.runtime.client.mixin.render;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    @Shadow @Final private TextureAtlas armorTrimAtlas;

    public ArmorFeatureRendererMixin(RenderLayerParent<T, M> context) {
        super(context);
    }

    @Inject(
            //? if >1.21 && neoforge {
            method = "renderArmorPiece",
            //?} else {
            /*method = "renderArmor",
            *///?}
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V"
            )
    )
    private void captureContext(CallbackInfo ci, @Local(argsOnly = true) T entity, @Local ArmorItem trimmed) {
        RuntimeTrimsClient.getTrimRenderer().setContext(entity, trimmed);
    }

    @ModifyExpressionValue(
            method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;getSprite(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
            )
    )
    private TextureAtlasSprite captureSprite(TextureAtlasSprite original, @Share("sprite") LocalRef<TextureAtlasSprite> spriteLocalRef) {
        spriteLocalRef.set(original);
        return original;
    }

    @WrapOperation(
            method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/HumanoidModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
            )
    )
    private void renderDynamicTrim(HumanoidModel<T> instance, PoseStack matrixStack, VertexConsumer vertexConsumer, int light, int uv, Operation<Void> original,
            @Local(argsOnly = true) ArmorTrim trim,
            @Local(argsOnly = true) MultiBufferSource vertexConsumers,
            @Local(argsOnly = true) /*$ armour_material >>*/ Holder<ArmorMaterial> armourMaterial,
            @Local(argsOnly = true) boolean leggings,
            @Share("sprite") LocalRef<TextureAtlasSprite> spriteLocalRef) {
        RuntimeTrimsClient.getTrimRenderer().renderTrim(
                trim,
                armourMaterial,
                leggings,
                spriteLocalRef.get(),
                matrixStack,
                vertexConsumers,
                light,
                uv,
                -1,
                armorTrimAtlas,
                instance::renderToBuffer
        );
    }
}
