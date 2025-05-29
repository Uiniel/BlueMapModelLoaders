package me.owies.bluemapmodelloaders.resources;


import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;

import java.util.stream.Stream;

public interface ModelExtension {

    void applyParent(ExtendedModel parent);

    void bake(ResourcePack blueMapResourcePack, ModelLoaderResourcePack modelLoaderResourcePack);

    default Stream<ResourcePath<Texture>> getUsedTextures() {
        return Stream.empty();
    };
}
