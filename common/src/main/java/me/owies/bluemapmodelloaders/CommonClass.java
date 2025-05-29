package me.owies.bluemapmodelloaders;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtensionType;
import me.owies.bluemapmodelloaders.renderer.ObjModelRenderer;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePackFactory;

public class CommonClass {

    public static void init() {

        addBluemapRegistryValues();
    }

    private static void addBluemapRegistryValues() {
        ResourcePackExtensionType.REGISTRY.register(ModelLoaderResourcePackFactory.INSTANCE);
        BlockRendererType.REGISTRY.register(ObjModelRenderer.INSTANCE);
    }
}
