package me.noramibu.dynamictrim.runtime.client.palette;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.colour.ColourHSB;
import me.noramibu.dynamictrim.runtime.client.colour.OkLabHelper;
import me.noramibu.dynamictrim.runtime.client.mixin.accessor.SpriteContentsAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class TrimPaletteGenerator {
    public TrimPalette generatePalette(Item item) {
        Minecraft client = Minecraft.getInstance();
        ItemRenderer itemRenderer = client.getItemRenderer();
        LocalPlayer player = client.player;
        if (player == null) {
            return TrimPalette.DEFAULT;
        }

        ItemStack stack = item.getDefaultInstance();
        BakedModel itemModel = itemRenderer.getModel(stack, client.level, player, player.getId());

        List<Integer> colours;
        if(itemModel.isCustomRenderer()) {
            colours = getColoursFromBuiltin(itemModel);
        } else {
            colours = getColoursFromStandard(itemModel);
        }

        List<Integer> vibrantPalette = generateVibrantPalette(colours);
        if(vibrantPalette.isEmpty()) {
            RuntimeTrims.LOGGER.warn("Could not generate palette for {}", item.getDescription().getString());
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
        List<ColourHSB> toSort = ColourHSB.fromRGB(colours);
        RuntimeTrimsClient.PaletteSorting paletteSorting = RuntimeTrimsClient.paletteSorting;
        Comparator<ColourHSB> comparator = Comparator.comparing(colourHSB -> {
            if(paletteSorting.isBrightness()) {
                return colourHSB.brightness();
            } else if (paletteSorting.isSaturation()) {
                return colourHSB.saturation();
            } else if (paletteSorting.isColour()) {
                return (float) colourHSB.colour();
            }
            return 0f;
        });
        if(!paletteSorting.isReversed()) { // match vanilla's direction of lightest -> darkest and avoid unneccessary double reversal
            comparator = comparator.reversed();
        }
        toSort.sort(comparator);
        return toSort.stream().map(ColourHSB::colour).toList();
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

    private List<Integer> getColoursFromBuiltin(BakedModel model) {
        return Arrays.stream(extractColours(model.getParticleIcon())).boxed().toList();
    }

    private List<Integer> getColoursFromStandard(BakedModel model) {
        List<BakedQuad> quads = new ArrayList<>();
        RandomSource random = RandomSource.create();
        for (Direction direction : Direction.values()) {
            random.setSeed(42);
            quads.addAll(model.getQuads(null, direction, random));
        }
        random.setSeed(42);
        quads.addAll(model.getQuads(null, null, random));
        return getColoursFromQuads(quads);
    }

    /**
     * Extracts every pixel colour in a quad's sprite ignoring transparent pixels
     */
    private @NotNull List<Integer> getColoursFromQuads(List<BakedQuad> quads) {
        List<Integer> colours = new ArrayList<>(quads.size() * 16 * 16);
        for (BakedQuad bakedQuad : quads) {
            int[] colourData = extractColours(bakedQuad.getSprite());
            for (int colour : colourData) {
                colours.add(colour);
            }
        }
        return colours.stream().filter(i -> i != 0).toList();
    }

    // [x * y] = rgb
    private int[] extractColours(TextureAtlasSprite sprite) {
        NativeImage spriteImage = ((SpriteContentsAccessor) sprite.contents()).getOriginalImage();
        int width = spriteImage.getWidth();
        int height = spriteImage.getHeight();

        int[] colourData = new int[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int colour = spriteImage.getPixelRGBA(x, y);
                int alpha = FastColor.ABGR32.alpha(colour);
                if (alpha == 0) {
                    continue;
                }

                int red = FastColor.ABGR32.red(colour);
                int green = FastColor.ABGR32.green(colour);
                int blue = FastColor.ABGR32.blue(colour);
                int packed = red << 16 | green << 8 | blue;
                colourData[x + y * width] = packed;
            }
        }

        return colourData;
    }
}
