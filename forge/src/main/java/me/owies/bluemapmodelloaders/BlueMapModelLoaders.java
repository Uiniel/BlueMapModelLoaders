package me.owies.bluemapmodelloaders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class BlueMapModelLoaders {

    public BlueMapModelLoaders() {

        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> CommonClass::init);

    }
}
