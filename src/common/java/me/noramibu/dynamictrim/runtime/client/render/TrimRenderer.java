package me.noramibu.dynamictrim.runtime.client.render;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.colour.ARGBColourHelper;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import me.noramibu.dynamictrim.runtime.client.render.adapter.TrimRendererAdapter;
import me.noramibu.dynamictrim.runtime.client.shader.RenderContext;
import me.noramibu.dynamictrim.runtime.util.ItemAdaptable;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import java.util.List;

public final class TrimRenderer extends ItemAdaptable<TrimRendererAdapter> {
    private RenderContext context;

    public void setContext(Entity entity, Item trimmed) {
        context = new RenderContext(entity, trimmed);
    }

    public void setContext(Item trimmed) {
        context = new RenderContext(null, trimmed);
    }

    public RenderContext getContext() {
        return context;
    }

    public boolean isSpriteDynamic(TextureAtlasSprite sprite) {
        return sprite.contents().name().getPath().endsWith("_%s".formatted(RuntimeTrims.DYNAMIC));
    }

    /**
     * Always used to render overrides and when a shader is enabled.
     * @apiNote User can force the legacy renderer to be used.
     */
    public boolean useLegacyRenderer(TextureAtlasSprite sprite) {
        return isSpriteDynamic(sprite) || RuntimeTrimsClient.overrideExisting;
    }

    public RenderType getLegacyRenderLayer(Item trimmed, ArmorTrim trim) {
        return getAdapter(trimmed).getLegacyRenderLayer(trim);
    }

    public int getTrimAlpha(RenderContext context) {
        return getAdapter(context.trimmed()).getAlpha(context);
    }

    public <S> void submitTrim(ArmorTrim trim, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> equipmentAsset, TextureAtlasSprite sprite, Model<? super S> model, S state, PoseStack matrices, OrderedSubmitNodeCollector collector, int light, int overlay, int colour, int outlineColour, TextureAtlas atlasTexture, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if(context == null) {
            throw new IllegalStateException("Trim shader context not available");
        }

        RenderContext renderContext = context;
        ResourceLocation modelId = RuntimeTrimsClient.overrideExisting ? getOverridenId(trim, layerType, equipmentAsset) : sprite.contents().name();
        colour = ARGBColourHelper.withAlpha(colour, getTrimAlpha(renderContext));

        if (useLegacyRenderer(sprite)) {
            submitLegacyTrim(renderContext, trim, model, state, matrices, collector, light, overlay, modelId, atlasTexture, outlineColour, crumblingOverlay);
        } else {
            collector.submitModel(model, state, matrices, getLegacyRenderLayer(renderContext.trimmed(), trim), light, overlay, colour, sprite, outlineColour, crumblingOverlay);
        }
    }

    private <S> void submitLegacyTrim(RenderContext context, ArmorTrim trim, Model<? super S> model, S state, PoseStack matrices, OrderedSubmitNodeCollector collector, int light, int overlay, ResourceLocation modelId, TextureAtlas atlasTexture, int outlineColour, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if(modelId.equals(MissingTextureAtlasSprite.getLocation())) return;

        RenderType renderLayer = getLegacyRenderLayer(context.trimmed(), trim);
        forEachLegacyTrimLayer(context, trim, modelId, atlasTexture, (layerSprite, colour) ->
                collector.submitModel(model, state, matrices, renderLayer, light, overlay, colour, layerSprite, outlineColour, crumblingOverlay)
        );
    }

    private void forEachLegacyTrimLayer(RenderContext context, ArmorTrim trim, ResourceLocation modelId, TextureAtlas atlasTexture, LegacyTrimLayerConsumer consumer) {
        TrimMaterial trimMaterial = trim.material().value();
        ResourceLocation patternId = modelId.withPath(path -> "textures/%s.png".formatted(path.substring(0, path.lastIndexOf("_"))));
        int maxSupportedLayer = RuntimeTrimsClient.getLayerData().getMaxSupportedLayer(patternId);

        TrimPalette trimPalette = RuntimeTrimsClient.getTrimPalettes().getOrGeneratePalette(getTrimItem(trim));
        List<Integer> paletteColours = Lists.reverse(trimPalette.getColours().subList(0, maxSupportedLayer));
        String assetName = getAssetName(trimMaterial);
        int alpha = getTrimAlpha(context);
        for (int i = 0; i < maxSupportedLayer; i++) {
            ResourceLocation layerSpriteId = modelId.withPath(modelId.getPath().replace(assetName, "%d_%s".formatted(i, assetName)));
            TextureAtlasSprite layerSprite = atlasTexture.getSprite(layerSpriteId);
            int colour = ARGBColourHelper.withAlpha(paletteColours.get(i), alpha);
            consumer.accept(layerSprite, colour);
        }
    }

    @FunctionalInterface
    private interface LegacyTrimLayerConsumer {
        void accept(TextureAtlasSprite sprite, int colour);
    }

    public String getAssetName(TrimMaterial trimMaterial) {
        return RuntimeTrimsClient.overrideExisting ? RuntimeTrims.DYNAMIC : trimMaterial.assets().base().suffix();
    }

    public ResourceLocation getOverridenId(ArmorTrim trim, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> equipmentAsset) {
        String assetId = trim.material().value().assets().assetId(equipmentAsset).suffix();
        return getModelId(trim, layerType, equipmentAsset).withPath(path -> path.replace(assetId, RuntimeTrims.DYNAMIC));
    }

    public ResourceLocation getModelId(ArmorTrim trim, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> equipmentAsset) {
        return trim.layerAssetId(layerType.trimAssetPrefix(), equipmentAsset);
    }

    private Item getTrimItem(ArmorTrim trim) {
        return trim.material()
                .unwrapKey()
                .flatMap(key -> BuiltInRegistries.ITEM.get(key.location()).map(Holder::value))
                .orElse(Items.AIR);
    }
}
