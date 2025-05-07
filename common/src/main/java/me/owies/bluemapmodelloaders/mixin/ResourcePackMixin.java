package me.owies.bluemapmodelloaders.mixin;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import me.owies.bluemapmodelloaders.resources.ModelLoaderResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResourcePack.class)
public abstract class ResourcePackMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        ModelLoaderResourcePack.BLUEMAP_RESOURCE_PACK = (ResourcePack) (Object) this;
    }
}
