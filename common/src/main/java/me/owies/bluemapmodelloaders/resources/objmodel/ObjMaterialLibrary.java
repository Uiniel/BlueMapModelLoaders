package me.owies.bluemapmodelloaders.resources.objmodel;

import de.bluecolored.bluemap.core.logger.Logger;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.util.math.Color;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ObjMaterialLibrary {
    private Map<String, ObjMaterial>  materials;

    public static ObjMaterialLibrary fromReader(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);

        Map<String, ObjMaterial> materials = new HashMap<>();
        ObjMaterial currentMaterial = null;

        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String[] args = line.split(" ");

            switch (args[0]) {
                case "newmtl" -> {
                    if (args.length < 2) {
                        Logger.global.logWarning("Missing material name");
                        // exception
                        continue;
                    }
                    currentMaterial = new ObjMaterial();
                    materials.put(args[1], currentMaterial);
                }

                case "map_Kd" -> {
                    if (args.length < 2) {
                        Logger.global.logWarning("No texture specified");
                        continue;
                    }
                    if (args[1].charAt(0) != '#') {
                        Logger.global.logWarning("Can only reference textures specified in the json");
                        continue;
                    }
                    if (currentMaterial == null) {
                        Logger.global.logWarning("No material declaration found");
                        continue;
                    }
                    currentMaterial.setTexture(new TextureVariable(args[1].substring(1)));
                }

                case "Kd" -> {
                    if (args.length < 4) {
                        Logger.global.logWarning("No color specified");
                        continue;
                    }
                    if (currentMaterial == null) {
                        Logger.global.logWarning("No material declaration found");
                        continue;
                    }
                    if (currentMaterial.getTexture() == ObjMaterial.MISSING_TEXTURE) {
                        currentMaterial.setTexture(ObjMaterial.WHITE_TEXTURE);
                    }

                    float r = Float.parseFloat(args[1]);
                    float g = Float.parseFloat(args[2]);
                    float b = Float.parseFloat(args[3]);
                    float a = 1.0f;
                    if (args.length >= 5) {
                        a = Float.parseFloat(args[4]);
                    }

                    currentMaterial.setColor(new Color().set(r, g, b, a, false));
                }

                case "Tr" -> {
                    if (args.length < 2) {
                        Logger.global.logWarning("No transparency specified");
                        continue;
                    }
                    if (currentMaterial == null) {
                        Logger.global.logWarning("No material declaration found");
                        continue;
                    }
                    currentMaterial.setOpacity(1.0f - Float.parseFloat(args[1]));
                }

                case "d" -> {
                    if (args.length < 2) {
                        Logger.global.logWarning("No transparency specified");
                        continue;
                    }
                    if (currentMaterial == null) {
                        Logger.global.logWarning("No material declaration found");
                        continue;
                    }
                    currentMaterial.setOpacity(Float.parseFloat(args[1]));
                }

                case "forge_TintIndex" -> {
                    if (args.length < 2) {
                        Logger.global.logWarning("No tint index specified");
                        continue;
                    }
                    if (currentMaterial == null) {
                        Logger.global.logWarning("No material declaration found");
                        continue;
                    }
                    currentMaterial.setTintIndex(Integer.parseInt(args[1]));
                }
            }
        }

        br.close();

        ObjMaterialLibrary materialLibrary = new ObjMaterialLibrary();
        materialLibrary.materials = materials;
        return materialLibrary;
    }
}
