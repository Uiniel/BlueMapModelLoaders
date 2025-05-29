package me.owies.bluemapmodelloaders.renderer;

import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import me.owies.bluemapmodelloaders.resources.ExtendedModel;

public interface ExtendedBlockRenderer extends BlockRenderer {
    void renderModel(BlockNeighborhood block, Variant variant, Model model, ExtendedModel extendedModel, TileModelView tileModel, Color blockColor);
}
