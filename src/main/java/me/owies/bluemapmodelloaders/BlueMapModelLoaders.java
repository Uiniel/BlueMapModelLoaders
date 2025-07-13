package me.owies.bluemapmodelloaders;

import com.technicjelle.BMUtils.BMNative.BMNLogger;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import me.owies.bluemapmodelloaders.renderer.CompositeModelRenderer;
import me.owies.bluemapmodelloaders.renderer.EmptyModelRenderer;
import me.owies.bluemapmodelloaders.renderer.ObjModelRenderer;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePackFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class BlueMapModelLoaders implements Runnable {

    public void run() {
        try {
            Constants.LOG = new BMNLogger(this.getClass().getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        addBluemapRegistryValues();
        Constants.LOG.logInfo("BlueMapModelLoaders loaded.");
    }

    private void addBluemapRegistryValues() {
        ResourcePack.Extension.REGISTRY.register(ModelLoaderResourcePackFactory.INSTANCE);
        BlockRendererType.REGISTRY.register(ObjModelRenderer.TYPE);
        BlockRendererType.REGISTRY.register(EmptyModelRenderer.TYPE);
        BlockRendererType.REGISTRY.register(CompositeModelRenderer.TYPE);
    }
}
