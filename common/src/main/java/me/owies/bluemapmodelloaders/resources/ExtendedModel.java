package me.owies.bluemapmodelloaders.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ExtendedModel {
    protected @Nullable ResourcePath<ExtendedModel> parent;
    @SerializedName(value = "loader", alternate = {"porting_lib:loader"})
    @Nullable
    protected LoaderType loader;

    protected Map<LoaderType, ModelExtension> extensions;

    public static ExtendedModel fromJson(Reader json) throws IOException {
        ExtendedModel extendedModel = new ExtendedModel();

        JsonObject model = JsonParser.parseReader(json).getAsJsonObject();
        JsonElement loaderElement = model.get("loader");

        if (loaderElement == null) {
            loaderElement = model.get("porting_lib:loader");
        }
        if (loaderElement != null) {
            extendedModel.loader = ResourcesGson.INSTANCE.fromJson(loaderElement, LoaderType.class);
        }

        JsonElement parentElement = model.get("parent");

        if (parentElement != null) {
            extendedModel.parent = ResourcesGson.INSTANCE.fromJson(loaderElement, ResourcePath.class);
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

            extensions.values().forEach(ext -> ext.applyParent(parent));
        }
    }

    public ModelExtension getExtension(LoaderType loaderType) {
        return extensions.get(loaderType);
    }
}
