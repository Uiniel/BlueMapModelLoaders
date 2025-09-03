package me.owies.bluemapmodelloaders.renderer;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import me.owies.bluemapmodelloaders.resources.ExtendedModel;
import me.owies.bluemapmodelloaders.resources.LoaderType;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePack;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePackFactory;
import me.owies.bluemapmodelloaders.resources.composite.CompositeChildModel;
import me.owies.bluemapmodelloaders.resources.composite.CompositeModelExtension;


public class CompositeModelRenderer implements ExtendedBlockRenderer {
    public static final BlockRendererType TYPE = new BlockRendererType.Impl(new Key("bluemapmodelloaders",  "composite"), CompositeModelRenderer::new);
    private static LoadingCache<BlockRendererType, BlockRenderer> blockRenderers = null;
    private final ModelLoaderResourcePack modelLoaderResourcePack;
    private final ResourcePack resourcePack;

    private Variant variant;

    public CompositeModelRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        this.resourcePack = resourcePack;
        if (blockRenderers == null) { // prevent infinite recursion
            blockRenderers = Caffeine.newBuilder()
                            .build(type -> type.create(resourcePack, textureGallery, renderSettings));
        }
        modelLoaderResourcePack = resourcePack.getExtension(ModelLoaderResourcePackFactory.INSTANCE);
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

        int modelStart = tileModel.getStart();

        for (CompositeChildModel childModel: compositeModelResource.getChildren().values()) {
            renderCompositeChildModel(block, variant, model, extendedModel, tileModel, blockColor, childModel);
        }

        tileModel.initialize(modelStart);
    }

    public void renderCompositeChildModel(BlockNeighborhood block, Variant parent_variant, Model model, ExtendedModel extendedModel, TileModelView tileModel, Color blockColor, CompositeChildModel childModel) {
        BlockRendererType rendererType = BlockRendererType.DEFAULT;
        LoaderType<?> loaderType = childModel.getExtendedModel().getLoader();
        if (childModel.getExtendedModel().getLoader() != null) {
            rendererType = loaderType.getRenderer();
        }

        BlockRenderer renderer = blockRenderers.get(rendererType);
        tileModel.initialize();

        if (renderer instanceof ExtendedBlockRenderer) {
            ((ExtendedBlockRenderer) renderer).renderModel(block, parent_variant, childModel.getModel(), childModel.getExtendedModel(), tileModel, blockColor);
        } else {
            ResourcePath<Model> childModelPath = new ResourcePath<>("bluemapmodelloaders:composite/child/dummy");
            childModelPath.setResource(childModel.getModel());
            Variant child_variant = new Variant(childModelPath, parent_variant.getX(), parent_variant.getY(), parent_variant.isUvlock(), parent_variant.getWeight());

            renderer.render(block, child_variant, tileModel, blockColor);
        }
    }
}
