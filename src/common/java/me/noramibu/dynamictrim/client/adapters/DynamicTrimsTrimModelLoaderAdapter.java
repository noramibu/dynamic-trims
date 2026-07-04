package me.noramibu.dynamictrim.client.adapters;

import com.google.gson.JsonObject;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import me.noramibu.dynamictrim.DynamicTrim;
import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.RuntimeTrimsClient;
import me.noramibu.dynamictrim.runtime.client.model.item.JsonParser;
import me.noramibu.dynamictrim.runtime.client.model.item.TrimmableResource;
import me.noramibu.dynamictrim.runtime.client.model.item.adapter.DefaultTrimModelLoaderAdapter;
import me.noramibu.dynamictrim.runtime.client.model.item.json.ModelOverride;
import me.noramibu.dynamictrim.runtime.client.model.item.json.TrimmableItemModel;
import me.noramibu.dynamictrim.runtime.util.Memoizer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public class DynamicTrimsTrimModelLoaderAdapter extends DefaultTrimModelLoaderAdapter {
    public static final Supplier<Map<ResourceLocation, Float>> TEMPLATE_PATTERN_INDEX_SUPPLIER = Memoizer.memoize(() -> {
        Set<ResourceLocation> ids = BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof SmithingTemplateItem)
                .map(DynamicTrimsTrimModelLoaderAdapter::getPatternAssetId)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (ids.isEmpty()) {
            return Map.of();
        }
        int increment = 10000 / ids.size();
        Map<ResourceLocation, Float> patternIndexes = new HashMap<>(ids.size());
        for (ResourceLocation id : ids) {
            patternIndexes.put(id, increment / 10000f);
            increment += 10000 / ids.size();
        }
        return patternIndexes;
    });

    private ResourceLocation generatingModelId;

    public static float getPatternIndex(ArmorTrim trim) {
        ResourceLocation patternId = trim.pattern()
                .unwrapKey()
                .map(ResourceKey::location)
                .orElse(null);
        if (patternId == null) {
            return Float.NEGATIVE_INFINITY;
        }
        return TEMPLATE_PATTERN_INDEX_SUPPLIER.get().getOrDefault(patternId, Float.NEGATIVE_INFINITY);
    }

    @Override
    public Integer getLayerCount(Item item) {
        TrimInfo info = TrimInfo.fromModelId(item, generatingModelId);
        if(info == null) return super.getLayerCount(item);
        if (!info.isDynamic()) return 1;

        ResourceLocation patternTextureId = info.getPatternTextureId(getEquipmentType(item));
        int maxLayerCount = RuntimeTrimsClient.getLayerData().getMaxSupportedLayer(patternTextureId);
        if(maxLayerCount == 0) return super.getLayerCount(item);

        return maxLayerCount;
    }

    @Override
    public String getLayerName(Item item, int layerIndex) {
        TrimInfo info = TrimInfo.fromModelId(item, generatingModelId);
        if (info == null) return super.getLayerName(item, layerIndex);
        if (!info.isDynamic()) {
            return "minecraft:trims/items/%s/%s_%s".formatted(
                    getEquipmentType(item),
                    info.trimPattern(),
                    info.trimType()
            );
        }

        ResourceLocation patternTextureId = info.getPatternTextureId(getEquipmentType(item));
        if(RuntimeTrimsClient.getLayerData().getMaxSupportedLayer(patternTextureId) == 0) {
            return super.getLayerName(item, layerIndex);
        }

        return "minecraft:trims/items/%s/%s_%s_%s".formatted(
                getEquipmentType(item),
                info.trimPattern(),
                layerIndex,
                info.trimType()
        );
    }

    @Override
    public Map<ResourceLocation, TrimmableItemModel> supplyOverrides(JsonParser jsonParser, TrimmableItemModel itemModel, TrimmableResource resource, BiFunction<TrimmableItemModel, TrimmableResource, TrimmableItemModel> overrideCreator) {
        Map<ResourceLocation, TrimmableItemModel> overrides = new HashMap<>();
        List<ModelOverride> baseOverrides = List.copyOf(itemModel.overrides);
        TrimmableItemModel base = itemModel.copy();
        String equipmentType = getEquipmentType(resource.item());

        baseOverrides.forEach(modelOverride -> {
            for (Map.Entry<ResourceLocation, Float> pattern : TEMPLATE_PATTERN_INDEX_SUPPLIER.get().entrySet()) {
                ResourceLocation id = pattern.getKey();
                if (!hasItemTexture(equipmentType, id)) {
                    continue;
                }

                ResourceLocation modelId = getPatternModelId(modelOverride.model, id);
                ModelOverride override = ModelOverride.builder()
                        .withPredicate(withPatternPredicate(modelOverride.predicate, pattern.getValue()))
                        .withModel(modelId.toString())
                        .build();

                itemModel.addOverride(override);
                generatingModelId = modelId;
                try {
                    overrides.put(modelId, overrideCreator.apply(base, resource));
                } finally {
                    generatingModelId = null;
                }
            }
        });

        return overrides;
    }

    private static boolean hasItemTexture(String equipmentType, ResourceLocation patternId) {
        String path = "assets/minecraft/textures/trims/items/%s/%s.png".formatted(
                equipmentType,
                getPatternTextureName(patternId)
        );
        return DynamicTrimsTrimModelLoaderAdapter.class.getClassLoader().getResource(path) != null;
    }

    private static ResourceLocation getPatternModelId(String baseModelId, ResourceLocation patternId) {
        return ResourceLocation.parse("%s-%s".formatted(baseModelId, getPatternTextureName(patternId)));
    }

    private static String getPatternTextureName(ResourceLocation patternId) {
        return patternId.toString().replace(":", "-");
    }

    private static ResourceLocation getPatternAssetId(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        String itemPath = itemId.getPath();
        String trimTemplateSuffix = "_armor_trim_smithing_template";
        if (!itemPath.endsWith(trimTemplateSuffix)) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(
                itemId.getNamespace(),
                itemPath.substring(0, itemPath.length() - trimTemplateSuffix.length()));
    }

    private static JsonObject withPatternPredicate(JsonObject predicate, float index) {
        JsonObject copy = predicate.deepCopy();
        copy.addProperty(DynamicTrim.TRIM_PATTERN.toString(), index);
        return copy;
    }

    private record TrimInfo(String trimType, String trimPattern) {
        private static final Function<String, Pattern> TRIM_INFO_PATTERN_FUNCTION = Memoizer.memoize(itemId -> Pattern.compile("[^:]+:item/" + itemId + "_(?<trimType>.+)(?=_trim)_trim-(?<trimPattern>.*)"));

        public static TrimInfo fromModelId(Item item, ResourceLocation modelId) {
            if (modelId == null) return null;

            Pattern pattern = TRIM_INFO_PATTERN_FUNCTION.apply(BuiltInRegistries.ITEM.getKey(item).getPath());
            Matcher matcher = pattern.matcher(modelId.toString());
            if (!matcher.find()) {
                RuntimeTrims.LOGGER.debug("Couldn't find match trim info for {}", modelId);
                return null;
            }
            String trimType = matcher.group("trimType");
            String trimPattern = matcher.group("trimPattern");
            return new TrimInfo(trimType, trimPattern);
        }

        public boolean isDynamic() {
            return trimType.equals(RuntimeTrims.DYNAMIC);
        }

        public ResourceLocation getPatternTextureId(String equipmentType) {
            return ResourceLocation.withDefaultNamespace("textures/trims/items/%s/%s.png".formatted(equipmentType, trimPattern));
        }
    }
}
