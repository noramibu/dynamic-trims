package me.noramibu.dynamictrim.runtime.client.palette;

import me.noramibu.dynamictrim.DynamicTrim;
import me.noramibu.dynamictrim.runtime.client.colour.ColourHSB;
import me.noramibu.dynamictrim.runtime.client.colour.OkLabHelper;
import me.noramibu.dynamictrim.runtime.client.mixin.accessor.SpriteContentsAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class TrimPaletteGenerator {
    public TrimPalette generatePalette(Item item) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) {
            return TrimPalette.DEFAULT;
        }

        ItemStack stack = item.getDefaultInstance();
        ItemStackRenderState renderState = new ItemStackRenderState();
        client.getItemModelResolver().updateForTopItem(
                renderState,
                stack,
                ItemDisplayContext.GUI,
                client.level,
                player,
                player.getId()
        );
        Material.Baked particle = renderState.pickParticleMaterial(RandomSource.create());
        if (particle == null) {
            DynamicTrim.LOGGER.warn("Could not generate palette for {}", item.getName(stack).getString());
            return TrimPalette.DEFAULT;
        }

        List<Integer> colours = getColoursFromSprite(particle.sprite());

        List<Integer> vibrantPalette = generateVibrantPalette(colours);
        if(vibrantPalette.isEmpty()) {
            DynamicTrim.LOGGER.warn("Could not generate palette for {}", item.getName(stack).getString());
            return TrimPalette.DEFAULT;
        }

        vibrantPalette = stretchPalette(vibrantPalette);
        vibrantPalette = sortPalette(vibrantPalette);

        return new TrimPalette(vibrantPalette);
    }

    /**
     * Attempt to generate the most vibrant palette from the item texture colours
     */
    private List<Integer> generateVibrantPalette(List<Integer> colours) {
        List<ColourHSB> hsbColours = ColourHSB.fromRGB(colours);

        hsbColours = hsbColours.stream()
                .distinct()
                .sorted(Comparator.comparing(ColourHSB::saturation)
                        .thenComparing(ColourHSB::brightness)
                        .reversed())
                .toList();

        List<Integer> vibrantPalette = new ArrayList<>();
        for (int i = 0; i < Math.min(TrimPalette.PALETTE_SIZE, hsbColours.size()); i++) {
            vibrantPalette.add(hsbColours.get(i).colour());
        }

        return vibrantPalette;
    }

    private List<Integer> sortPalette(List<Integer> colours) {
        return ColourHSB.fromRGB(colours).stream()
                .sorted(Comparator.comparing(ColourHSB::colour).reversed())
                .map(ColourHSB::colour)
                .toList();
    }

    /**
     * Generated palattes may be less than 8 pixels, so we need to stretch them
     */
    private List<Integer> stretchPalette(List<Integer> palette) {
        int size = palette.size();
        int targetSize = TrimPalette.PALETTE_SIZE;
        if (size >= targetSize) {
            return palette;
        }

        List<double[]> oklabPalette = new ArrayList<>();
        for (int rgb : palette) {
            double[] oklab = OkLabHelper.rgbToOKLab(rgb);
            oklabPalette.add(oklab);
        }

        List<double[]> stretchedOKLab = OkLabHelper.strechOkLab(targetSize, size, oklabPalette);

        List<Integer> stretchedPalette = new ArrayList<>(targetSize);
        for (double[] oklab : stretchedOKLab) {
            int rgb = OkLabHelper.oklabToRGB(oklab);
            stretchedPalette.add(rgb);
        }

        return stretchedPalette;
    }

    private List<Integer> getColoursFromSprite(TextureAtlasSprite sprite) {
        return Arrays.stream(extractColours(sprite)).boxed().filter(colour -> colour != 0).toList();
    }

    // [x * y] = rgb
    private int[] extractColours(TextureAtlasSprite sprite) {
        NativeImage spriteImage = ((SpriteContentsAccessor) sprite.contents()).getOriginalImage();
        int width = spriteImage.getWidth();
        int height = spriteImage.getHeight();

        int[] colourData = new int[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int colour = spriteImage.getPixel(x, y);
                int alpha = ARGB.alpha(colour);
                if (alpha == 0) {
                    continue;
                }

                int red = ARGB.red(colour);
                int green = ARGB.green(colour);
                int blue = ARGB.blue(colour);
                int packed = red << 16 | green << 8 | blue;
                colourData[x + y * width] = packed;
            }
        }

        return colourData;
    }
}
