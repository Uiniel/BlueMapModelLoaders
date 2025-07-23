package me.owies.bluemapmodelloaders.resources;

import de.bluecolored.bluemap.core.BlueMap;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.Pack;
import de.bluecolored.bluemap.core.resources.pack.ResourcePool;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import lombok.Getter;
import me.owies.bluemapmodelloaders.Constants;
import me.owies.bluemapmodelloaders.resources.obj.ObjMaterialLibrary;
import me.owies.bluemapmodelloaders.resources.obj.ObjModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ModelLoaderResourcePack implements ResourcePackExtension {
    private ResourcePack blueMapResourcePack;

    @Getter
    private final ResourcePool<ExtendedModel> models;
    @Getter
    private final ResourcePool<ObjModel> objModels;
    @Getter
    private final ResourcePool<ObjMaterialLibrary> mtlLibraries;

    public ModelLoaderResourcePack(ResourcePack blueMapResourcePack) {
        this.models = new ResourcePool<>();
        this.objModels = new ResourcePool<>();
        this.mtlLibraries = new ResourcePool<>();
        this.blueMapResourcePack = blueMapResourcePack;
    }

    @Override
    public void loadResources(Iterable<Path> roots) throws IOException, InterruptedException {
        for (Path root : roots) {
            blueMapResourcePack.loadResourcePath(root, this::loadResourcesFromPath);
        }
    }

    public void loadResourcesFromPath(Path root) throws IOException {

        try {
            CompletableFuture.allOf(
                    // load model extensions
                    CompletableFuture.runAsync(() -> {
                        ResourcePack.list(root.resolve("assets"))
                                .map(path -> path.resolve("models"))
                                .flatMap(ResourcePack::list)
                                .filter(path -> !path.getFileName().toString().equals("item"))
                                .flatMap(ResourcePack::walk)
                                .filter(path -> path.getFileName().toString().endsWith(".json"))
                                .filter(Files::isRegularFile)
                                .forEach(file -> models.load(
                                        new ResourcePath<>(root.relativize(file), 1, 3),
                                        key -> {
                                            try (BufferedReader reader = Files.newBufferedReader(file)) {
                                                return ResourcesGson.INSTANCE.fromJson(reader, ExtendedModel.class);
                                            }
                                        }
                                ));
                    }, BlueMap.THREAD_POOL),

                    // load .obj models
                    CompletableFuture.runAsync(() -> {
                        ResourcePack.list(root.resolve("assets"))
                                .map(path -> path.resolve("models"))
                                .flatMap(ResourcePack::list)
                                .filter(path -> !path.getFileName().toString().equals("item"))
                                .flatMap(ResourcePack::walk)
                                .filter(path -> path.getFileName().toString().endsWith(".obj"))
                                .filter(Files::isRegularFile)
                                .forEach(file -> objModels.load(
                                        new ResourcePath<>(root.relativize(file.resolveSibling(file.getFileName() + ".")), 1, 2),
                                        key -> {
                                            try (BufferedReader reader = Files.newBufferedReader(file)) {
                                                return ObjModel.fromReader(reader, root.relativize(file), 1, 2);
                                            }
                                        }
                                ));
                    }, BlueMap.THREAD_POOL),

                    // load .mtl models
                    CompletableFuture.runAsync(() -> {
                        ResourcePack.list(root.resolve("assets"))
                                .map(path -> path.resolve("models"))
                                .flatMap(ResourcePack::list)
                                .filter(path -> !path.getFileName().toString().equals("item"))
                                .flatMap(ResourcePack::walk)
                                .filter(path -> path.getFileName().toString().endsWith(".mtl"))
                                .filter(Files::isRegularFile)
                                .forEach(file -> mtlLibraries.load(
                                        new ResourcePath<>(root.relativize(file.resolveSibling(file.getFileName() + ".")), 1, 2),
                                        key -> {
                                            try (BufferedReader reader = Files.newBufferedReader(file)) {
                                                return ObjMaterialLibrary.fromReader(reader);
                                            }
                                        }
                                ));
                    }, BlueMap.THREAD_POOL)
            ).join();
        } catch (RuntimeException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof IOException) throw (IOException) cause;
            if (cause != null) throw new IOException(cause);
            throw new IOException(ex);
        }
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        HashSet<Key> keys = new HashSet<>();
        keys.add(new ResourcePath<Texture>("bluemapmodelloaders:block/white"));
        models
                .values()
                .stream()
                .flatMap(ExtendedModel::getUsedTextures)
                .forEach(keys::add);

        return keys;
    }

    @Override
    public void bake() throws IOException {
        models.values().forEach(model -> model.applyParent(this));
        models.values().forEach(model -> model.bake(blueMapResourcePack, this));

        blueMapResourcePack
                .getBlockStates()
                .values()
                .forEach(blockState -> {
                    blockState.forEach(variant -> {
                        ExtendedModel model = models.get(variant.getModel());
                        if (model == null) return;

                        LoaderType<?> loader = model.loader;
                        if (loader != null) {
                            variant.setRenderer(loader.getRenderer());
                            Constants.LOG.logDebug("CustomRenderer found: " + variant.getModel().getFormatted());
                        }
                    });
                });
    }
}
