package me.noramibu.dynamictrim.runtime.client.render;

import me.noramibu.dynamictrim.DynamicTrim;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import java.util.List;

public final class TrimRenderer {
    public boolean useLegacyRenderer(TextureAtlasSprite sprite) {
        return sprite.contents().name().getPath().endsWith("_%s".formatted(DynamicTrim.DYNAMIC));
    }

    public <S> void submitTrim(ArmorTrim trim, TextureAtlasSprite sprite, Model<? super S> model, S state, PoseStack matrices, OrderedSubmitNodeCollector collector, int light, int overlay, int outlineColour, TextureAtlas atlasTexture, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        Identifier modelId = sprite.contents().name();
        submitLegacyTrim(trim, model, state, matrices, collector, light, overlay, modelId, atlasTexture, outlineColour, crumblingOverlay);
    }

    private <S> void submitLegacyTrim(ArmorTrim trim, Model<? super S> model, S state, PoseStack matrices, OrderedSubmitNodeCollector collector, int light, int overlay, Identifier modelId, TextureAtlas atlasTexture, int outlineColour, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if(modelId.equals(MissingTextureAtlasSprite.getLocation())) return;

        TrimMaterial trimMaterial = trim.material().value();
        Identifier patternId = modelId.withPath(path -> "textures/%s.png".formatted(path.substring(0, path.lastIndexOf("_"))));
        int maxSupportedLayer = RuntimeTrimsClient.getLayerData().getMaxSupportedLayer(patternId);

        TrimPalette trimPalette = RuntimeTrimsClient.getTrimPalettes().getOrGeneratePalette(getTrimItem(trim));
        List<Integer> paletteColours = Lists.reverse(trimPalette.getColours().subList(0, maxSupportedLayer));
        String assetName = getAssetName(trimMaterial);
        RenderType renderLayer = Sheets.armorTrimsSheet(trim.pattern().value().decal());
        for (int i = 0; i < maxSupportedLayer; i++) {
            Identifier layerSpriteId = modelId.withPath(modelId.getPath().replace(assetName, "%d_%s".formatted(i, assetName)));
            TextureAtlasSprite layerSprite = atlasTexture.getSprite(layerSpriteId);
            int colour = paletteColours.get(i) | 0xFF000000;
            collector.submitModel(
                    model,
                    state,
                    matrices,
                    renderLayer,
                    light,
                    overlay,
                    colour,
                    layerSprite,
                    outlineColour,
                    crumblingOverlay
            );
        }
    }

    private String getAssetName(TrimMaterial trimMaterial) {
        return trimMaterial.assets().base().suffix();
    }

    private Item getTrimItem(ArmorTrim trim) {
        return trim.material()
                .unwrapKey()
                .flatMap(key -> BuiltInRegistries.ITEM.get(key.identifier()).map(Holder::value))
                .orElse(Items.AIR);
    }
}
