package me.noramibu.dynamictrim.runtime.client.mixin.shader;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Unique
    private static final String DYNAMICTRIM_SHADER = "shaders/core/rendertype_dynamic_trim";

    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyReceiver(
            method = "reloadShaders",
            at = @At(
                    value = "INVOKE:LAST",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private List<Pair<ShaderInstance, Consumer<ShaderInstance>>> loadDynamicTrimProgram(List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaders, Object arg, ResourceProvider factory) throws IOException {
        shaders.add(
                Pair.of(
                        new ShaderInstance(
                                dynamictrim$shaderProvider(factory),
                                "rendertype_dynamic_trim",
                                DefaultVertexFormat.NEW_ENTITY
                        ),
                        program -> RuntimeTrimsClient.getShaderManager().renderTypeDynamicTrimProgram = program
                )
        );
        return shaders;
    }

    @Unique
    private ResourceProvider dynamictrim$shaderProvider(ResourceProvider fallback) {
        return id -> fallback.getResource(id).or(() -> dynamictrim$getShaderResource(id));
    }

    @Unique
    private Optional<Resource> dynamictrim$getShaderResource(ResourceLocation id) {
        if (!"minecraft".equals(id.getNamespace())) {
            return Optional.empty();
        }

        String path = id.getPath();
        if (!(path.equals(DYNAMICTRIM_SHADER + ".json")
                || path.equals(DYNAMICTRIM_SHADER + ".vsh")
                || path.equals(DYNAMICTRIM_SHADER + ".fsh"))) {
            return Optional.empty();
        }

        String classpathResource = "assets/minecraft/" + path;
        return Optional.of(new Resource(Minecraft.getInstance().getVanillaPackResources(), () -> {
            InputStream stream = GameRendererMixin.class.getClassLoader().getResourceAsStream(classpathResource);
            if (stream == null) {
                throw new FileNotFoundException(id.toString());
            }
            return stream;
        }));
    }
}
