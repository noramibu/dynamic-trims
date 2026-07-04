package me.noramibu.dynamictrim.runtime.client;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.model.armour.ArmourTrimModelLoader;
import me.noramibu.dynamictrim.runtime.client.model.item.GroupPermutationsAtlasSource;
import me.noramibu.dynamictrim.runtime.client.model.item.ItemTrimModelLoader;
import me.noramibu.dynamictrim.runtime.client.model.item.adapter.DefaultTrimModelLoaderAdapter;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalette;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalettes;
import me.noramibu.dynamictrim.runtime.client.render.LayerData;
import me.noramibu.dynamictrim.runtime.client.render.TrimRenderer;
import me.noramibu.dynamictrim.runtime.client.render.adapter.DefaultTrimRendererAdapter;
import me.noramibu.dynamictrim.runtime.client.shader.TrimShaderManager;
import me.noramibu.dynamictrim.runtime.client.shader.adapter.DefaultTrimRenderLayerAdapter;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public final class RuntimeTrimsClient {
    private static final LayerData layerData = new LayerData();
    private static final TrimPalettes trimPalettes = new TrimPalettes();
    private static final TrimRenderer trimRenderer = new TrimRenderer();
    private static final ItemTrimModelLoader itemModelLoader = new ItemTrimModelLoader(layerData);
    private static final ArmourTrimModelLoader armourModelLoader = new ArmourTrimModelLoader(layerData);
    private static final TrimShaderManager shaderManager = new TrimShaderManager();

    public static boolean overrideExisting = false;
    public static boolean useLegacyRenderer = false;
    public static boolean debug = false;
    public static PaletteSorting paletteSorting = PaletteSorting.COLOUR;
    public static boolean animate = false;
    public static float msBetweenCycles = 75;

    public static void init() {
        GroupPermutationsAtlasSource.init();
        itemModelLoader.setDefaultAdapter(new DefaultTrimModelLoaderAdapter());
        trimRenderer.setDefaultAdapter(new DefaultTrimRendererAdapter());
        shaderManager.setDefaultAdapter(new DefaultTrimRenderLayerAdapter());
    }

    public static boolean isDynamic(ArmorTrim trim) {
        return trim.material().value().itemModelIndex() == RuntimeTrims.MATERIAL_MODEL_INDEX || overrideExisting;
    }

    public static TrimPalettes getTrimPalettes() {
        return trimPalettes;
    }

    public static TrimRenderer getTrimRenderer() {
        return trimRenderer;
    }

    /**
     * The inventory item models
     */
    public static ItemTrimModelLoader getItemModelLoader() {
        return itemModelLoader;
    }

    /**
     * The in-world armour models
     */
    public static ArmourTrimModelLoader getArmourModelLoader() {
        return armourModelLoader;
    }

    public static TrimShaderManager getShaderManager() {
        return shaderManager;
    }

    public static LayerData getLayerData() {
        return layerData;
    }

    public enum PaletteSorting {
        COLOUR, COLOUR_REVERSED,
        SATURATION, SATURATION_REVERSED,
        BRIGHTNESS, BRIGHTNESS_REVERSED;

        public boolean isReversed() {
            return this == COLOUR_REVERSED || this == SATURATION_REVERSED || this == BRIGHTNESS_REVERSED;
        }

        public boolean isColour() {
            return this == COLOUR || this == COLOUR_REVERSED;
        }

        public boolean isSaturation() {
            return this == SATURATION || this == SATURATION_REVERSED;
        }

        public boolean isBrightness() {
            return this == BRIGHTNESS || this == BRIGHTNESS_REVERSED;
        }
    }

    public static void clearPaletteCache(PaletteSorting paletteSorting) {
        getTrimPalettes().regenerate();
        clearRenderLayerCache(msBetweenCycles);
    }

    public static void clearAnimationCache(boolean animate) {
        clearRenderLayerCache(msBetweenCycles);
        if (!animate) {
            getTrimPalettes().forEach(TrimPalette::computeColourArr);
        }
    }

    public static void clearRenderLayerCache(float msBetweenCycles) {
        getShaderManager().clearRenderLayerCaches();
    }
}
