package me.owies.bluemapmodelloaders.resources.obj;

import com.flowpowered.math.vector.Vector3f;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ObjFace {
    private String material;
    private ObjVertexData[] vertices;

    @Setter
    private Vector3f normal = null;

    public ObjFace(String material, ObjVertexData[] vertices) {
        this.material = material;
        this.vertices = vertices;
    }
}
