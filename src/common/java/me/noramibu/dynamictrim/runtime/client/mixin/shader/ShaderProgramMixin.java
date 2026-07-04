package me.noramibu.dynamictrim.runtime.client.mixin.shader;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.extend.ShaderProgramExtender;
import me.noramibu.dynamictrim.runtime.client.mixin.accessor.GlUniformAccessor;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.ShaderInstance;

@Mixin(ShaderInstance.class)
public abstract class ShaderProgramMixin implements ShaderProgramExtender {
    @Shadow @Nullable
    public abstract Uniform getUniform(String name);

    @Unique
    private Uniform runtimetrims$trimPalette;

    @Unique
    private Uniform runtimetrims$debug;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void initAdditionalUniforms(CallbackInfo ci) {
        runtimetrims$trimPalette = getUniform("dynamictrim_TrimPalette");
        runtimetrims$debug = getUniform("dynamictrim_Debug");
    }

    @Override
    public Uniform runtimetrims$getTrimPalette() {
        return runtimetrims$trimPalette;
    }

    @Override
    public Uniform runtimetrims$getDebug() {
        return runtimetrims$debug;
    }

    @Inject(
            method = "setDefaultUniforms",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setupShaderLights(Lnet/minecraft/client/renderer/ShaderInstance;)V"
            )
    )
    private void setAdditionalUniforms(VertexFormat.Mode mode, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Window window, CallbackInfo ci) {
        if(runtimetrims$trimPalette != null) {
            int[] trimPalette = RuntimeTrimsClient.getShaderManager().getTrimPalette();
            IntBuffer intData = runtimetrims$trimPalette.getIntBuffer();
            intData.position(0);
            for (int i = 0; i < trimPalette.length; i++) {
                intData.put(i, trimPalette[i]);
            }
            ((GlUniformAccessor) runtimetrims$trimPalette).callMarkDirty();
        }

        if(runtimetrims$debug != null) {
            runtimetrims$debug.set(RuntimeTrimsClient.debug ? 1 : 0);
        }
    }
}
