package me.owies.bluemapmodelloaders;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtensionType;
import me.owies.bluemapmodelloaders.renderer.CompositeModelRenderer;
import me.owies.bluemapmodelloaders.renderer.EmptyModelRenderer;
import me.owies.bluemapmodelloaders.renderer.ObjModelRenderer;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePackFactory;

public class CommonClass {

    public static void init() {

        addBluemapRegistryValues();
    }

    private static void addBluemapRegistryValues() {
        ResourcePackExtensionType.REGISTRY.register(ModelLoaderResourcePackFactory.INSTANCE);
        BlockRendererType.REGISTRY.register(ObjModelRenderer.TYPE);
        BlockRendererType.REGISTRY.register(EmptyModelRenderer.TYPE);
        BlockRendererType.REGISTRY.register(CompositeModelRenderer.TYPE);
    }
}
