package me.owies.bluemapmodelloaders.resources.objmodel;

import lombok.Getter;

@Getter
public class ObjVertexData {
    private int vertexIndex;
    private int uvIndex;

    public ObjVertexData(int vertexIndex, int uvIndex) {
        this.vertexIndex = vertexIndex;
        this.uvIndex = uvIndex;
    }
}
