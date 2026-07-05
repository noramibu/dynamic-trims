package me.noramibu.dynamictrim.runtime.client.model.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.noramibu.dynamictrim.DynamicTrim;
import me.noramibu.dynamictrim.client.TrimPatternProperty;
import me.noramibu.dynamictrim.runtime.client.debug.Debugger;
import me.noramibu.dynamictrim.runtime.client.model.item.json.BlockAtlas;
import me.noramibu.dynamictrim.runtime.client.model.item.json.TextureLayers;
import me.noramibu.dynamictrim.runtime.client.model.item.json.TrimmableItemModel;
import me.noramibu.dynamictrim.runtime.client.render.LayerData;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.Equippable;

public final class ItemTrimModelLoader {
    private static final Pattern itemDefinitionIdPattern = Pattern.compile("^items/(.+)?(?=.json).json$");
    private static final String MODEL_RESOURCE_PREFIX = "models/";
    private static final String MODEL_RESOURCE_SUFFIX = ".json";
    private static final String TRIM_MODEL_SUFFIX = "_trim.json";
    private static final String TRIM_TEMPLATE_SUFFIX = "_armor_trim_smithing_template";
    private final JsonParser jsonParser = new JsonParser();
    private final LayerData layerData;
    private Set<Identifier> templatePatternIds;

    public ItemTrimModelLoader(LayerData layerData) {
        this.layerData = layerData;
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
        Map<String, DefinitionTemplate> templatesByEquipmentType = new HashMap<>();
        Set<Identifier> processedDefinitions = new HashSet<>();
        for (Map.Entry<Identifier, Resource> entry : loadedDefinitions.entrySet()) {
            Item item = getItemFromDefinitionResource(entry.getKey());
            if (item == null || !canTrim(item)) {
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
            processedDefinitions.add(entry.getKey());
            addTemplate(templatesByEquipmentType, item, equipmentType, itemDefinition, resource);
            Debugger.createJson("resources/%s".formatted(entry.getKey()), resource);
        }
        addMissingItemDefinitions(extendedDefinitions, processedDefinitions, templatesByEquipmentType);
        return extendedDefinitions;
    }

    private void addTemplate(
            Map<String, DefinitionTemplate> templatesByEquipmentType,
            Item item,
            String equipmentType,
            JsonObject definition,
            Resource resource) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        DefinitionTemplate existing = templatesByEquipmentType.get(equipmentType);
        boolean leather = itemId.getPath().startsWith("leather_");
        if (existing != null && (!existing.leather() || leather)) {
            return;
        }
        templatesByEquipmentType.put(equipmentType, new DefinitionTemplate(
                definition.deepCopy(),
                resource,
                "%s:item/%s".formatted(itemId.getNamespace(), itemId.getPath()),
                leather
        ));
    }

    private void addMissingItemDefinitions(
            Map<Identifier, Resource> definitions,
            Set<Identifier> processedDefinitions,
        Map<String, DefinitionTemplate> templatesByEquipmentType) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (!canTrim(item)) {
                continue;
            }

            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            if (!itemId.getNamespace().equals("minecraft")) {
                continue;
            }

            Identifier resourceId = Identifier.fromNamespaceAndPath(
                    itemId.getNamespace(),
                    "items/%s.json".formatted(itemId.getPath())
            );
            if (processedDefinitions.contains(resourceId)) {
                continue;
            }

            String equipmentType = getEquipmentType(item);
            DefinitionTemplate template = templatesByEquipmentType.get(equipmentType);
            if (template == null) {
                continue;
            }

            JsonObject definition = template.definition().deepCopy();
            replaceStringValues(
                    definition,
                    template.modelPrefix(),
                    "%s:item/%s".formatted(itemId.getNamespace(), itemId.getPath())
            );
            if (itemId.getPath().startsWith("leather_")) {
                addLeatherDyeTints(definition);
            }

            Resource resource = jsonParser.toResource(template.resource().source(), definition);
            definitions.put(resourceId, resource);
            Debugger.createJson("resources/%s".formatted(resourceId), resource);
        }
    }

    private void replaceStringValues(JsonElement element, String from, String to) {
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    entry.setValue(new JsonPrimitive(value.getAsString().replace(from, to)));
                } else {
                    replaceStringValues(value, from, to);
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                JsonElement value = array.get(i);
                if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                    array.set(i, new JsonPrimitive(value.getAsString().replace(from, to)));
                } else {
                    replaceStringValues(value, from, to);
                }
            }
        }
    }

    private void addLeatherDyeTints(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (isModelObject(object) && !object.has("tints")) {
                JsonObject tint = new JsonObject();
                tint.addProperty("type", "minecraft:dye");
                tint.addProperty("default", DyedItemColor.LEATHER_COLOR);
                JsonArray tints = new JsonArray();
                tints.add(tint);
                object.add("tints", tints);
            }
            object.entrySet().forEach(entry -> addLeatherDyeTints(entry.getValue()));
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(this::addLeatherDyeTints);
        }
    }

    private boolean isModelObject(JsonObject object) {
        return object.has("type") && object.get("type").getAsString().equals("minecraft:model");
    }

    private Map<Identifier, TrimmableItemModel> createPatternModels(
            TrimModelResource trimModelResource,
            TrimmableItemModel baseModel) {
        Map<Identifier, TrimmableItemModel> patternModels = new HashMap<>();
        int trimStartLayer = getTrimStartLayer(baseModel.textures);

        for (Identifier patternId : getTemplatePatternIds()) {
            if (!hasItemTexture(
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
        String textureName = getPatternTextureName(patternId);
        if (!trimModelResource.trimType().equals(DynamicTrim.DYNAMIC)) {
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
                    DynamicTrim.DYNAMIC
            ));
        }

        List<String> layers = new ArrayList<>();
        for (int i = 0; i < maxLayerCount; i++) {
            layers.add("minecraft:trims/items/%s/%s_%s_%s".formatted(
                    trimModelResource.equipmentType(),
                    textureName,
                    i,
                    DynamicTrim.DYNAMIC
            ));
        }
        return layers;
    }

    private JsonObject createPatternSelect(
            JsonObject fallbackModel,
            String baseModelId,
            String equipmentType) {
        JsonArray cases = new JsonArray();
        for (Identifier patternId : getTemplatePatternIds()) {
            if (!hasItemTexture(equipmentType, patternId)) {
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
                "-" + getPatternTextureName(patternId)
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

    private Set<Identifier> getTemplatePatternIds() {
        if (templatePatternIds == null) {
            templatePatternIds = BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof SmithingTemplateItem)
                    .map(this::getPatternAssetId)
                    .filter(patternId -> patternId != null)
                    .sorted(Comparator.comparing(Identifier::toString))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return templatePatternIds;
    }

    private boolean hasItemTexture(String equipmentType, Identifier patternId) {
        String path = "assets/minecraft/textures/trims/items/%s/%s.png".formatted(
                equipmentType,
                getPatternTextureName(patternId)
        );
        return ItemTrimModelLoader.class.getClassLoader().getResource(path) != null;
    }

    private String getPatternTextureName(Identifier patternId) {
        return patternId.toString().replace(":", "-");
    }

    private Identifier getPatternAssetId(Item item) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        String itemPath = itemId.getPath();
        if (!itemPath.endsWith(TRIM_TEMPLATE_SUFFIX)) {
            return null;
        }
        return Identifier.fromNamespaceAndPath(
                itemId.getNamespace(),
                itemPath.substring(0, itemPath.length() - TRIM_TEMPLATE_SUFFIX.length()));
    }

    private boolean canTrim(Item item) {
        if (item == Items.ELYTRA) {
            return false;
        }

        Equippable equipment = getEquippable(item);
        return equipment != null ? equipment.slot().isArmor() : getEquipmentType(item) != null;
    }

    private Equippable getEquippable(Item item) {
        if (!BuiltInRegistries.ITEM.wrapAsHolder(item).areComponentsBound()) {
            return null;
        }

        return item.components().get(DataComponents.EQUIPPABLE);
    }

    private String getEquipmentType(Item item) {
        Equippable equipment = getEquippable(item);
        if (equipment != null) {
            return getEquipmentType(equipment);
        }

        String itemPath = BuiltInRegistries.ITEM.getKey(item).getPath();
        if (itemPath.endsWith("_helmet")) {
            return "helmet";
        } else if (itemPath.endsWith("_chestplate")) {
            return "chestplate";
        } else if (itemPath.endsWith("_leggings")) {
            return "leggings";
        } else if (itemPath.endsWith("_boots")) {
            return "boots";
        }

        return null;
    }

    private String getEquipmentType(Equippable equipment) {
        return switch (equipment.slot()) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> null;
        };
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
            if (!itemId.getNamespace().equals(resourceId.getNamespace()) || !canTrim(item)) {
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

    public BufferedReader addGroupPermutationsToAtlasSources(BufferedReader original, List<String> directories) {
        JsonObject atlasJson = jsonParser.fromReader(original, JsonObject.class);
        BlockAtlas atlas = jsonParser.fromJson(atlasJson, BlockAtlas.class);
        Optional<BlockAtlas.Source> palettedPermuationsSource = atlas.getPalettedPermutationsSource("trims/color_palettes/trim_palette");
        if (palettedPermuationsSource.isEmpty()) {
            return jsonParser.toReader(atlasJson);
        }

        atlas.addSource(palettedPermuationsSource.get()
                .copy()
                .withType(DynamicTrim.id("group_permutations").toString())
                .withDirectories(directories)
                .withTextures(null)
        );

        return jsonParser.toReader(atlas);
    }

    private record DefinitionTemplate(
            JsonObject definition,
            Resource resource,
            String modelPrefix,
            boolean leather) {
    }

    private record TrimModelResource(
            String equipmentType,
            String trimType,
            Identifier modelId,
            Resource resource) {
    }
}
