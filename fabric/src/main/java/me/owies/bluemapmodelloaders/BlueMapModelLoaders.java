package me.owies.bluemapmodelloaders;

import net.fabricmc.api.DedicatedServerModInitializer;

public class BlueMapModelLoaders implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CommonClass.init();
    }
}
