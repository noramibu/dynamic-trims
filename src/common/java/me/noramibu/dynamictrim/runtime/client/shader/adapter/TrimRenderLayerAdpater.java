package me.noramibu.dynamictrim.runtime.client.shader.adapter;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.extend.RenderLayer$MultiPhaseParametersExtender;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import me.noramibu.dynamictrim.runtime.client.shader.RenderContext;
import me.noramibu.dynamictrim.runtime.client.shader.TrimPalettePhase;
import me.noramibu.dynamictrim.runtime.client.shader.TrimShaderManager;
import me.noramibu.dynamictrim.runtime.util.MemoizedFunction;
import net.minecraft.client.renderer.RenderType;

public abstract class TrimRenderLayerAdpater {
    private final TrimShaderManager shaderManager = RuntimeTrimsClient.getShaderManager();

    protected abstract MemoizedFunction<TrimPalette, RenderType> getRenderLayer();

    protected abstract RenderType.CompositeState.CompositeStateBuilder getPhaseParametersBuilder();

    protected RenderType.CompositeState getPhaseParameters(TrimPalette palette) {
        RenderType.CompositeState.CompositeStateBuilder builder = getPhaseParametersBuilder()
                .setShaderState(shaderManager.getProgram());
        RenderType.CompositeState parameters = builder.createCompositeState(true);
        ((RenderLayer$MultiPhaseParametersExtender) (Object) parameters).runtimetrims$attachTrimPalette(
                new TrimPalettePhase(
                        "trim_palette",
                        () -> shaderManager.setTrimPalette(getPaletteColours(palette)),
                        () -> {}
                )
        );
        return parameters;
    }

    protected RenderContext getContext() {
        return RuntimeTrimsClient.getTrimRenderer().getContext();
    }

    protected int[] getPaletteColours(TrimPalette palette) {
        return palette.getColourArr();
    }

    public RenderType getRenderLayer(TrimPalette trimPalette) {
        return getRenderLayer().apply(trimPalette);
    }

    public void clearCache() {
        getRenderLayer().clear();
    }
}
