package me.noramibu.dynamictrim.runtime.client.palette;

import me.noramibu.dynamictrim.runtime.client.debug.Debugger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public final class TrimPalettes {
    private final ConcurrentMap<Item, TrimPalette> cache = new ConcurrentHashMap<>();
    private final TrimPaletteGenerator generator = new TrimPaletteGenerator();

    public TrimPalette getOrGeneratePalette(Item item) {
        return cache.computeIfAbsent(item, k -> {
            TrimPalette newPalette = generator.generatePalette(k);
            createDebugFile(k, newPalette);
            return newPalette;
        });
    }

    private void createDebugFile(Item item, TrimPalette palette) {
        Debugger.createImage(
                "palettes/%s.png".formatted(BuiltInRegistries.ITEM.getKey(item)),
                palette.toBufferedImage()
        );
    }
}
