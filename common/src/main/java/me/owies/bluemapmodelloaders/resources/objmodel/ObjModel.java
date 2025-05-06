package me.owies.bluemapmodelloaders.resources.objmodel;

import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3f;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ObjModel {
    public static final ObjMaterial MISSING_MATERIAL = new ObjMaterial();

    private Vector3f[] vertices;
    private Vector2f[] textureCoords;
    private ObjFace[] faces;
    private ResourcePath<ObjMaterialLibrary>[] materialLibraries;

    public static ObjModel fromReader(Reader reader, Path path, int namespacePos, int valuePos) throws IOException {
        BufferedReader br = new BufferedReader(reader);

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> textureCoords = new ArrayList<>();
        List<ObjFace> faces = new ArrayList<>();
        List<ResourcePath<ObjMaterialLibrary>> materialLibraries = new ArrayList<>();

        String currentMaterial = "";

        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String[] args = line.split(" ");

            switch (args[0]) {
                case "v" -> {
                    if (args.length < 4) {
                        // TODO: Exception
                        continue;
                    }
                    vertices.add(new Vector3f(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])));
                }
                case "vt" -> {
                    if (args.length < 3) {
                        // TODO: Exception
                        continue;
                    }
                    textureCoords.add(new Vector2f(Double.parseDouble(args[1]), Double.parseDouble(args[2])));
                }
                case "f" -> {
                    if (args.length < 4) {
                        continue; // invalid face entry
                    }
                    ObjVertexData[] faceVertices = new ObjVertexData[args.length - 1];
                    for (int i = 0; i < faceVertices.length; i++) {
                        String[] vertex = args[i + 1].split("/");
                        int vertexIndex = Integer.parseInt(vertex[0]) - 1;
                        int uvIndex = -1;
                        if (vertex.length >= 2 && !vertex[1].isEmpty()) {
                            uvIndex = Integer.parseInt(vertex[1]) - 1;
                        }
                        faceVertices[i] = new ObjVertexData(vertexIndex, uvIndex);
                    }
                    faces.add(new ObjFace(currentMaterial, faceVertices));
                }
                case "usemtl" -> {
                    if (args.length < 2) {
                        continue; // missing material name
                    }

                    currentMaterial = args[1];
                }
                case "mtllib" -> {
                    if (args.length < 2) {
                        continue; // missing material library
                    }

                    materialLibraries.add(new ResourcePath<>(path.resolveSibling(args[1]+".mtl"), namespacePos, valuePos));
                }
            }
        }

        br.close();

        ObjModel obj = new ObjModel();
        obj.vertices = vertices.toArray(new Vector3f[0]);
        obj.textureCoords = textureCoords.toArray(new Vector2f[0]);
        obj.faces = faces.toArray(new ObjFace[0]);
        obj.materialLibraries = materialLibraries.toArray(new ResourcePath[0]);

        return obj;
    }
}
