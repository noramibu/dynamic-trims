package me.noramibu.dynamictrim.runtime.client.model.item;

import me.noramibu.dynamictrim.runtime.client.debug.Debugger;
import me.noramibu.dynamictrim.runtime.client.model.item.adapter.TrimModelLoaderAdapter;
import me.noramibu.dynamictrim.runtime.client.model.item.json.BlockAtlas;
import me.noramibu.dynamictrim.runtime.client.model.item.json.ModelOverride;
import me.noramibu.dynamictrim.runtime.client.model.item.json.TextureLayers;
import me.noramibu.dynamictrim.runtime.client.model.item.json.TrimmableItemModel;
import me.noramibu.dynamictrim.runtime.client.render.LayerData;
import me.noramibu.dynamictrim.runtime.util.ItemAdaptable;
import me.noramibu.dynamictrim.DynamicTrim;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ItemTrimModelLoader extends ItemAdaptable<TrimModelLoaderAdapter> {
    private static final Pattern itemIdPattern = Pattern.compile("^models/item/(.+)?(?=.json).json$");
    private final JsonParser jsonParser;
    private final LayerData layerData;

    public ItemTrimModelLoader(LayerData layerData) {
        this.layerData = layerData;
        this.jsonParser = new JsonParser();
    }

    public Map<ResourceLocation, Resource> loadModels(Map<ResourceLocation, Resource> loadedModels) {
        Map<ResourceLocation, Resource> extendedModels = new HashMap<>(loadedModels);
        List<TrimmableResource> trimmableResources = findTrimmableResources(loadedModels);
        for(TrimmableResource trimmableResource : trimmableResources) {
            TrimmableItemModel itemModel = jsonParser.fromResource(trimmableResource.resource(), TrimmableItemModel.class);
            if(itemModel == null) continue;

            if(itemModel.isComplex()) {
                Item item = trimmableResource.item();
                if (!hasAdapter(item) || !getAdapter(item).canTrim(item)) {
                    continue;
                }
            }

            if(itemModel.textures == null) itemModel.textures = TextureLayers.empty();
            if(itemModel.overrides == null) itemModel.overrides = new ArrayList<>();

            Map<ResourceLocation, TrimmableItemModel> suppliedOverrides = getAdapter(trimmableResource.item()).supplyOverrides(jsonParser, itemModel, trimmableResource, this::createModelOverride);

            itemModel.overrides.sort(Comparator.<ModelOverride, Float>comparing(override -> {
                JsonObject predicate = override.predicate;
                if(predicate.has("trim_type")) {
                    return predicate.get("trim_type").getAsFloat();
                } else if (predicate.has("minecraft:trim_type")) {
                    return predicate.get("minecraft:trim_type").getAsFloat();
                }
                return 0f;
            }).thenComparing(override -> {
                JsonObject predicate = override.predicate;
                if (predicate.has(DynamicTrim.TRIM_PATTERN.toString())) {
                    return predicate.get(DynamicTrim.TRIM_PATTERN.toString()).getAsFloat();
                }
                return 0f;
            }));

            Resource newResource = jsonParser.toResource(trimmableResource.resource().source(), itemModel);
            extendedModels.put(trimmableResource.resourceId(), newResource);

            Debugger.createJson("resources/%s".formatted(trimmableResource.resourceId()), newResource);
            suppliedOverrides.forEach((modelId, override) -> {
                Resource overrideResource = jsonParser.toResource(trimmableResource.resource().source(), override);
                ResourceLocation overrideResourceId = modelId.withPrefix("models/").withSuffix(".json");
                extendedModels.put(overrideResourceId, overrideResource);

                Debugger.createJson("resources/%s".formatted(overrideResourceId), overrideResource);
            });
        }
        return extendedModels;
    }

    public void loadModels(ResourceLocation id, Resource resource, BiConsumer<ResourceLocation, Resource> loadedModelConsumer) {
        loadModels(Map.of(id, resource)).forEach(loadedModelConsumer);
    }

    /**
     * Generates a list of for valid trimmable item
     */
    private List<TrimmableResource> findTrimmableResources(Map<ResourceLocation, Resource> loadedModels) {
        List<TrimmableResource> trimmableResources = new ArrayList<>();
        for (ResourceLocation resourceId : loadedModels.keySet()) {
            String resourcePath = resourceId.getPath();
            Matcher matcher = itemIdPattern.matcher(resourcePath);
            if (!matcher.matches()) continue;

            String itemPath = matcher.group(1);
            ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(resourceId.getNamespace(), itemPath);
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item == Items.AIR) continue;
            if (!getAdapter(item).canTrim(item)) continue;

            trimmableResources.add(new TrimmableResource(item, resourceId, loadedModels.get(resourceId)));
        }
        return trimmableResources;
    }

    private TrimmableItemModel createModelOverride(TrimmableItemModel overridenModel, TrimmableResource trimmableResource) {
        Map<String, String> layers = new HashMap<>(overridenModel.textures.layers);

        int startLayer = layers.size();
        Item trimmable = trimmableResource.item();
        layerData.setTrimStartLayer(trimmable, startLayer);

        TrimModelLoaderAdapter adapter = getAdapter(trimmable);
        int layerCount = adapter.getLayerCount(trimmable);

        for(int i = 0; i < layerCount; i++) {
            layers.put("layer%s".formatted(i + startLayer), adapter.getLayerName(trimmable, i));
        }
        return TrimmableItemModel.builder()
                .parent(overridenModel.parent)
                .textures(TextureLayers.of(layers))
                .build();
    }

    public BufferedReader addGroupPermutationsToAtlasSources(BufferedReader original) {
        JsonObject atlasJson = jsonParser.fromReader(original, JsonObject.class);
        BlockAtlas atlas = jsonParser.fromJson(atlasJson, BlockAtlas.class);
        Optional<BlockAtlas.Source> palettedPermuationsSource = atlas.getPalettedPermutationsSource("trims/color_palettes/trim_palette");
        if (palettedPermuationsSource.isEmpty()) {
            return jsonParser.toReader(atlasJson);
        }

        atlas.addSource(palettedPermuationsSource.get()
                .copy()
                .withType(DynamicTrim.id("group_permutations").toString())
                .withDirectories(List.of(
                        "trims/items/helmet",
                        "trims/items/chestplate",
                        "trims/items/leggings",
                        "trims/items/boots"
                ))
                .withTextures(null)
        );

        return jsonParser.toReader(atlas);
    }
}
