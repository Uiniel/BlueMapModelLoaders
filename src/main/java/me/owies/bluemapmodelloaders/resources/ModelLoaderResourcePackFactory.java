package me.owies.bluemapmodelloaders.resources;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;

public class ModelLoaderResourcePackFactory implements ResourcePack.Extension<ModelLoaderResourcePack> {
    public static final ModelLoaderResourcePackFactory INSTANCE = new ModelLoaderResourcePackFactory();

    @Override
    public ModelLoaderResourcePack create(ResourcePack pack) {
        return new ModelLoaderResourcePack(pack);
    }

    @Override
    public Key getKey() {
        return new Key("bluemapmodelloaders", "resourcepack");
    }
}
