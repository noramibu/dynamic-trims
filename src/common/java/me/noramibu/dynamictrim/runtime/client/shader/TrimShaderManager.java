package me.noramibu.dynamictrim.runtime.client.shader;

import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import me.noramibu.dynamictrim.runtime.client.shader.adapter.TrimRenderLayerAdpater;
import me.noramibu.dynamictrim.runtime.util.ItemAdaptable;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.item.Item;

public final class TrimShaderManager extends ItemAdaptable<TrimRenderLayerAdpater> {
    public ShaderInstance renderTypeDynamicTrimProgram;

    private final RenderStateShard.ShaderStateShard DYNAMIC_TRIM_PROGRAM = new RenderStateShard.ShaderStateShard(() -> renderTypeDynamicTrimProgram);

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
        return DYNAMIC_TRIM_PROGRAM;
    }
}
