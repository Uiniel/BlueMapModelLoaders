package me.owies.bluemapmodelloaders.resources;


import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;

public interface ModelExtension {

    void applyParent(ExtendedModel parent);
    void bake(ResourcePack blueMapResourcePack, ModelLoaderResourcePack modelLoaderResourcePack);
}
