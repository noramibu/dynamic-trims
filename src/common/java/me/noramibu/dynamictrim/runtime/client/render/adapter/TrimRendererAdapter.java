package me.noramibu.dynamictrim.runtime.client.render.adapter;

import me.noramibu.dynamictrim.runtime.client.render.TrimRenderer;
import me.noramibu.dynamictrim.runtime.client.shader.RenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.armortrim.ArmorTrim;

public abstract class TrimRendererAdapter {
    /**
     * Render layer to use when rendering trims through the legacy renderer.<br>
     * This is used when {@link TrimRenderer#useLegacyRenderer} passes
     */
    public abstract RenderType getLegacyRenderLayer(ArmorTrim trim);

    /**
     * Alpha to render the trim at.
     * @return alpha in 0-255 range
     */
    public abstract int getAlpha(RenderContext context);

    /**
     * Entrypoint to modify how the trim renders.
     * @apiNote Must call {@link TrimRenderer.RenderCallback#render}
     */
    public abstract void render(RenderContext context, PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int colour, TrimRenderer.RenderCallback callback);
}
