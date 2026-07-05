package me.noramibu.dynamictrim.runtime.client.model.item.json;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BlockAtlas {
    public List<Source> sources;

    public Optional<Source> getPalettedPermutationsSource(String paletteKey) {
        for(Source source : sources) {
            if(idMatches(source.type, "paletted_permutations") && idMatches(source.paletteKey, paletteKey)) {
                return Optional.of(source);
            }
        }
        return Optional.empty();
    }

    private static boolean idMatches(String actual, String expected) {
        return actual != null && (actual.equals(expected) || actual.equals("minecraft:" + expected));
    }

    public void addSource(Source source) {
        sources.add(source);
    }

    public static class Source {
        public String type;
        public String source;
        public String prefix;
        public String resource;
        public JsonElement textures;
        public List<String> directories;
        public String paletteKey;
        public Map<String, String> permutations;

        public Source withType(String type) {
            this.type = type;
            return this;
        }

        public Source withSource(String source) {
            this.source = source;
            return this;
        }

        public Source withPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Source withResource(String resource) {
            this.resource = resource;
            return this;
        }

        public Source withTextures(JsonElement textures) {
            this.textures = textures;
            return this;
        }

        public Source withDirectories(List<String> directories) {
            this.directories = directories;
            return this;
        }

        public Source withPaletteKey(String paletteKey) {
            this.paletteKey = paletteKey;
            return this;
        }

        public Source withPermutations(Map<String, String> permutations) {
            this.permutations = permutations;
            return this;
        }

        public Source copy() {
            return new Source()
                    .withType(type)
                    .withSource(source)
                    .withPrefix(prefix)
                    .withResource(resource)
                    .withTextures(textures)
                    .withDirectories(directories)
                    .withPaletteKey(paletteKey)
                    .withPermutations(permutations);
        }
    }
}
