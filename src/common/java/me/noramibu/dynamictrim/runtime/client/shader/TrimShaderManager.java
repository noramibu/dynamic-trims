package me.noramibu.dynamictrim.runtime.client.shader;

import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import me.noramibu.dynamictrim.runtime.client.shader.adapter.TrimRenderLayerAdpater;
import me.noramibu.dynamictrim.runtime.util.ItemAdaptable;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class TrimShaderManager extends ItemAdaptable<TrimRenderLayerAdpater> {
    private static final ShaderProgram DYNAMIC_TRIM_PROGRAM = new ShaderProgram(
            ResourceLocation.withDefaultNamespace("core/rendertype_dynamic_trim"),
            DefaultVertexFormat.NEW_ENTITY,
            ShaderDefines.EMPTY
    );

    private final RenderStateShard.ShaderStateShard dynamicTrimProgram = new RenderStateShard.ShaderStateShard(DYNAMIC_TRIM_PROGRAM);

    private int[] trimPalette = new int[8];

    public RenderType getTrimRenderLayer(Item trimmed, TrimPalette palette) {
        return getAdapter(trimmed).getRenderLayer(palette);
    }

    public void clearRenderLayerCaches() {
        getAdapters().forEach(TrimRenderLayerAdpater::clearCache);
    }

    public void setTrimPalette(int[] trimPalette) {
        RenderSystem.assertOnRenderThread();
        this.trimPalette = trimPalette;
    }

    public int[] getTrimPalette() {
        RenderSystem.assertOnRenderThread();
        return trimPalette;
    }

    public RenderStateShard.ShaderStateShard getProgram() {
        return dynamicTrimProgram;
    }
}
