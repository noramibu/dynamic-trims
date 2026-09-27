package me.noramibu.dynamictrim.runtime.client;

import me.noramibu.dynamictrim.runtime.client.model.armour.ArmourTrimModelLoader;
import me.noramibu.dynamictrim.runtime.client.model.item.ItemTrimModelLoader;
import me.noramibu.dynamictrim.runtime.client.palette.TrimPalettes;
import me.noramibu.dynamictrim.runtime.client.render.LayerData;

public final class RuntimeTrimsClient {
    private static final LayerData layerData = new LayerData();
    private static final TrimPalettes trimPalettes = new TrimPalettes();
    private static final ItemTrimModelLoader itemModelLoader = new ItemTrimModelLoader(layerData);
    private static final ArmourTrimModelLoader armourModelLoader = new ArmourTrimModelLoader(layerData);

    public static boolean debug = Boolean.getBoolean("dynamictrim.debug");

    public static TrimPalettes getTrimPalettes() {
        return trimPalettes;
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

    public static LayerData getLayerData() {
        return layerData;
    }
}
