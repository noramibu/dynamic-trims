package me.noramibu.dynamictrim.runtime.client.extend;

import com.mojang.blaze3d.shaders.Uniform;

public interface ShaderProgramExtender {
    Uniform runtimetrims$getTrimPalette();
    Uniform runtimetrims$getDebug();
}
