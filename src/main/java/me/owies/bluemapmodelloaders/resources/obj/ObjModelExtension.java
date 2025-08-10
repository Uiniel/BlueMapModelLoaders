package me.owies.bluemapmodelloaders.resources.obj;

import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import lombok.Getter;
import me.owies.bluemapmodelloaders.resources.ExtendedModel;
import me.owies.bluemapmodelloaders.resources.LoaderType;
import me.owies.bluemapmodelloaders.resources.ModelExtension;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePack;
import org.jetbrains.annotations.Nullable;

@Getter
public class ObjModelExtension implements ModelExtension {
    @Nullable
    protected ResourcePath<ObjModel> model;
    @Nullable
    protected Boolean automatic_culling;
    @Nullable
    protected Boolean shade_quads;
    @Nullable
    protected Boolean flip_v;

    public boolean isAutomaticCulling() {
        if (automatic_culling == null) return true;
        return automatic_culling;
    }

    public boolean isShadeQuads() {
        if (shade_quads == null) return true;
        return shade_quads;
    }

    public boolean isFlipV() {
        if (flip_v == null) return false;
        return flip_v;
    }

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

}
