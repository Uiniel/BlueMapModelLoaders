package me.owies.bluemapmodelloaders.resources.obj;

import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import lombok.Getter;
import me.owies.bluemapmodelloaders.resources.ExtendedModel;
import me.owies.bluemapmodelloaders.resources.LoaderType;
import me.owies.bluemapmodelloaders.resources.ModelExtension;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePack;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

@Getter
public class ObjModelExtension implements ModelExtension {
    @Nullable
    protected ResourcePath<ObjModel> model;
    @Nullable
    protected Boolean automatic_culling = true;
    @Nullable
    protected Boolean shade_quads = true;
    @Nullable
    protected Boolean flip_v = false;

    @Override
    public synchronized void applyParent(ExtendedModel parent) {
        ObjModelExtension parentObj = parent.getExtension(LoaderType.OBJ);

        if (this.model == null) {
            this.model = parentObj.model;
        }

        if (this.automatic_culling == null) {
            this.automatic_culling = parentObj.automatic_culling;
        }

        if (this.shade_quads == null) {
            this.shade_quads = parentObj.shade_quads;
        }

        if (this.flip_v == null) {
            this.flip_v = parentObj.flip_v;
        }
    }

    @Override
    public void bake(ResourcePack blueMapResourcePack, ModelLoaderResourcePack modelLoaderResourcePack) {

    }

    @Override
    public Stream<ResourcePath<Texture>> getUsedTextures() {
        return Stream.empty();
    }
}
