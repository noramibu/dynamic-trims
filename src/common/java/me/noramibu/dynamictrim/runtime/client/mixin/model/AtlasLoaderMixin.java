package me.noramibu.dynamictrim.runtime.client.mixin.model;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.io.BufferedReader;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(SpriteSourceList.class)
public abstract class AtlasLoaderMixin {
    @ModifyExpressionValue(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/Resource;openAsReader()Ljava/io/BufferedReader;"
            )
    )
    private static BufferedReader addGroupPermutationsToAtlasSources(BufferedReader original, ResourceManager manager, ResourceLocation id) {
        String path = id.getPath();
        if (!(id.getNamespace().equals("minecraft") && (path.equals("blocks") || path.equals("atlases/blocks")))) {
            return original;
        }

        return RuntimeTrimsClient.getItemModelLoader().addGroupPermutationsToAtlasSources(original);
    }
}
