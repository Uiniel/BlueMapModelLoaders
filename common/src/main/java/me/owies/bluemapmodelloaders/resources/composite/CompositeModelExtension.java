package me.owies.bluemapmodelloaders.resources.composite;

import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import lombok.Getter;
import me.owies.bluemapmodelloaders.resources.ExtendedModel;
import me.owies.bluemapmodelloaders.resources.LoaderType;
import me.owies.bluemapmodelloaders.resources.ModelExtension;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class CompositeModelExtension implements ModelExtension {
    protected Map<String, CompositeChildModel> children = new HashMap<>();
    protected Map<String, Boolean> visibility = new HashMap<>();

    @Override
    public void applyParent(ExtendedModel parent) {
        CompositeModelExtension parentModel = parent.getExtension(LoaderType.COMPOSITE);

        for (Map.Entry<String, CompositeChildModel> entry : parentModel.children.entrySet()) {
            if (!children.containsKey(entry.getKey())) {
                children.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<String, Boolean> entry : parentModel.visibility.entrySet()) {
            if (!visibility.containsKey(entry.getKey())) {
                visibility.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void bake(ResourcePack blueMapResourcePack, ModelLoaderResourcePack modelLoaderResourcePack) {
        children = children
                .entrySet()
                .stream()
                .filter(e -> !visibility.containsKey(e.getKey()) || visibility.get(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        children.forEach((c, child) -> {
            child.model.optimize(blueMapResourcePack);
        });
        children.forEach((c, child) -> {
            child.model.applyParent(blueMapResourcePack);
            child.extendedModel.applyParent(modelLoaderResourcePack);
        });
        children.forEach((c, child) -> {
            child.model.calculateProperties(blueMapResourcePack);
            child.extendedModel.bake(blueMapResourcePack, modelLoaderResourcePack);
        });
    }

    @Override
    public Stream<ResourcePath<Texture>> getUsedTextures() {
        return children
                .values()
                .stream()
                .flatMap(child ->
                        Stream.concat(
                                child.extendedModel.getUsedTextures(),
                                child.model
                                        .getTextures()
                                        .values()
                                        .stream()
                                        .map(TextureVariable::getTexturePath)
                                        .filter(Objects::nonNull)
                        )
                );
    }
}
