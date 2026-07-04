package me.noramibu.dynamictrim.runtime.client.model.item;

import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import me.noramibu.dynamictrim.runtime.client.mixin.accessor.PalettedPermutationsAtlasSourceAccessor;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class GroupPermutationsAtlasSource extends PalettedPermutations {
    public static final MapCodec<GroupPermutationsAtlasSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.list(ResourceLocation.CODEC)
                            .fieldOf("directories")
                            .forGetter(source -> ((PalettedPermutationsAtlasSourceAccessor) source).getTextures()),
                    ResourceLocation.CODEC.fieldOf("palette_key")
                            .forGetter(source -> ((PalettedPermutationsAtlasSourceAccessor) source).getPaletteKey()),
                    Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
                            .fieldOf("permutations")
                            .forGetter(source -> ((PalettedPermutationsAtlasSourceAccessor) source).getPermutations())
            ).apply(instance, GroupPermutationsAtlasSource::new));

    public static SpriteSourceType TYPE;

    private GroupPermutationsAtlasSource(List<ResourceLocation> directories, ResourceLocation paletteKey, Map<String, ResourceLocation> permutations) {
        super(new ArrayList<>(directories), paletteKey, addBlankPermutation(permutations));
    }

    public static void init() {
        // no-op
    }

    private static Map<String, ResourceLocation> addBlankPermutation(Map<String, ResourceLocation> permutations) {
        return ImmutableMap.<String, ResourceLocation>builder()
                .putAll(permutations)
                .put(RuntimeTrims.DYNAMIC, ResourceLocation.withDefaultNamespace("trims/color_palettes/%s".formatted(RuntimeTrims.DYNAMIC)))
                .build();
    }

    @Override
    public void run(ResourceManager resourceManager, Output regions) {
        List<ResourceLocation> combinedTextures = new ArrayList<>();
        List<ResourceLocation> originalTextures = ((PalettedPermutationsAtlasSourceAccessor) this).getTextures();
        for (ResourceLocation dir : originalTextures) {
            String textureDirectory = "textures/" + dir.getPath();
            String childTextureDirectory = textureDirectory + "/";
            resourceManager.listResources(textureDirectory, id -> {
                        String path = id.getPath();
                        return path.startsWith(childTextureDirectory) && path.endsWith(".png");
                    })
                    .forEach((identifier, resource) -> {
                        String path = identifier.getPath();
                        if (!path.startsWith(childTextureDirectory)) {
                            return;
                        }
                        String texturePath = path.substring(
                                childTextureDirectory.length(),
                                path.length() - ".png".length()
                        );
                        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                                identifier.getNamespace(),
                                dir.getPath() + "/" + texturePath
                        );
                        combinedTextures.add(id);
                        for (int i = 0; i < 8; i++) {
                            combinedTextures.add(id.withSuffix("_" + i));
                        }
                    });
        }
        originalTextures.clear();
        originalTextures.addAll(combinedTextures);
        super.run(resourceManager, regions);
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }
}
