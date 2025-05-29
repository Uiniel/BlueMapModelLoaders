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
    protected boolean automatic_culling = true;
    protected boolean shade_quads = true;
    protected boolean flip_v = false;

    @Override
    public synchronized void applyParent(ExtendedModel parent) {
        ObjModelExtension parentObj = (ObjModelExtension) parent.getExtension(LoaderType.OBJ);

        if (this.model == null) {
            this.model = parentObj.model;
        }
    }

    @Override
    public void bake(ResourcePack blueMapResourcePack, ModelLoaderResourcePack modelLoaderResourcePack) {

    }
}
