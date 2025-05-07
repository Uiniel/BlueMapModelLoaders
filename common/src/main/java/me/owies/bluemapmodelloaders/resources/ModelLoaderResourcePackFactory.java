package me.owies.bluemapmodelloaders.resources;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtensionType;
import de.bluecolored.bluemap.core.util.Key;

public class ModelLoaderResourcePackFactory implements ResourcePackExtensionType<ModelLoaderResourcePack> {
    public static final ModelLoaderResourcePackFactory INSTANCE = new ModelLoaderResourcePackFactory();

    @Override
    public ModelLoaderResourcePack create() {
        ModelLoaderResourcePack.INSTANCE = new ModelLoaderResourcePack();
        return ModelLoaderResourcePack.INSTANCE;
    }

    @Override
    public Key getKey() {
        return new Key("bluemapmodelloaders", "resourcepack");
    }
}
