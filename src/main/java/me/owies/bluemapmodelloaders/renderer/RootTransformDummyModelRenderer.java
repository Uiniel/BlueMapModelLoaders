package me.owies.bluemapmodelloaders.renderer;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
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
import me.owies.bluemapmodelloaders.resources.roottransformdummy.RootTransformDummyModelExtension;

public class RootTransformDummyModelRenderer extends CompositeModelRenderer {
    public static final BlockRendererType TYPE = new BlockRendererType.Impl(new Key("bluemapmodelloaders",  "root_transform_dummy"), RootTransformDummyModelRenderer::new);


    private ResourcePack resourcePack;
    private ModelLoaderResourcePack modelResourcePack;


    public RootTransformDummyModelRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        super(resourcePack, textureGallery, renderSettings);

        this.resourcePack = resourcePack;

        modelResourcePack = resourcePack.getExtension(ModelLoaderResourcePackFactory.INSTANCE);
    }

    @Override
    public void render(BlockNeighborhood block, Variant variant, TileModelView tileModel, Color blockColor) {
        ExtendedModel modelLoaderResource = modelResourcePack.getModels().get(variant.getModel());
        Model modelResource = resourcePack.getModels().get(variant.getModel());

        if (modelLoaderResource == null) return;

        renderModel(block, variant, modelResource, modelLoaderResource, tileModel, blockColor);
    }

    @Override
    public void renderModel(BlockNeighborhood block, Variant variant, Model modelResource, ExtendedModel modelLoaderResource, TileModelView blockModel, Color color) {
        RootTransformDummyModelExtension modelExtension = modelLoaderResource.getExtension(LoaderType.ROOT_TRANSFORM_DUMMY);

        int modelStart = blockModel.getStart();
        renderCompositeChildModel(
                block,
                new Variant(variant.getModel(), 0, 0, 0, variant.isUvlock(), variant.getWeight()),
                modelResource,
                modelLoaderResource,
                blockModel,
                color,
                new CompositeChildModel(modelResource, modelLoaderResource),
                true
        );
        blockModel.initialize(modelStart);

        blockModel.transform(modelExtension.getTransform());

        if (variant.isTransformed())
            blockModel.transform(variant.getTransformMatrix());
    }
}
