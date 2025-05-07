package me.owies.bluemapmodelloaders.mixin;

import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ResourcePack.class)
public interface ResourcePackAccessorMixin {
    @Accessor("blockStates")
    public abstract Map<ResourcePath<BlockState>, BlockState> getBlockStates();
}
