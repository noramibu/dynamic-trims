package me.noramibu.dynamictrim.runtime.client.render.adapter;

import me.noramibu.dynamictrim.runtime.client.shader.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public abstract class TrimRendererAdapter {
    public abstract RenderType getLegacyRenderLayer(ArmorTrim trim);

    public abstract int getAlpha(RenderContext context);
}
