package me.noramibu.dynamictrim.runtime.client.model.item;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.noramibu.dynamictrim.runtime.RuntimeTrims;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public record GroupPermutationsAtlasSource(
        List<Identifier> directories,
        Identifier paletteKey,
        Map<String, Identifier> permutations)
        implements SpriteSource {
    public static final MapCodec<GroupPermutationsAtlasSource> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.list(Identifier.CODEC)
                            .fieldOf("directories")
                            .forGetter(GroupPermutationsAtlasSource::directories),
                    Identifier.CODEC.fieldOf("palette_key")
                            .forGetter(GroupPermutationsAtlasSource::paletteKey),
                    Codec.unboundedMap(Codec.STRING, Identifier.CODEC)
                            .fieldOf("permutations")
                            .forGetter(GroupPermutationsAtlasSource::permutations)
            ).apply(instance, GroupPermutationsAtlasSource::new));

    public GroupPermutationsAtlasSource {
        directories = List.copyOf(directories);
        permutations = addBlankPermutation(permutations);
    }

    public static void init() {
        // no-op
    }

    private static Map<String, Identifier> addBlankPermutation(Map<String, Identifier> permutations) {
        return ImmutableMap.<String, Identifier>builder()
                .putAll(permutations)
                .put(RuntimeTrims.DYNAMIC, Identifier.withDefaultNamespace("trims/color_palettes/%s".formatted(RuntimeTrims.DYNAMIC)))
                .build();
    }

    @Override
    public void run(ResourceManager resourceManager, Output regions) {
        List<Identifier> combinedTextures = new ArrayList<>();
        for (Identifier dir : directories) {
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
                        Identifier id = Identifier.fromNamespaceAndPath(
                                identifier.getNamespace(),
                                dir.getPath() + "/" + texturePath
                        );
                        combinedTextures.add(id);
                        for (int i = 0; i < 8; i++) {
                            combinedTextures.add(id.withSuffix("_" + i));
                        }
                    });
        }

        new PalettedPermutations(combinedTextures, paletteKey, permutations).run(resourceManager, regions);
    }

    @Override
    public MapCodec<GroupPermutationsAtlasSource> codec() {
        return CODEC;
    }
}
