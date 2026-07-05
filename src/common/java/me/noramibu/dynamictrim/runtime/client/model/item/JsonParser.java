package me.noramibu.dynamictrim.runtime.client.model.item;

import me.noramibu.dynamictrim.runtime.client.model.item.json.BlockAtlas;
import me.noramibu.dynamictrim.runtime.client.model.item.json.TextureLayers;
import me.noramibu.dynamictrim.runtime.client.model.item.json.serialisation.TextureLayersSerializer;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;

public final class JsonParser {
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(TextureLayers.class, new TextureLayersSerializer())
            .setPrettyPrinting()
            .create();

    public <T> T fromResource(Resource resource, Class<T> clazz) {
        try(BufferedReader reader = resource.openAsReader()) {
            return fromReader(reader, clazz);
        } catch (IOException e) {
            return null;
        }
    }

    public <T> T fromReader(Reader original, Class<T> clazz) {
        return GSON.fromJson(original, clazz);
    }

    public <T> T fromJson(JsonObject json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public Resource toResource(PackResources resourcePack, Object object) {
        return new Resource(resourcePack, () -> IOUtils.toInputStream(GSON.toJson(object), StandardCharsets.UTF_8));
    }

    public BufferedReader toReader(Object object) {
        return new BufferedReader(new StringReader(GSON.toJson(object)));
    }
}
