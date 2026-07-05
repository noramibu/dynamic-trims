package me.noramibu.dynamictrim.runtime.client.mixin.model;

import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.io.BufferedReader;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.resources.Identifier;
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
    private static BufferedReader addGroupPermutationsToAtlasSources(BufferedReader original, ResourceManager manager, Identifier id) {
        String path = id.getPath();
        if (!id.getNamespace().equals("minecraft")) {
            return original;
        }

        if (path.equals("items") || path.equals("atlases/items") || path.equals("blocks") || path.equals("atlases/blocks")) {
            return RuntimeTrimsClient.getItemModelLoader().addGroupPermutationsToAtlasSources(
                    original,
                    List.of(
                            "trims/items/helmet",
                            "trims/items/chestplate",
                            "trims/items/leggings",
                            "trims/items/boots"
                    )
            );
        }

        if (path.equals("armor_trims") || path.equals("atlases/armor_trims")) {
            return RuntimeTrimsClient.getItemModelLoader().addGroupPermutationsToAtlasSources(
                    original,
                    List.of(
                            "trims/entity/humanoid",
                            "trims/entity/humanoid_leggings"
                    )
            );
        }

        return original;
    }
}
