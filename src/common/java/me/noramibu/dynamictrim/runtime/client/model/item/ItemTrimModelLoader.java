package me.noramibu.dynamictrim.runtime.client.model.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.noramibu.dynamictrim.DynamicTrim;
import me.noramibu.dynamictrim.client.TrimPatternProperty;
import me.noramibu.dynamictrim.client.adapters.DynamicTrimsTrimModelLoaderAdapter;
import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.debug.Debugger;
import me.noramibu.dynamictrim.runtime.client.model.item.adapter.TrimModelLoaderAdapter;
import me.noramibu.dynamictrim.runtime.client.model.item.json.BlockAtlas;
import me.noramibu.dynamictrim.runtime.client.model.item.json.TextureLayers;
import me.noramibu.dynamictrim.runtime.client.model.item.json.TrimmableItemModel;
import me.noramibu.dynamictrim.runtime.client.render.LayerData;
import me.noramibu.dynamictrim.runtime.util.ItemAdaptable;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;

public final class ItemTrimModelLoader extends ItemAdaptable<TrimModelLoaderAdapter> {
    private static final Pattern itemDefinitionIdPattern = Pattern.compile("^items/(.+)?(?=.json).json$");
    private static final String MODEL_RESOURCE_PREFIX = "models/";
    private static final String MODEL_RESOURCE_SUFFIX = ".json";
    private static final String TRIM_MODEL_SUFFIX = "_trim.json";
    private final JsonParser jsonParser;
    private final LayerData layerData;

    public ItemTrimModelLoader(LayerData layerData) {
        this.layerData = layerData;
        this.jsonParser = new JsonParser();
    }

    public Map<Identifier, Resource> loadModels(Map<Identifier, Resource> loadedModels) {
        Map<Identifier, Resource> extendedModels = new HashMap<>(loadedModels);
        for (Map.Entry<Identifier, Resource> entry : loadedModels.entrySet()) {
            TrimModelResource trimModelResource = getTrimModelResource(entry.getKey(), entry.getValue());
            if (trimModelResource == null) {
                continue;
            }

            TrimmableItemModel baseModel = jsonParser.fromResource(entry.getValue(), TrimmableItemModel.class);
            if (baseModel == null || baseModel.textures == null) {
                continue;
            }

            Map<Identifier, TrimmableItemModel> patternModels = createPatternModels(
                    trimModelResource,
                    baseModel
            );
            patternModels.forEach((modelId, model) -> {
                Resource resource = jsonParser.toResource(trimModelResource.resource().source(), model);
                Identifier resourceId = modelId.withPrefix(MODEL_RESOURCE_PREFIX)
                        .withSuffix(MODEL_RESOURCE_SUFFIX);
                extendedModels.put(resourceId, resource);
                Debugger.createJson("resources/%s".formatted(resourceId), resource);
            });
        }
        return extendedModels;
    }

    public Map<Identifier, Resource> loadItemDefinitions(Map<Identifier, Resource> loadedDefinitions) {
        Map<Identifier, Resource> extendedDefinitions = new HashMap<>(loadedDefinitions);
        for (Map.Entry<Identifier, Resource> entry : loadedDefinitions.entrySet()) {
            Item item = getItemFromDefinitionResource(entry.getKey());
            if (item == null || !getAdapter(item).canTrim(item)) {
                continue;
            }

            String equipmentType = getEquipmentType(item);
            if (equipmentType == null) {
                continue;
            }

            JsonObject itemDefinition = jsonParser.fromResource(entry.getValue(), JsonObject.class);
            if (itemDefinition == null || !itemDefinition.has("model")) {
                continue;
            }

            JsonObject model = itemDefinition.getAsJsonObject("model");
            if (!isTrimMaterialSelect(model)) {
                continue;
            }

            JsonArray cases = model.getAsJsonArray("cases");
            for (JsonElement element : cases) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject switchCase = element.getAsJsonObject();
                JsonObject caseModel = getModelObject(switchCase);
                if (caseModel == null || !caseModel.has("model")) {
                    continue;
                }

                JsonObject patternSelect = createPatternSelect(
                        caseModel,
                        caseModel.get("model").getAsString(),
                        equipmentType
                );
                if (patternSelect != null) {
                    switchCase.add("model", patternSelect);
                }
            }

            Resource resource = jsonParser.toResource(entry.getValue().source(), itemDefinition);
            extendedDefinitions.put(entry.getKey(), resource);
            Debugger.createJson("resources/%s".formatted(entry.getKey()), resource);
        }
        return extendedDefinitions;
    }

    public void loadModels(Identifier id, Resource resource, BiConsumer<Identifier, Resource> loadedModelConsumer) {
        loadModels(Map.of(id, resource)).forEach(loadedModelConsumer);
    }

    private Map<Identifier, TrimmableItemModel> createPatternModels(
            TrimModelResource trimModelResource,
            TrimmableItemModel baseModel) {
        Map<Identifier, TrimmableItemModel> patternModels = new HashMap<>();
        int trimStartLayer = getTrimStartLayer(baseModel.textures);
        layerData.setTrimStartLayer(trimModelResource.item(), trimStartLayer);

        for (Identifier patternId : DynamicTrimsTrimModelLoaderAdapter.TEMPLATE_PATTERN_INDEX_SUPPLIER.get().keySet()) {
            if (!DynamicTrimsTrimModelLoaderAdapter.hasItemTexture(
                    trimModelResource.equipmentType(),
                    patternId
            )) {
                continue;
            }

            Identifier modelId = getPatternModelId(trimModelResource.modelId(), patternId);
            TrimmableItemModel patternModel = baseModel.copy();
            patternModel.textures = TextureLayers.of(createPatternLayers(
                    baseModel.textures,
                    trimStartLayer,
                    trimModelResource,
                    patternId
            ));
            patternModels.put(modelId, patternModel);
        }
        return patternModels;
    }

    private Map<String, String> createPatternLayers(
            TextureLayers baseTextures,
            int trimStartLayer,
            TrimModelResource trimModelResource,
            Identifier patternId) {
        Map<String, String> layers = new LinkedHashMap<>();
        baseTextures.layers.entrySet().stream()
                .filter(entry -> getLayerIndex(entry.getKey()) < trimStartLayer)
                .sorted(Comparator.comparingInt(entry -> getLayerIndex(entry.getKey())))
                .forEach(entry -> layers.put(entry.getKey(), entry.getValue()));

        List<String> trimLayers = getPatternLayerNames(trimModelResource, patternId);
        for (int i = 0; i < trimLayers.size(); i++) {
            layers.put("layer%s".formatted(trimStartLayer + i), trimLayers.get(i));
        }
        return layers;
    }

    private List<String> getPatternLayerNames(TrimModelResource trimModelResource, Identifier patternId) {
        String textureName = DynamicTrimsTrimModelLoaderAdapter.getPatternTextureName(patternId);
        if (!trimModelResource.trimType().equals(RuntimeTrims.DYNAMIC)) {
            return List.of("minecraft:trims/items/%s/%s_%s".formatted(
                    trimModelResource.equipmentType(),
                    textureName,
                    trimModelResource.trimType()
            ));
        }

        Identifier patternTextureId = Identifier.withDefaultNamespace(
                "textures/trims/items/%s/%s.png".formatted(trimModelResource.equipmentType(), textureName)
        );
        int maxLayerCount = layerData.getMaxSupportedLayer(patternTextureId);
        if (maxLayerCount == 0) {
            return List.of("minecraft:trims/items/%s/%s_%s".formatted(
                    trimModelResource.equipmentType(),
                    textureName,
                    RuntimeTrims.DYNAMIC
            ));
        }

        List<String> layers = new ArrayList<>();
        for (int i = 0; i < maxLayerCount; i++) {
            layers.add("minecraft:trims/items/%s/%s_%s_%s".formatted(
                    trimModelResource.equipmentType(),
                    textureName,
                    i,
                    RuntimeTrims.DYNAMIC
            ));
        }
        return layers;
    }

    private JsonObject createPatternSelect(
            JsonObject fallbackModel,
            String baseModelId,
            String equipmentType) {
        JsonArray cases = new JsonArray();
        for (Identifier patternId : DynamicTrimsTrimModelLoaderAdapter.TEMPLATE_PATTERN_INDEX_SUPPLIER.get().keySet()) {
            if (!DynamicTrimsTrimModelLoaderAdapter.hasItemTexture(equipmentType, patternId)) {
                continue;
            }

            JsonObject model = fallbackModel.deepCopy();
            model.addProperty("model", getPatternModelId(
                    Identifier.parse(baseModelId),
                    patternId
            ).toString());

            JsonObject switchCase = new JsonObject();
            switchCase.addProperty("when", patternId.toString());
            switchCase.add("model", model);
            cases.add(switchCase);
        }

        if (cases.size() == 0) {
            return null;
        }

        JsonObject select = new JsonObject();
        select.addProperty("type", "minecraft:select");
        select.addProperty("property", TrimPatternProperty.propertyId());
        select.add("cases", cases);
        select.add("fallback", fallbackModel.deepCopy());
        return select;
    }

    private Identifier getPatternModelId(Identifier baseModelId, Identifier patternId) {
        return baseModelId.withSuffix(
                "-" + DynamicTrimsTrimModelLoaderAdapter.getPatternTextureName(patternId)
        );
    }

    private JsonObject getModelObject(JsonObject switchCase) {
        if (!switchCase.has("model") || !switchCase.get("model").isJsonObject()) {
            return null;
        }
        return switchCase.getAsJsonObject("model");
    }

    private boolean isTrimMaterialSelect(JsonObject model) {
        return model.has("type")
                && model.has("property")
                && model.has("cases")
                && model.get("type").getAsString().equals("minecraft:select")
                && model.get("property").getAsString().equals("minecraft:trim_material")
                && model.get("cases").isJsonArray();
    }

    private int getTrimStartLayer(TextureLayers textures) {
        return textures.layers.entrySet().stream()
                .filter(entry -> entry.getValue().contains("trims/items"))
                .mapToInt(entry -> getLayerIndex(entry.getKey()))
                .min()
                .orElse(textures.layers.size());
    }

    private int getLayerIndex(String layerKey) {
        if (!layerKey.startsWith("layer")) {
            return Integer.MAX_VALUE;
        }
        return Integer.parseInt(layerKey.substring("layer".length()));
    }

    private Item getItemFromDefinitionResource(Identifier resourceId) {
        Matcher matcher = itemDefinitionIdPattern.matcher(resourceId.getPath());
        if (!matcher.matches()) {
            return null;
        }

        Identifier itemId = Identifier.fromNamespaceAndPath(
                resourceId.getNamespace(),
                matcher.group(1)
        );
        return BuiltInRegistries.ITEM.get(itemId).map(Holder::value).orElse(null);
    }

    private TrimModelResource getTrimModelResource(Identifier resourceId, Resource resource) {
        String path = resourceId.getPath();
        if (!path.endsWith(TRIM_MODEL_SUFFIX)) {
            return null;
        }

        for (Item item : BuiltInRegistries.ITEM) {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            if (!itemId.getNamespace().equals(resourceId.getNamespace()) || !getAdapter(item).canTrim(item)) {
                continue;
            }

            String prefix = "models/item/%s_".formatted(itemId.getPath());
            if (!path.startsWith(prefix)) {
                continue;
            }

            String trimType = path.substring(prefix.length(), path.length() - TRIM_MODEL_SUFFIX.length());
            String equipmentType = getEquipmentType(item);
            if (equipmentType == null) {
                continue;
            }

            return new TrimModelResource(
                    item,
                    equipmentType,
                    trimType,
                    resourceId.withPath(modelPath -> modelPath.substring(
                            MODEL_RESOURCE_PREFIX.length(),
                            modelPath.length() - MODEL_RESOURCE_SUFFIX.length()
                    )),
                    resource
            );
        }
        return null;
    }

    private String getEquipmentType(Item item) {
        Equippable equipment = item.components().get(DataComponents.EQUIPPABLE);
        if (equipment == null) {
            return null;
        }

        return switch (equipment.slot()) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> null;
        };
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

    private record TrimModelResource(
            Item item,
            String equipmentType,
            String trimType,
            Identifier modelId,
            Resource resource) {
    }
}
