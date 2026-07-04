package me.noramibu.dynamictrim.runtime.client.shader.adapter;

import me.noramibu.dynamictrim.runtime.client.mixin.accessor.RenderPhaseAccessor;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import me.noramibu.dynamictrim.runtime.util.MemoizedFunction;
import me.noramibu.dynamictrim.runtime.util.Memoizer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.util.TriState;

public final class DefaultTrimRenderLayerAdapter extends TrimRenderLayerAdpater {
    private final MemoizedFunction<TrimPalette, RenderType> DYNAMIC_TRIM_RENDER_LAYER = Memoizer.memoize(palette -> RenderType.create(
            "dynamic_trim",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            true,
            false,
            getPhaseParameters(palette)
    ));

    @Override
    protected MemoizedFunction<TrimPalette, RenderType> getRenderLayer() {
        return DYNAMIC_TRIM_RENDER_LAYER;
    }

    protected RenderType.CompositeState.CompositeStateBuilder getPhaseParametersBuilder() {
        return RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(Sheets.ARMOR_TRIMS_SHEET, TriState.FALSE, false))
                .setCullState(RenderPhaseAccessor.getDisableCulling())
                .setTransparencyState(RenderPhaseAccessor.getNoTransparency())
                .setLightmapState(RenderPhaseAccessor.getEnableLightmap())
                .setOverlayState(RenderPhaseAccessor.getEnableOverlayColor())
                .setLayeringState(RenderPhaseAccessor.getViewOffsetZLayering())
                .setDepthTestState(RenderPhaseAccessor.getLequalDepthTest());
    }
}
