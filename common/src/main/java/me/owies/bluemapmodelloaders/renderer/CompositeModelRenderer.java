package me.owies.bluemapmodelloaders.renderer;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import me.owies.bluemapmodelloaders.Constants;
import me.owies.bluemapmodelloaders.resources.ExtendedModel;
import me.owies.bluemapmodelloaders.resources.LoaderType;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePack;
import me.owies.bluemapmodelloaders.resources.composite.CompositeChildModel;
import me.owies.bluemapmodelloaders.resources.composite.CompositeModelExtension;
import java.util.Map;
import java.util.stream.Collectors;


public class CompositeModelRenderer implements ExtendedBlockRenderer {
    public static final BlockRendererType TYPE = new BlockRendererType.Impl(new Key("bluemapmodelloaders",  "composite"), CompositeModelRenderer::new);
    private final Map<BlockRendererType, BlockRenderer> blockRenderers;
    private final ModelLoaderResourcePack modelLoaderResourcePack;
    private final ResourcePack resourcePack;

    private Variant variant;

    public CompositeModelRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        this.resourcePack = resourcePack;
        this.blockRenderers = BlockRendererType.REGISTRY
                .values()
                .stream()
                .collect(Collectors.toMap(
                        type -> type,
                        type -> {
                            if (type == TYPE) {
                                return this; // prevent infinite recursion
                            }
                            return type.create(resourcePack, textureGallery, renderSettings);
                        }
                ));
        modelLoaderResourcePack = ModelLoaderResourcePack.INSTANCE;
    }

    @Override
    public void render(BlockNeighborhood block, Variant variant, TileModelView tileModel, Color blockColor) {
        this.variant = variant;
        ExtendedModel modelLoaderResource = modelLoaderResourcePack.getModels().get(variant.getModel());

        if (modelLoaderResource == null) return;

        renderModel(block, variant, null, modelLoaderResource, tileModel, blockColor);
    }

    @Override
    public void renderModel(BlockNeighborhood block, Variant variant, Model model, ExtendedModel extendedModel, TileModelView tileModel, Color blockColor) {

        CompositeModelExtension compositeModelResource = extendedModel.getExtension(LoaderType.COMPOSITE);

        if (compositeModelResource == null) return;

        Model originalVariantModel = variant.getModel().getResource(resourcePack::getModel);

        for (CompositeChildModel childModel: compositeModelResource.getChildren().values()) {
            BlockRendererType rendererType = BlockRendererType.DEFAULT;
            LoaderType<?> loaderType = childModel.getExtendedModel().getLoader();
            if (childModel.getExtendedModel().getLoader() != null) {
                rendererType = loaderType.getRenderer();
            }

            BlockRenderer renderer = blockRenderers.get(rendererType);

            Constants.LOG.info("Rendering child model of type : " + rendererType);

            if (renderer instanceof ExtendedBlockRenderer) {
                ((ExtendedBlockRenderer) renderer).renderModel(block, variant, childModel.getModel(), childModel.getExtendedModel(), tileModel, blockColor);
            } else {
                variant.getModel().setResource(childModel.getModel());

                renderer.render(block, variant, tileModel, blockColor);
            }
        }

        variant.getModel().setResource(originalVariantModel);
    }
}
