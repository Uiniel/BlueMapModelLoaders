package me.owies.bluemapmodelloaders.resources;

import com.google.gson.annotations.SerializedName;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import lombok.Getter;
import me.owies.bluemapmodelloaders.resources.objmodel.ObjModel;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

// Code copied and modified from de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model, because the private constructor prevented inheritance
// Copyright (c) Blue <https://www.bluecolored.de>
@Getter
public class ExtendedModel {
    protected @Nullable ResourcePath<ExtendedModel> parent;
    @SerializedName(value = "loader", alternate={"porting_lib:loader"})
    @Nullable protected LoaderType loader;

    // Obj
    @Nullable protected ResourcePath<ObjModel> model;
    protected Map<String, TextureVariable> textures = new HashMap<>();
    protected boolean automatic_culling = true;
    protected boolean shade_quads = false;
    protected boolean flip_v = false;

    public synchronized void optimize(ResourcePack resourcePack, ResourcePath<ObjModel> thisPath) {
        for (var variable : this.textures.values()) {
            variable.optimize(resourcePack);
        }

        // TODO: model.optimize();
    }

    public synchronized void applyParent(ModelLoaderResourcePack resourcePack) {
        if (this.parent == null) return;

        // set parent to null early to avoid trying to resolve reference-loops
        ResourcePath<ExtendedModel> parentPath = this.parent;
        this.parent = null;

        ExtendedModel parent = parentPath.getResource(resourcePack::getExtendedModel);
        if (parent != null) {
            parent.applyParent(resourcePack);

            parent.textures.forEach(this::applyTextureVariable);
            if (this.model == null) {
                this.model = parent.model;
            }
            if (this.loader == null) {
                this.loader = parent.loader;
            }
        }
    }

    private synchronized void applyTextureVariable(String key, TextureVariable value) {
        if (!this.textures.containsKey(key)) {
            this.textures.put(key, value.copy());
        }
    }

    public synchronized void calculateProperties(ResourcePack resourcePack) {
        // TODO
    }
}
