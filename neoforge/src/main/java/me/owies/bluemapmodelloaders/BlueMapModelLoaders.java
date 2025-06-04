package me.owies.bluemapmodelloaders;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = Constants.MOD_ID, dist = Dist.DEDICATED_SERVER)
public class BlueMapModelLoaders {

    public BlueMapModelLoaders(IEventBus eventBus) {
        CommonClass.init();
    }
}
