package me.noramibu.dynamictrim.runtime.client.mixin.model;

import me.noramibu.dynamictrim.DynamicTrim;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    @WrapOperation(
            method = "loadPaletteEntryFromImage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResource(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;"
            )
    )
    private static Optional<Resource> addDynamicPaletteImage(ResourceManager instance, Identifier identifier, Operation<Optional<Resource>> original) {
        Optional<Resource> existing = original.call(instance, identifier);
        if (existing.isPresent()) return existing;
        if (!isDynamicPalette(identifier)) return existing;

        PackResources defaultPack = Minecraft.getInstance().getVanillaPackResources().fullResources();
        Resource dynamicResource = runtimetrims$createGradientTrimPaletteResource(defaultPack);
        return Optional.of(dynamicResource);
    }

    @Unique
    private static boolean isDynamicPalette(Identifier identifier) {
        return identifier.equals(Identifier.withDefaultNamespace("textures/trims/color_palettes/%s.png".formatted(DynamicTrim.DYNAMIC)))
                || identifier.equals(Identifier.withDefaultNamespace("textures/trim/%s.png".formatted(DynamicTrim.DYNAMIC)));
    }

    @Unique
    private static @NotNull Resource runtimetrims$createGradientTrimPaletteResource(PackResources defaultPack) {
        float dif = 255 / 8f;
        List<Integer> colours = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int brightness = (int) (255 - dif * i);
            colours.add(ARGB.color(255, brightness, brightness, brightness));
        }
        TrimPalette palette = new TrimPalette(colours);
        return new Resource(defaultPack, palette::toInputStream);
    }

    @WrapOperation(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResource(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;"
            )
    )
    private Optional<Resource> getLayeredTrimResource(ResourceManager instance, Identifier layerId, Operation<Optional<Resource>> original, @Share("layerId") LocalRef<Identifier> layerIdRef) {
        layerIdRef.set(layerId);
        Optional<Resource> originalResource = original.call(instance, layerId);
        if (originalResource.isPresent()) return originalResource;

        String path = layerId.getPath();
        int pathEnd = path.lastIndexOf('_');
        Identifier originalLayerId = pathEnd > 0 ? layerId.withPath(path.substring(0, pathEnd) + ".png") : layerId;
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
    private Set<Map.Entry<String, Supplier<IntUnaryOperator>>> removeAllNonBlankPalettes(Set<Map.Entry<String, Supplier<IntUnaryOperator>>> permutations, @Share("layerId") LocalRef<Identifier> layerIdRef) {
        return RuntimeTrimsClient.getArmourModelLoader().cleanPermutations(permutations, layerIdRef.get());
    }
}
