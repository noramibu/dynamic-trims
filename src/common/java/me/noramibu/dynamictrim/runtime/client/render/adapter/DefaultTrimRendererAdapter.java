package me.noramibu.dynamictrim.runtime.client.render.adapter;

import me.noramibu.dynamictrim.runtime.client.render.TrimRenderer;
import me.noramibu.dynamictrim.runtime.client.shader.RenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
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

    @Override
    public void render(RenderContext context, PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int colour, TrimRenderer.RenderCallback callback) {
        callback.render(matrices, vertexConsumer, light, overlay, colour);
    }
}
