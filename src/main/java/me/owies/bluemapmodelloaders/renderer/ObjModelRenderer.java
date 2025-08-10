package me.owies.bluemapmodelloaders.renderer;

import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.logger.Logger;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.BlockColorCalculatorFactory;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.VectorM2f;
import de.bluecolored.bluemap.core.util.math.VectorM3f;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import me.owies.bluemapmodelloaders.Constants;
import me.owies.bluemapmodelloaders.resources.ExtendedModel;
import me.owies.bluemapmodelloaders.resources.LoaderType;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePack;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePackFactory;
import me.owies.bluemapmodelloaders.resources.obj.*;

// Code copied and modified from de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer
// Copyright (c) Blue <https://www.bluecolored.de>
public class ObjModelRenderer implements ExtendedBlockRenderer {
    public static final BlockRendererType TYPE = new BlockRendererType.Impl(new Key("bluemapmodelloaders",  "obj"), ObjModelRenderer::new);

    private final ResourcePack resourcePack;
    private final ModelLoaderResourcePack modelLoaderResourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;
    private final BlockColorCalculatorFactory.BlockColorCalculator blockColorCalculator;

    private final VectorM3f[] corners = new VectorM3f[8];
    private final VectorM2f[] rawUvs = new VectorM2f[4];
    private final VectorM2f[] uvs = new VectorM2f[4];
    private final Color tintColor = new Color();
    private final Color mapColor = new Color();

    private BlockNeighborhood block;
    private Variant variant;
    private Model modelResource;
    private ObjModelExtension objModelResource;
    private TileModelView blockModel;
    private Color blockColor;
    private float blockColorOpacity;

    public ObjModelRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings) {
        this.resourcePack = resourcePack;
        this.textureGallery = textureGallery;
        this.renderSettings = renderSettings;
        this.blockColorCalculator = resourcePack.getColorCalculatorFactory().createCalculator();
        modelLoaderResourcePack = resourcePack.getExtension(ModelLoaderResourcePackFactory.INSTANCE);

        for (int i = 0; i < corners.length; i++) corners[i] = new VectorM3f(0, 0, 0);
        for (int i = 0; i < rawUvs.length; i++) rawUvs[i] = new VectorM2f(0, 0);
    }

    public void render(BlockNeighborhood block, Variant variant, TileModelView blockModel, Color color) {
        Model modelResource = variant.getModel().getResource(resourcePack.getModels()::get);
        ExtendedModel modelLoaderResource = modelLoaderResourcePack.getModels().get(variant.getModel());

        if (modelLoaderResource == null) return;

        renderModel(block, variant, modelResource, modelLoaderResource, blockModel, color);
    }

    @Override
    public void renderModel(BlockNeighborhood block, Variant variant, Model model, ExtendedModel extendedModel, TileModelView blockModel, Color color) {

        this.block = block;
        this.blockModel = blockModel;
        this.blockColor = color;
        this.blockColorOpacity = 0f;
        this.variant = variant;
        this.modelResource = model;

        this.objModelResource = extendedModel.getExtension(LoaderType.OBJ);

        if (this.objModelResource == null) return;

        this.tintColor.set(0, 0, 0, -1, true);

        ResourcePath<ObjModel> objPath = objModelResource.getModel();
        if (objPath == null) {
            Constants.LOG.logWarning("No obj model specified: " + variant.getModel());
            return;
        }
        ObjModel objModel = objPath.getResource(modelLoaderResourcePack.getObjModels()::get);
        if (objModel == null) {
            Constants.LOG.logWarning("Missing obj model: " + variant.getModel());
            return;
        }

        // render model
        int modelStart = blockModel.getStart();

        buildModelObjResource(objModel, blockModel);

        if (color.a > 0) {
            color.flatten().straight();
            color.a = blockColorOpacity;
        }

        blockModel.initialize(modelStart);

        // apply model-transform
        if (variant.isTransformed())
            blockModel.transform(variant.getTransformMatrix());

        //random offset
        if (block.getProperties().isRandomOffset()){
            float dx = (hashToFloat(block.getX(), block.getZ(), 123984) - 0.5f) * 0.75f;
            float dz = (hashToFloat(block.getX(), block.getZ(), 345542) - 0.5f) * 0.75f;
            blockModel.translate(dx, 0, dz);
        }
    }

    private void buildModelObjResource(ObjModel model, TileModelView blockModel) {
        Constants.LOG.logDebug("Building model obj resource, for model " + objModelResource.getModel());
        for (ObjFace face : model.getFaces()) {
            createObjTri(face, model,  blockModel);
        }
    }

    private void createObjTri(ObjFace face, ObjModel model, TileModelView blockModel) {
        ObjVertexData p0Data = face.getVertices()[0];
        ObjVertexData p1Data = face.getVertices()[1];
        ObjVertexData p2Data = face.getVertices()[2];

        Vector3f p0 = model.getVertex(p0Data.getVertexIndex());
        Vector3f p1 = model.getVertex(p1Data.getVertexIndex());
        Vector3f p2 = model.getVertex(p2Data.getVertexIndex());

        int sunLight = 15;
        int blockLight = 15;
        if (objModelResource.isShadeQuads()) {
            // light calculation
            Vector3f face_pos = p0.add(p1).add(p2).mul(1 / 3f).add(new Vector3f(-0.5, -0.5, -0.5)).round(); // in case of models bigger than one block
            ExtendedBlock faceBlockLocation = getRotationRelativeBlock(face_pos);
            ExtendedBlock facedBlockNeighbor = getRotationRelativeBlock(face_pos.add(face.getNormal()));
            LightData blockLightData = faceBlockLocation.getLightData();
            LightData facedLightData = facedBlockNeighbor.getLightData();

            sunLight = Math.max(blockLightData.getSkyLight(), facedLightData.getSkyLight());
            blockLight = Math.max(blockLightData.getBlockLight(), facedLightData.getBlockLight());
        }

        // filter out faces that are in a "cave" that should not be rendered
        if (
                block.isRemoveIfCave() &&
                        (renderSettings.isCaveDetectionUsesBlockLight() ? Math.max(blockLight, sunLight) : sunLight) == 0
        ) return;

        TileModel tileModel = blockModel.getTileModel();

        blockModel.initialize();
        blockModel.add(1);

        int start = blockModel.getStart();

        tileModel.setPositions(start,
                p0.getX(), p0.getY(), p0.getZ(),
                p1.getX(), p1.getY(), p1.getZ(),
                p2.getX(), p2.getY(), p2.getZ());

        ObjMaterial material = ObjModel.MISSING_MATERIAL;
        for (ResourcePath<ObjMaterialLibrary> mtlPath: model.getMaterialLibraries()) {
            ObjMaterialLibrary mtl = mtlPath.getResource(modelLoaderResourcePack.getMtlLibraries()::get);
            if (mtl == null) {
                Constants.LOG.logWarning("Missing mtl library: " + mtlPath);
                continue;
            }
            if (mtl.getMaterials().containsKey(face.getMaterial())) {
                material = mtl.getMaterials().get(face.getMaterial());
            }
        }
        if (material == ObjModel.MISSING_MATERIAL) {
            Constants.LOG.logWarning(objModelResource.getModel() + ": material not found (" + face.getMaterial() + ")");
        }

        // the mtl can be reused for different models with different textures and calling material.getTexture().getTexturePath(...) would cache the texture
        ResourcePath<Texture> texturePath;
        if (material.getTexture().isReference()) {
            texturePath = modelResource.getTextures().get(material.getTexture().getReferenceName()).getTexturePath(modelResource.getTextures()::get);
        } else {
            texturePath = material.getTexture().getTexturePath();
        }

        int textureId = textureGallery.get(texturePath);
        tileModel.setMaterialIndex(start, textureId);

        Vector2f uv0 = new Vector2f(1, 0);
        Vector2f uv1 = new Vector2f(0, 0);
        Vector2f uv2 = new Vector2f(0, 1);
        if (p0Data.getUvIndex() != 0 && p1Data.getUvIndex() != 0 && p2Data.getUvIndex() != 0) {
            uv0 = model.getTextureCoord(p0Data.getUvIndex());
            uv1 = model.getTextureCoord(p1Data.getUvIndex());
            uv2 = model.getTextureCoord(p2Data.getUvIndex());
        }

        if (objModelResource.isFlipV()) {
            uv0 = uv0.mul(1, -1).add(0, 1);
            uv1 = uv1.mul(1, -1).add(0, 1);
            uv2 = uv2.mul(1, -1).add(0, 1);
        }

        tileModel.setUvs(start,
                uv0.getX(), uv0.getY(),
                uv1.getX(), uv1.getY(),
                uv2.getX(), uv2.getY());

        Color color = material.getColor();
        tileModel.setColor(start, color.r, color.g, color.b);

        tileModel.setAOs(start, 1.0f, 1.0f, 1.0f);

        tileModel.setBlocklight(start, blockLight);
        tileModel.setSunlight(start, sunLight);
    }

    private ExtendedBlock getRotationRelativeBlock(Vector3f direction){
        return getRotationRelativeBlock(
                direction.getX(),
                direction.getY(),
                direction.getZ()
        );
    }

    private final VectorM3f rotationRelativeBlockDirection = new VectorM3f(0, 0, 0);
    private ExtendedBlock getRotationRelativeBlock(float dx, float dy, float dz){
        rotationRelativeBlockDirection.set(dx, dy, dz);
        makeRotationRelative(rotationRelativeBlockDirection);

        return block.getNeighborBlock(
                Math.round(rotationRelativeBlockDirection.x),
                Math.round(rotationRelativeBlockDirection.y),
                Math.round(rotationRelativeBlockDirection.z)
        );
    }

    private void makeRotationRelative(VectorM3f direction){
        if (variant.isTransformed())
            direction.rotateAndScale(variant.getTransformMatrix());
    }

    private float testAo(VectorM3f vertex, Direction dir){
        Vector3i dirVec = dir.toVector();
        int occluding = 0;

        int x = 0;
        if (vertex.x == 16){
            x = 1;
        } else if (vertex.x == 0){
            x = -1;
        }

        int y = 0;
        if (vertex.y == 16){
            y = 1;
        } else if (vertex.y == 0){
            y = -1;
        }

        int z = 0;
        if (vertex.z == 16){
            z = 1;
        } else if (vertex.z == 0){
            z = -1;
        }


        if (x * dirVec.getX() + y * dirVec.getY() > 0){
            if (getRotationRelativeBlock(x, y, 0).getProperties().isOccluding()) occluding++;
        }

        if (x * dirVec.getX() + z * dirVec.getZ() > 0){
            if (getRotationRelativeBlock(x, 0, z).getProperties().isOccluding()) occluding++;
        }

        if (y * dirVec.getY() + z * dirVec.getZ() > 0){
            if (getRotationRelativeBlock(0, y, z).getProperties().isOccluding()) occluding++;
        }

        if (x * dirVec.getX() + y * dirVec.getY() + z * dirVec.getZ() > 0){
            if (getRotationRelativeBlock(x, y, z).getProperties().isOccluding()) occluding++;
        }

        if (occluding > 3) occluding = 3;
        return  Math.max(0f, Math.min(1f - occluding * 0.25f, 1f));
    }

    private static float hashToFloat(int x, int z, long seed) {
        final long hash = x * 73428767L ^ z * 4382893L ^ seed * 457;
        return (hash * (hash + 456149) & 0x00ffffff) / (float) 0x01000000;
    }
}
