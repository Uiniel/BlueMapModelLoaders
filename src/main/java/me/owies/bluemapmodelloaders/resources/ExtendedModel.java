package me.owies.bluemapmodelloaders.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import lombok.Getter;
import lombok.ToString;
import me.owies.bluemapmodelloaders.Constants;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonAdapter(ExtendedModel.Adapter.class)
@Getter
@ToString
public class ExtendedModel {
    protected @Nullable ResourcePath<ExtendedModel> parent;
    @SerializedName(value = "loader", alternate = {"porting_lib:loader"})
    @Nullable
    protected LoaderType<?> loader;

    protected Map<LoaderType<?>, ModelExtension> extensions;

    public void bake(ResourcePack blueMapResourcePack, ModelLoaderResourcePack modelLoaderResourcePack) {
        extensions.values().forEach(ext -> ext.bake(blueMapResourcePack, modelLoaderResourcePack));
    }

    public void applyParent(ModelLoaderResourcePack resourcePack) {
        if (this.parent == null) return;

        // set parent to null early to avoid trying to resolve reference-loops
        ResourcePath<ExtendedModel> parentPath = this.parent;
        this.parent = null;

        ExtendedModel parent = parentPath.getResource(resourcePack.getModels()::get);
        if (parent != null) {
            parent.applyParent(resourcePack);

            if (loader == null) {
                loader = parent.loader;
            }

            extensions.values().forEach(ext -> ext.applyParent(parent));
        }
    }

    public Stream<ResourcePath<Texture>> getUsedTextures() {
        return extensions.values().stream().flatMap(ModelExtension::getUsedTextures);
    }

    public <M extends ModelExtension> M getExtension(LoaderType<M> loaderType) {
        return (M) extensions.get(loaderType);
    }

    public static class Adapter extends TypeAdapter<ExtendedModel> {

        @Override
        public void write(JsonWriter out, ExtendedModel value) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public ExtendedModel read(JsonReader in) throws IOException {
            ExtendedModel extendedModel = new ExtendedModel();

            JsonObject model = ResourcesGson.INSTANCE.fromJson(in, JsonObject.class);
            JsonElement loaderElement = model.get("loader");

            if (loaderElement == null) {
                loaderElement = model.get("porting_lib:loader");
            }
            if (loaderElement != null) {
                extendedModel.loader = ResourcesGson.INSTANCE.fromJson(loaderElement, LoaderType.class);
                if (extendedModel.loader == LoaderType.MISSING_MODEL_LOADER) {
                    extendedModel.loader = null; // Make sure not to overwrite renderers that might be set by other addons later on
                }
            }

            JsonElement parentElement = model.get("parent");

            if (parentElement != null) {
                extendedModel.parent = ResourcesGson.INSTANCE.fromJson(parentElement, new TypeToken<ResourcePath<ExtendedModel>>() {}.getType());
            }

            extendedModel.extensions = LoaderType.REGISTRY
                    .values()
                    .stream().
                    collect(Collectors.toMap(
                            loaderType -> loaderType,
                            loaderType -> ResourcesGson.INSTANCE.fromJson(model, loaderType.getModelExtensionClass())
                    ));

            return extendedModel;
        }
    }
}
