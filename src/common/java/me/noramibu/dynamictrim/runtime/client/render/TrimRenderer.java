package me.noramibu.dynamictrim.runtime.client.render;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.colour.ARGBColourHelper;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import me.noramibu.dynamictrim.runtime.client.render.adapter.TrimRendererAdapter;
import me.noramibu.dynamictrim.runtime.client.shader.RenderContext;
import me.noramibu.dynamictrim.runtime.util.ItemAdaptable;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import java.util.List;
import java.util.Map;

public final class TrimRenderer extends ItemAdaptable<TrimRendererAdapter> {
    private RenderContext context;

    public void setContext(Entity entity, Item trimmed) {
        context = new RenderContext(entity, trimmed);
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
        boolean useLegacyRenderer = RuntimeTrimsClient.useLegacyRenderer && isSpriteDynamic(sprite); // specified to use it
        useLegacyRenderer |= RuntimeTrimsClient.overrideExisting && !isSpriteDynamic(sprite); // overriding existing
        return useLegacyRenderer;
    }

    public RenderType getLegacyRenderLayer(Item trimmed, ArmorTrim trim) {
        return getAdapter(trimmed).getLegacyRenderLayer(trim);
    }

    public int getTrimAlpha(RenderContext context) {
        return getAdapter(context.trimmed()).getAlpha(context);
    }

    /**
     * Uses default render layer<br>
     * Uses default model id
     * @see #renderTrim(ArmorTrim, TextureAtlasSprite, PoseStack, MultiBufferSource, int, int, int, ResourceLocation, TextureAtlas, RenderType, RenderCallback)
     */
    public void renderTrim(ArmorTrim trim, TextureAtlasSprite sprite, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, int colour, TextureAtlas atlasTexture, RenderCallback callback) {
        renderTrim(trim, sprite, matrices, vertexConsumers, light, overlay, colour, atlasTexture, null, callback);
    }

    /**
     * Uses default render layer
     * @see #renderTrim(ArmorTrim, TextureAtlasSprite, PoseStack, MultiBufferSource, int, int, int, ResourceLocation, TextureAtlas, RenderType, RenderCallback)
     */
    public void renderTrim(ArmorTrim trim, TextureAtlasSprite sprite, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, int colour, ResourceLocation modelId, TextureAtlas atlasTexture, RenderCallback callback) {
        renderTrim(trim, sprite, matrices, vertexConsumers, light, overlay, colour, modelId, atlasTexture, null, callback);
    }

    /**
     * Uses default model id
     * @see #renderTrim(ArmorTrim, TextureAtlasSprite, PoseStack, MultiBufferSource, int, int, int, ResourceLocation, TextureAtlas, RenderType, RenderCallback)
     */
    public void renderTrim(ArmorTrim trim, TextureAtlasSprite sprite, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, int colour, TextureAtlas atlasTexture, RenderType renderLayer, RenderCallback callback) {
        renderTrim(trim, sprite, matrices, vertexConsumers, light, overlay, colour, getModelId(sprite), atlasTexture, renderLayer, callback);
    }

    /**
     * Handles overriding automatically
     * @see #renderTrim(ArmorTrim, TextureAtlasSprite, PoseStack, MultiBufferSource, int, int, int, ResourceLocation, TextureAtlas, RenderType, RenderCallback)
     */
    public void renderTrim(ArmorTrim trim, Holder<ArmorMaterial> armourMaterial, boolean leggings, TextureAtlasSprite sprite, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light, int overlay, int colour, TextureAtlas atlasTexture, RenderCallback callback) {
        if (RuntimeTrimsClient.overrideExisting) {
            renderTrim(trim, sprite, matrixStack, vertexConsumers, light, overlay, colour, getOverridenId(trim, armourMaterial, leggings), atlasTexture, callback);
        } else {
            renderTrim(trim, sprite, matrixStack, vertexConsumers, light, overlay, colour, atlasTexture, callback);
        }
    }

    /**
     * Calculates how to render a trim then passes it to the {@link RenderCallback}
     * <br>
     * Types of rendering:
     * <ul>
     *  <li><b>Legacy Rendering</b>: Splits the pattern texture into layers and renders each layer of separately with the colour of that layer derived from the trim palette</li>
     *  <li><b>Shader Rendering</b>: Uses a core shader to render the trim. ~8x more performant than Legacy Rendering</li>
     * </ul>
     *
     * @param sprite          The sprite of the trim pattern, if overriding existing is enabled, it will be ignored.
     * @param vertexConsumers Providers for the Legacy and Shader renderers
     * @param modelId         Optionally provide the model to use
     * @param atlasTexture    The atlas to pull the trim texture layers from. Used by Legacy
     * @param renderLayer     Optionally provide a render layer, otherwise the default will be used.
     * @param callback        The renderer. Typically {@link Model#renderToBuffer(PoseStack, VertexConsumer, int, int, int)}. But can be a {@link Operation} if the call is wrapped.
     */
    public void renderTrim(ArmorTrim trim, TextureAtlasSprite sprite, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, int colour, ResourceLocation modelId, TextureAtlas atlasTexture, RenderType renderLayer, RenderCallback callback) {
        if(context == null) {
            throw new IllegalStateException("Trim shader context not available");
        }

        colour = ARGBColourHelper.withAlpha(colour, getTrimAlpha(context));

        if (useLegacyRenderer(sprite)) {
            if(!(isSpriteDynamic(sprite) || RuntimeTrimsClient.overrideExisting)) {
                callback.render(matrices, sprite.wrap(vertexConsumers.getBuffer(getLegacyRenderLayer(context.trimmed(), trim))), light, overlay, colour);
            } else {
                legacyRenderTrim(context, trim, matrices, vertexConsumers, light, overlay, modelId, atlasTexture, renderLayer, callback);
            }
        } else if (isSpriteDynamic(sprite)) {
            shaderRenderTrim(context, trim, sprite, matrices, vertexConsumers, light, overlay, colour, renderLayer, callback);
        } else {
            callback.render(matrices, sprite.wrap(vertexConsumers.getBuffer(getLegacyRenderLayer(context.trimmed(), trim))), light, overlay, colour);
        }
    }

    public void shaderRenderTrim(RenderContext context, ArmorTrim trim, TextureAtlasSprite sprite, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, int colour, RenderType renderLayer, RenderCallback callback) {
        Item material = trim.material().value().ingredient().value();
        Item trimmed = context.trimmed();
        TrimPalette palette = RuntimeTrimsClient.getTrimPalettes().getOrGeneratePalette(material);
        if(renderLayer == null) {
            renderLayer = RuntimeTrimsClient.getShaderManager().getTrimRenderLayer(trimmed, palette);
        }
        VertexConsumer vertices = sprite.wrap(vertexConsumers.getBuffer(renderLayer));
        getAdapter(trimmed).render(context, matrices, vertices, light, overlay, colour, callback);
    }

    public void legacyRenderTrim(RenderContext context, ArmorTrim trim, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, ResourceLocation modelId, TextureAtlas atlasTexture, RenderType renderLayer, RenderCallback callback) {
        if(modelId.equals(MissingTextureAtlasSprite.getLocation())) return;

        TrimMaterial trimMaterial = trim.material().value();
        Item trimItem = trimMaterial.ingredient().value();
        Item trimmed = context.trimmed();

        if(renderLayer == null) {
            renderLayer = getLegacyRenderLayer(trimmed, trim);
        }

        ResourceLocation patternId = modelId.withPath(path -> "textures/%s.png".formatted(path.substring(0, path.lastIndexOf("_"))));
        int maxSupportedLayer = RuntimeTrimsClient.getLayerData().getMaxSupportedLayer(patternId);

        TrimPalette trimPalette = RuntimeTrimsClient.getTrimPalettes().getOrGeneratePalette(trimItem);
        List<Integer> paletteColours = Lists.reverse(trimPalette.getColours().subList(0, maxSupportedLayer));
        String assetName = getAssetName(trimMaterial);
        TrimRendererAdapter adapter = getAdapter(trimmed);
        int alpha = getTrimAlpha(context);
        for (int i = 0; i < maxSupportedLayer; i++) {
            ResourceLocation layerSpriteId = modelId.withPath(modelId.getPath().replace(assetName, "%d_%s".formatted(i, assetName)));
            TextureAtlasSprite layerSprite = atlasTexture.getSprite(layerSpriteId);
            VertexConsumer vertexConsumer = layerSprite.wrap(vertexConsumers.getBuffer(renderLayer));
            int colour = ARGBColourHelper.withAlpha(paletteColours.get(i), alpha);
            adapter.render(context, matrices, vertexConsumer, light, overlay, colour, callback);
        }
    }

    public String getAssetName(TrimMaterial trimMaterial) {
        String assetName;
        if(RuntimeTrimsClient.overrideExisting) {
            assetName = RuntimeTrims.DYNAMIC;
        } else {
            assetName = trimMaterial.assetName();
        }
        return assetName;
    }

    public ResourceLocation getOverridenId(ArmorTrim trim, Holder<ArmorMaterial> armourMaterial, boolean leggings) {
        ResourceLocation modelId = getModelId(trim, armourMaterial, leggings);
        modelId = modelId.withPath(path -> {
            TrimMaterial trimMaterial = trim.material().value();
            Map<Holder<ArmorMaterial>, String> overrides = trimMaterial.overrideArmorMaterials();
            String assetId = overrides.getOrDefault(armourMaterial, trimMaterial.assetName());
            return path.replace(assetId, RuntimeTrims.DYNAMIC);
        });
        return modelId;
    }

    public ResourceLocation getModelId(TextureAtlasSprite sprite) {
        return sprite.contents().name();
    }

    public ResourceLocation getModelId(ArmorTrim trim, Holder<ArmorMaterial> armourMaterial, boolean leggings) {
        return leggings ? trim.innerTexture(armourMaterial) : trim.outerTexture(armourMaterial);
    }

    public interface RenderCallback {
        void render(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int colour);
    }
}
