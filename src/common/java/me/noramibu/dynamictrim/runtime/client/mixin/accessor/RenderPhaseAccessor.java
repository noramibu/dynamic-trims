package me.noramibu.dynamictrim.runtime.client.mixin.accessor;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderPhaseAccessor {
    @Accessor("NO_TRANSPARENCY")
    static RenderStateShard.TransparencyStateShard getNoTransparency() {
        throw new AssertionError();
    }

    @Accessor("TRANSLUCENT_TRANSPARENCY")
    static RenderStateShard.TransparencyStateShard getTranslucentTransparency() {
        throw new AssertionError();
    }

    @Accessor("NO_CULL")
    static RenderStateShard.CullStateShard getDisableCulling() {
        throw new AssertionError();
    }

    @Accessor("LIGHTMAP")
    static RenderStateShard.LightmapStateShard getEnableLightmap() {
        throw new AssertionError();
    }

    @Accessor("OVERLAY")
    static RenderStateShard.OverlayStateShard getEnableOverlayColor() {
        throw new AssertionError();
    }

    @Accessor("VIEW_OFFSET_Z_LAYERING")
    static RenderStateShard.LayeringStateShard getViewOffsetZLayering() {
        throw new AssertionError();
    }

    @Accessor("LEQUAL_DEPTH_TEST")
    static RenderStateShard.DepthTestStateShard getLequalDepthTest() {
        throw new AssertionError();
    }

    @Accessor("COLOR_WRITE")
    static RenderStateShard.WriteMaskStateShard getColorMask() {
        throw new AssertionError();
    }
}
