package me.owies.bluemapmodelloaders.resources.composite;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import lombok.Getter;
import me.owies.bluemapmodelloaders.resources.ExtendedModel;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePack;

import java.io.IOException;

@JsonAdapter(CompositeChildModel.Adapter.class)
@Getter
public class CompositeChildModel {
    protected Model model;
    protected ExtendedModel extendedModel;

    public static class Adapter extends TypeAdapter<CompositeChildModel> {

        @Override
        public void write(JsonWriter out, CompositeChildModel value) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompositeChildModel read(JsonReader in) throws IOException {
            JsonObject object = ResourcesGson.INSTANCE.fromJson(in, JsonObject.class);

            CompositeChildModel model = new CompositeChildModel();
            model.model = ResourcesGson.INSTANCE.fromJson(object, Model.class);
            model.extendedModel = ResourcesGson.INSTANCE.fromJson(object, ExtendedModel.class);

            return model;
        }
    }
}
