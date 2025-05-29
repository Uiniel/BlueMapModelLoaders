package me.owies.bluemapmodelloaders;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class BlueMapModelLoaders {

    public BlueMapModelLoaders(IEventBus eventBus) {
        CommonClass.init();
    }
}
