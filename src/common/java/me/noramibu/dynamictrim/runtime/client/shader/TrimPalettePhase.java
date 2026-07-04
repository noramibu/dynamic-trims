package me.noramibu.dynamictrim.runtime.client.shader;

import net.minecraft.client.renderer.RenderStateShard;

public final class TrimPalettePhase extends RenderStateShard {
    public TrimPalettePhase(String name, Runnable beginAction, Runnable endAction) {
        super(name, beginAction, endAction);
    }
}
