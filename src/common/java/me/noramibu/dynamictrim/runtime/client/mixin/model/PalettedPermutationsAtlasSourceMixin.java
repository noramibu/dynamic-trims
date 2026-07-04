package me.noramibu.dynamictrim.runtime.client.mixin.model;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.util.Function3;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(PalettedPermutations.class)
public abstract class PalettedPermutationsAtlasSourceMixin {
    @ModifyArg(
            method = "method_48487",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/datafixers/Products$P3;apply(Lcom/mojang/datafixers/kinds/Applicative;Lcom/mojang/datafixers/util/Function3;)Lcom/mojang/datafixers/kinds/App;",
                    remap = false
            ),
            index = 1
    )
    private static <R> Function3<List<ResourceLocation>, ResourceLocation, Map<String, ResourceLocation>, R> createDynamicTrimPermutation(Function3<List<ResourceLocation>, ResourceLocation, Map<String, ResourceLocation>, R> function) {
        return (textures, paletteKey, palettedPermutations) -> {
            if (!paletteKey.getPath().contains("trim_palette")) return function.apply(textures, paletteKey, palettedPermutations);

            List<ResourceLocation> newTextures = new ArrayList<>(textures.size() * 9);
            for (ResourceLocation texture : textures) {
                newTextures.add(texture);
                for (int i = 0; i < 8; i++) {
                    newTextures.add(texture.withSuffix("_" + i));
                }
            }

            Map<String, ResourceLocation> newPermutations = new HashMap<>(palettedPermutations);
            newPermutations.put(RuntimeTrims.DYNAMIC, ResourceLocation.withDefaultNamespace("trims/color_palettes/%s".formatted(RuntimeTrims.DYNAMIC)));
            return function.apply(newTextures, paletteKey, newPermutations);
        };
    }

    @WrapOperation(
            method = "loadPaletteEntryFromImage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResource(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;"
            )
    )
    private static Optional<Resource> addDynamicPaletteImage(ResourceManager instance, ResourceLocation identifier, Operation<Optional<Resource>> original) {
        Optional<Resource> existing = original.call(instance, identifier);
        if (existing.isPresent()) return existing;
        if (!identifier.equals(ResourceLocation.withDefaultNamespace("textures/trims/color_palettes/%s.png".formatted(RuntimeTrims.DYNAMIC)))) return existing;

        PackResources defaultPack = Minecraft.getInstance().getVanillaPackResources();
        Resource dynamicResource = runtimetrims$createGradientTrimPaletteResource(defaultPack);
        return Optional.of(dynamicResource);
    }

    @Unique
    private static @NotNull Resource runtimetrims$createGradientTrimPaletteResource(PackResources defaultPack) {
        float dif = 255 / 8f;
        List<Integer> colours = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int brightness = (int) (255 - dif * i);
            colours.add(FastColor.ARGB32.color(255, brightness, brightness, brightness));
        }
        TrimPalette palette = new TrimPalette(colours);
        return new Resource(defaultPack, palette::toInputStream);
    }

    @WrapOperation(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResource(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;"
            )
    )
    private Optional<Resource> getLayeredTrimResource(ResourceManager instance, ResourceLocation layerId, Operation<Optional<Resource>> original, @Share("layerId") LocalRef<ResourceLocation> layerIdRef) {
        layerIdRef.set(layerId);
        Optional<Resource> originalResource = original.call(instance, layerId);
        if (originalResource.isPresent()) return originalResource;

        String path = layerId.getPath();
        int pathEnd = path.lastIndexOf('_');
        ResourceLocation originalLayerId = pathEnd > 0 ? layerId.withPath(path.substring(0, pathEnd) + ".png") : layerId;
        Optional<Resource> optionalResource = instance.getResource(originalLayerId);
        if (optionalResource.isEmpty()) return optionalResource;

        Pattern pattern = Pattern.compile("\\d+(?![\\d\\D]*\\d)");
        Matcher matcher = pattern.matcher(path);
        if (!matcher.find()) {
            return optionalResource;
        }

        int layer = Integer.parseInt(matcher.group());
        Resource resource = optionalResource.get();
        try (InputStream inputStream = resource.open()) {
            BufferedImage layerImage = ImageIO.read(inputStream);

            return RuntimeTrimsClient.getArmourModelLoader().loadLayeredResource(layerId, layerImage, originalLayerId, layer, resource.source());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ModifyExpressionValue(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;entrySet()Ljava/util/Set;"
            )
    )
    private Set<Map.Entry<String, Supplier<IntUnaryOperator>>> removeAllNonBlankPalettes(Set<Map.Entry<String, Supplier<IntUnaryOperator>>> permutations, @Share("layerId") LocalRef<ResourceLocation> layerIdRef) {
        return RuntimeTrimsClient.getArmourModelLoader().cleanPermutations(permutations, layerIdRef.get());
    }
}
