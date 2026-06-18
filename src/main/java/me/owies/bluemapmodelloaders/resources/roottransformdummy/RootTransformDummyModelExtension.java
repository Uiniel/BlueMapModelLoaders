package me.owies.bluemapmodelloaders.resources.roottransformdummy;

import com.flowpowered.math.vector.Vector3f;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.owies.bluemapmodelloaders.Constants;
import me.owies.bluemapmodelloaders.resources.ExtendedModel;
import me.owies.bluemapmodelloaders.resources.LoaderType;
import me.owies.bluemapmodelloaders.resources.ModelExtension;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePack;
import me.owies.bluemapmodelloaders.resources.obj.ObjModelExtension;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.stream.Stream;

@JsonAdapter(RootTransformDummyModelExtension.Adapter.class)
@Getter
@ToString
public class RootTransformDummyModelExtension implements ModelExtension {
    @Nullable
    protected MatrixM4f transform;

    @Nullable @Setter
    protected BlockRendererType originalRenderer;

    @Override
    public void applyParent(ExtendedModel parent) {
        RootTransformDummyModelExtension transparent = parent.getExtension(LoaderType.ROOT_TRANSFORM_DUMMY);

        if (this.transform == null) {
            this.transform = transparent.transform;
        }
    }

    @Override
    public void bake(ResourcePack blueMapResourcePack, ModelLoaderResourcePack modelLoaderResourcePack) {

    }

    @Override
    public Stream<ResourcePath<Texture>> getUsedTextures() {
        return Stream.empty();
    }

    public static class Adapter extends TypeAdapter<RootTransformDummyModelExtension> {

        @Override
        public void write(JsonWriter out, RootTransformDummyModelExtension value) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public RootTransformDummyModelExtension read(JsonReader in) throws IOException {
            RootTransformDummyModelExtension extension = new RootTransformDummyModelExtension();
            extension.transform = null;

            JsonObject model = ResourcesGson.INSTANCE.fromJson(in, JsonObject.class);
            JsonElement transformElement = model.get("transform");

            if (transformElement == null || !transformElement.isJsonObject()) {
                return extension;
            }

            JsonObject transformObject = transformElement.getAsJsonObject();

            MatrixM4f matrix = new MatrixM4f();

            try {
                if (transformObject.has("matrix")) {
                    matrix = getMatrix(transformObject.getAsJsonArray("matrix"));
                } else {
                    Vector3f origin = new Vector3f(1, 1, 1);
                    if (transformObject.has("origin")) {
                        origin = getOrigin(transformObject.get("origin"));
                    }

                    matrix.translate(-origin.getX(), -origin.getY(), -origin.getZ());

                    if (transformObject.has("post_rotation")) {
                        rotate(matrix, transformObject.get("post_rotation"));
                    } else if (transformObject.has("right_rotation")) {
                        rotate(matrix, transformObject.get("right_rotation"));
                    }

                    if (transformObject.has("scale")) {
                        scale(matrix, transformObject.getAsJsonArray("scale"));
                    }

                    if (transformObject.has("rotation")) {
                        rotate(matrix, transformObject.get("rotation"));
                    } else if (transformObject.has("left_rotation")) {
                        rotate(matrix, transformObject.get("left_rotation"));
                    }

                    if (transformObject.has("translation")) {
                        translate(matrix, transformObject.getAsJsonArray("translation"));
                    }

                    matrix.translate(origin.getX(), origin.getY(), origin.getZ());
                }

                extension.transform = matrix;
            } catch (IllegalStateException ex) {
                Constants.LOG.logWarning("Error while parsing root transform: " + ex);
            }

            return extension;
        }

        private MatrixM4f getMatrix(JsonArray mat) {
            MatrixM4f matrix = new MatrixM4f();

            JsonArray mat0 = mat.get(0).getAsJsonArray();
            JsonArray mat1 = mat.get(1).getAsJsonArray();
            JsonArray mat2 = mat.get(2).getAsJsonArray();

            matrix.m00 = mat0.get(0).getAsFloat();
            matrix.m01 = mat0.get(1).getAsFloat();
            matrix.m02 = mat0.get(2).getAsFloat();
            matrix.m03 = mat0.get(3).getAsFloat();

            matrix.m10 = mat1.get(0).getAsFloat();
            matrix.m11 = mat1.get(1).getAsFloat();
            matrix.m12 = mat1.get(2).getAsFloat();
            matrix.m13 = mat1.get(3).getAsFloat();

            matrix.m20 = mat2.get(0).getAsFloat();
            matrix.m21 = mat2.get(1).getAsFloat();
            matrix.m22 = mat2.get(2).getAsFloat();
            matrix.m23 = mat2.get(3).getAsFloat();

            return matrix;
        }

        private Vector3f getOrigin(JsonElement originElement) {
            Vector3f origin = new Vector3f(1, 1, 1);

            if (originElement.isJsonArray()) {
                JsonArray originArray = originElement.getAsJsonArray();
                origin = new Vector3f(
                        originArray.get(0).getAsFloat(),
                        originArray.get(1).getAsFloat(),
                        originArray.get(2).getAsFloat()
                );
            } else {
                String originString = originElement.getAsString();
                if (originString.equals("corner")) {
                    origin = new Vector3f(0, 0, 0);
                } else if (originString.equals("center")) {
                    origin = new Vector3f(0.5, 0.5, 0.5);
                }
            }
            return origin;
        }

        private void translate(MatrixM4f matrix, JsonArray translationArray) {
            matrix.translate(
                    translationArray.get(0).getAsFloat(),
                    translationArray.get(1).getAsFloat(),
                    translationArray.get(2).getAsFloat()
            );
        }

        private void rotate(MatrixM4f matrix, JsonElement rotationElement) {
            if (rotationElement.isJsonObject()) {
                rotateByOneAxis(matrix, rotationElement.getAsJsonObject());
            } else if (rotationElement.isJsonArray()) {
                JsonArray rotationArray = rotationElement.getAsJsonArray();
                if (rotationArray.isEmpty()) {
                    return;
                }

                if (rotationArray.get(0).isJsonObject()) {
                    for (int i = rotationArray.size() - 1; i >= 0; i--) {
                        rotateByOneAxis(matrix, rotationArray.get(i).getAsJsonObject());
                    }
                } else if (rotationArray.get(0).isJsonPrimitive()) {
                    if (rotationArray.size() == 3) {
                        matrix.rotateXYZ(
                                rotationArray.get(0).getAsFloat(),
                                rotationArray.get(1).getAsFloat(),
                                rotationArray.get(2).getAsFloat()
                        );
                    } else if (rotationArray.size() == 4) {
                        matrix.rotateByQuaternion(
                                rotationArray.get(0).getAsFloat(),
                                rotationArray.get(1).getAsFloat(),
                                rotationArray.get(2).getAsFloat(),
                                rotationArray.get(4).getAsFloat()
                        );
                    }
                }
            }
        }

        private void rotateByOneAxis(MatrixM4f matrix, JsonObject rotationObject) {
            if (rotationObject.has("x")) {
                matrix.rotateXYZ(rotationObject.get("x").getAsFloat(), 0, 0);
            } else if (rotationObject.has("y")) {
                matrix.rotateXYZ(0, rotationObject.get("y").getAsFloat(), 0);
            } else if (rotationObject.has("z")) {
                matrix.rotateXYZ(0, 0, rotationObject.get("z").getAsFloat());
            }
        }

        private void scale(MatrixM4f matrix, JsonArray scaleArray) {
            matrix.scale(
                    scaleArray.get(0).getAsFloat(),
                    scaleArray.get(1).getAsFloat(),
                    scaleArray.get(2).getAsFloat()
            );
        }
    }
}
