package me.owies.bluemapmodelloaders.resources.objmodel;

import lombok.Getter;

@Getter
public class ObjFace {
    private String material;
    private ObjVertexData[] vertices;

    public ObjFace(String material, ObjVertexData[] vertices) {
        this.material = material;
        this.vertices = vertices;
    }
}
