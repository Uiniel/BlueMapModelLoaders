package me.owies.bluemapmodelloaders.resources.objmodel;

import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.util.math.Color;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ObjMaterial {
    public static final TextureVariable MISSING_TEXTURE = new TextureVariable(ResourcePack.MISSING_TEXTURE);
    public static final TextureVariable WHITE_TEXTURE = new TextureVariable(new ResourcePath<>("bluemap", "block/white"));

    private TextureVariable texture = MISSING_TEXTURE;
    private Color color = new Color().set(1.0f, 1.0f, 1.0f, 1.0f, false);
    private int tintIndex = -1;
    private float opacity = 1.0f;

    public ObjMaterial(TextureVariable texture, Color color, int tintIndex, float opacity) {
        this.texture = texture;
        this.color = color;
        this.tintIndex = tintIndex;
        this.opacity = opacity;
    }

    public ObjMaterial() {}
}
