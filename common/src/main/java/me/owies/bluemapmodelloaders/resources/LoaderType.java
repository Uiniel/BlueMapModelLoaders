package me.owies.bluemapmodelloaders.resources;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.Keyed;
import de.bluecolored.bluemap.core.util.Registry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.owies.bluemapmodelloaders.renderer.ObjModelRenderer;

import java.lang.reflect.Type;
import java.util.Arrays;

@JsonAdapter(LoaderType.Serializer.class)
public interface LoaderType extends Keyed {

    LoaderType OBJ = new Imp(
            new Key("bluemapmodelloaders", "obj"),
            ObjModelRenderer.OBJ,
            new String[]{"forge:obj", "neoforge:obj", "porting_lib:obj"}
    );

    LoaderType MISSING_MODEL_LOADER = new Imp(new Key("bluemapmodelloaders", "missing_model_loader"),
            BlockRendererType.MISSING,
            new String[]{}
    );

    Registry<LoaderType> REGISTRY = new Registry<>(
            OBJ
    );

    boolean isLoaderFor(JsonElement json, Type typeOfT, JsonDeserializationContext context);

    BlockRendererType getRenderer();

    @RequiredArgsConstructor
    class Imp implements LoaderType {
        @Getter
        private final Key key;
        @Getter
        private final BlockRendererType renderer;
        @Getter
        private final String[] acceptedLoaderStrings;

        @Override
        public boolean isLoaderFor(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return Arrays.stream(acceptedLoaderStrings).anyMatch(s -> s.equals(json.getAsString()));
        }
    }

    class Serializer implements JsonSerializer<LoaderType>, JsonDeserializer<LoaderType> {

        @Override
        public LoaderType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            for (LoaderType loaderType: LoaderType.REGISTRY.values()) {
                if (loaderType.isLoaderFor(json, typeOfT, context)) {
                    return loaderType;
                }
            }
            return MISSING_MODEL_LOADER;
        }

        @Override
        public JsonElement serialize(LoaderType src, Type typeOfSrc, JsonSerializationContext context) {
            return null;
        }
    }
}
