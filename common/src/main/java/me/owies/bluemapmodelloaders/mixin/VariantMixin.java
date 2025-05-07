package me.owies.bluemapmodelloaders.mixin;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Variant.class)
public interface VariantMixin {

    @Accessor("renderer")
    void setRenderer(BlockRendererType renderer);
}
