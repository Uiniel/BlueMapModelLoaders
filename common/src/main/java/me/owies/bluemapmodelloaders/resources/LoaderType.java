package me.owies.bluemapmodelloaders.resources;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.Keyed;
import de.bluecolored.bluemap.core.util.Registry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.owies.bluemapmodelloaders.renderer.CompositeModelRenderer;
import me.owies.bluemapmodelloaders.renderer.EmptyModelRenderer;
import me.owies.bluemapmodelloaders.renderer.ObjModelRenderer;
import me.owies.bluemapmodelloaders.resources.composite.CompositeModelExtension;
import me.owies.bluemapmodelloaders.resources.empty.EmptyModelExtension;
import me.owies.bluemapmodelloaders.resources.obj.ObjModelExtension;

import java.lang.reflect.Type;
import java.util.Arrays;

@JsonAdapter(LoaderType.Serializer.class)
public interface LoaderType<M extends ModelExtension> extends Keyed {

    LoaderType<ObjModelExtension> OBJ = new Imp<>(
            new Key("bluemapmodelloaders", "obj"),
            ObjModelRenderer.TYPE,
            new String[]{"forge:obj", "neoforge:obj", "porting_lib:obj"},
            ObjModelExtension.class
    );

    LoaderType<EmptyModelExtension> EMPTY = new Imp<>(
            new Key("bluemapmodelloaders", "empty"),
            EmptyModelRenderer.TYPE,
            new String[]{"forge:empty", "neoforge:empty", "porting_lib:empty"},
            EmptyModelExtension.class
    );

    LoaderType<CompositeModelExtension> COMPOSITE = new Imp<>(
            new Key("bluemapmodelloaders", "composite"),
            CompositeModelRenderer.TYPE,
            new String[]{"forge:composite", "neoforge:composite", "porting_lib:composite"},
            CompositeModelExtension.class
    );

    LoaderType<EmptyModelExtension> MISSING_MODEL_LOADER = new Imp<>(new Key("bluemapmodelloaders", "missing_model_loader"),
            BlockRendererType.MISSING,
            new String[]{},
            EmptyModelExtension.class
    );

    Registry<LoaderType<?>> REGISTRY = new Registry<>(
            OBJ,
            EMPTY,
            COMPOSITE,
            MISSING_MODEL_LOADER
    );

    boolean isLoaderFor(JsonElement json, Type typeOfT, JsonDeserializationContext context);

    BlockRendererType getRenderer();
    Class<M> getModelExtensionClass();

    @RequiredArgsConstructor
    class Imp<M extends ModelExtension> implements LoaderType<M> {
        @Getter
        private final Key key;
        @Getter
        private final BlockRendererType renderer;
        @Getter
        private final String[] acceptedLoaderStrings;
        @Getter
        private final Class<M> modelExtensionClass;

        @Override
        public boolean isLoaderFor(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return Arrays.stream(acceptedLoaderStrings).anyMatch(s -> s.equals(json.getAsString()));
        }
    }

    class Serializer implements JsonDeserializer<LoaderType<?>> {

        @Override
        public LoaderType<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            for (LoaderType<?> loaderType: LoaderType.REGISTRY.values()) {
                if (loaderType.isLoaderFor(json, typeOfT, context)) {
                    return loaderType;
                }
            }
            return MISSING_MODEL_LOADER;
        }
    }
}
