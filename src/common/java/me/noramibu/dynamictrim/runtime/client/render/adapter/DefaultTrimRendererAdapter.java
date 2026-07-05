package me.noramibu.dynamictrim.runtime.client.render.adapter;

import me.noramibu.dynamictrim.runtime.client.shader.RenderContext;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public final class DefaultTrimRendererAdapter extends TrimRendererAdapter {
    @Override
    public RenderType getLegacyRenderLayer(ArmorTrim trim) {
        return Sheets.armorTrimsSheet(trim.pattern().value().decal());
    }

    @Override
    public int getAlpha(RenderContext context) {
        return 255;
    }
}
