package me.noramibu.dynamictrim.runtime.client.mixin.shader;

import me.noramibu.dynamictrim.runtime.client.extend.RenderLayer$MultiPhaseParametersExtender;
import me.noramibu.dynamictrim.runtime.client.shader.TrimPalettePhase;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderType.CompositeState.class)
public abstract class RenderLayer$MultiPhaseParametersMixin implements RenderLayer$MultiPhaseParametersExtender {
    @Shadow @Final @Mutable
    ImmutableList<RenderStateShard> states;

    @Override
    public void runtimetrims$attachTrimPalette(TrimPalettePhase trimPalette) {
        states = ImmutableList.<RenderStateShard>builder()
                .addAll(states)
                .add(trimPalette)
                .build();
    }
}
