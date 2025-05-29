package me.owies.bluemapmodelloaders.resources;

import de.bluecolored.bluemap.core.BlueMap;
import de.bluecolored.bluemap.core.logger.Logger;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.Pack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import lombok.Getter;
import me.owies.bluemapmodelloaders.mixin.ResourcePackAccessorMixin;
import me.owies.bluemapmodelloaders.mixin.VariantMixin;
import me.owies.bluemapmodelloaders.resources.obj.ObjMaterialLibrary;
import me.owies.bluemapmodelloaders.resources.obj.ObjModel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ModelLoaderResourcePack extends Pack implements ResourcePackExtension {
    public static ResourcePack BLUEMAP_RESOURCE_PACK;
    public static ModelLoaderResourcePack INSTANCE;

    @Getter private final Map<ResourcePath<ExtendedModel>, ExtendedModel> models;
    @Getter private final Map<ResourcePath<ObjModel>, ObjModel> objModels;
    @Getter private final Map<ResourcePath<ObjMaterialLibrary>, ObjMaterialLibrary> mtlLibraries;

    public ModelLoaderResourcePack() {
        super(-1);
        this.models = new HashMap<>();
        this.objModels = new HashMap<>();
        this.mtlLibraries = new HashMap<>();
    }

    @Override
    public void loadResources(Path root) throws IOException {
        try {
            CompletableFuture.allOf(
                    // load model extensions
                    CompletableFuture.runAsync(() -> {
                        list(root.resolve("assets"))
                                .map(path -> path.resolve("models"))
                                .flatMap(ResourcePack::list)
                                .filter(path -> !path.getFileName().toString().equals("item"))
                                .flatMap(ResourcePack::walk)
                                .filter(path -> path.getFileName().toString().endsWith(".json"))
                                .filter(Files::isRegularFile)
                                .forEach(file -> loadResource(root, file, 1, 3, key -> {
                                    try (BufferedReader reader = Files.newBufferedReader(file)) {
                                        return ResourcesGson.INSTANCE.fromJson(reader, ExtendedModel.class);
                                    }
                                }, models));
                    }, BlueMap.THREAD_POOL),

                    // load .obj models
                    CompletableFuture.runAsync(() -> {
                        list(root.resolve("assets"))
                                .map(path -> path.resolve("models"))
                                .flatMap(ResourcePack::list)
                                .filter(path -> !path.getFileName().toString().equals("item"))
                                .flatMap(ResourcePack::walk)
                                .filter(path -> path.getFileName().toString().endsWith(".obj"))
                                .filter(Files::isRegularFile)
                                .forEach(file -> loadResource(root, file, 1, 2, false, key -> {
                                    try (BufferedReader reader = Files.newBufferedReader(file)) {
                                        return ObjModel.fromReader(reader, root.relativize(file), 1, 2);
                                    }
                                }, objModels));
                    }, BlueMap.THREAD_POOL),

                    // load .mtl models
                    CompletableFuture.runAsync(() -> {
                        list(root.resolve("assets"))
                                .map(path -> path.resolve("models"))
                                .flatMap(ResourcePack::list)
                                .filter(path -> !path.getFileName().toString().equals("item"))
                                .flatMap(ResourcePack::walk)
                                .filter(path -> path.getFileName().toString().endsWith(".mtl"))
                                .filter(Files::isRegularFile)
                                .forEach(file -> loadResource(root, file, 1, 2, false, key -> {
                                    try (BufferedReader reader = Files.newBufferedReader(file)) {
                                        return ObjMaterialLibrary.fromReader(reader);
                                    }
                                }, mtlLibraries));
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
    public Iterable<Texture> loadTextures(Path root) throws IOException {
        List<Texture> textures = new ArrayList<>();

        Path white_texture_path = root.resolve("assets/bluemapmodelloaders/textures/block/white.png");
        if (Files.exists(white_texture_path)) {
            BufferedImage image;
            try (InputStream in = Files.newInputStream(white_texture_path)) {
                image = ImageIO.read(in);
            }

            textures.add(Texture.from(new ResourcePath<>("bluemapmodelloaders:block/white"), image, null));
        }

        return textures;
    }

    @Override
    public void bake() throws IOException {
        models.values().forEach(model -> model.applyParent(this));
        models.values().forEach(model -> model.bake(BLUEMAP_RESOURCE_PACK, this));

        ((ResourcePackAccessorMixin) BLUEMAP_RESOURCE_PACK)
                .getBlockStates()
                .values()
                .stream()
                .flatMap(blockState -> {
                    Stream<VariantSet> variants = Stream.empty();
                    if (blockState.getVariants() != null) {
                        variants = Arrays.stream(blockState.getVariants().getVariants());
                    }
                    Stream<VariantSet> multipart = Stream.empty();
                    if (blockState.getMultipart() != null) {
                        multipart = Arrays.stream(blockState.getMultipart().getParts());
                    }
                    return Stream.concat(variants, multipart);
                })
                .flatMap(v -> Arrays.stream(v.getVariants()))
                .forEach(variant -> {
                    ExtendedModel model = models.get(variant.getModel());
                    if (model == null) return;

                    LoaderType loader = model.loader;
                    if (loader != null) {
                        ((VariantMixin) variant).setRenderer(loader.getRenderer());
                    }
                });
    }

    protected <T> void loadResource(Path root, Path file, int namespacePos, int valuePos,
                                    boolean removeEnding, Loader<T> loader, Map<? super ResourcePath<T>, T> resultMap) {
        if (removeEnding) {
            loadResource(root, file, namespacePos, valuePos, loader, resultMap);
        } else {
            try {
                String ending = "";
                String filename = file.getFileName().toString();
                int dotIndex = filename.lastIndexOf('.');
                if (dotIndex != -1) ending = filename.substring(dotIndex);
                ResourcePath<T> resourcePath = new ResourcePath<>(root.relativize(file), namespacePos, valuePos);
                resourcePath = new ResourcePath<>(resourcePath.getFormatted() + ending);

                if (resultMap.containsKey(resourcePath)) return; // don't load already present resources

                T resource = loader.load(resourcePath);
                if (resource == null) return; // don't load missing resources

                resourcePath.setResource(resource);
                resultMap.put(resourcePath, resource);
            } catch (Exception ex) {
                Logger.global.logDebug("Failed to parse resource-file '" + file + "': " + ex);
            }
        }
    }

    @Override
    public void loadResources(Iterable<Path> roots) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }
}
