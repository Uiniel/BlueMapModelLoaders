package me.owies.bluemapmodelloaders.resources.obj;

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
import java.util.stream.Stream;

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
                        int vertexIndex = Integer.parseInt(vertex[0]);
                        int uvIndex = 0;
                        if (vertex.length >= 2 && !vertex[1].isEmpty()) {
                            uvIndex = Integer.parseInt(vertex[1]);
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

                    materialLibraries.add(new ResourcePath<>(path.resolveSibling(args[1] + ".mtl"), namespacePos, valuePos));
                }
            }
        }

        br.close();


        return createModel(vertices, textureCoords, faces, materialLibraries);
    }

    private static ObjModel createModel(List<Vector3f> vertices, List<Vector2f> textureCoords, List<ObjFace> faces, List<ResourcePath<ObjMaterialLibrary>> materialLibraries) {
        ObjModel obj = new ObjModel();
        obj.vertices = vertices.toArray(Vector3f[]::new);
        obj.textureCoords = textureCoords.toArray(Vector2f[]::new);
        obj.faces = faces
                .stream()
                .flatMap(ObjModel::triangulateFace)
                .peek(face -> {
                    Vector3f normal = calculateNormal(
                            obj.getVertex(face.getVertices()[0].getVertexIndex()),
                            obj.getVertex(face.getVertices()[1].getVertexIndex()),
                            obj.getVertex(face.getVertices()[2].getVertexIndex())
                    );
                    face.setNormal(normal);
                })
                .filter(face -> face.getNormal() != null) // Filter out zero-area faces
                .toArray(ObjFace[]::new);
        obj.materialLibraries = materialLibraries.toArray(ResourcePath[]::new);

        return obj;
    }

    private static Stream<ObjFace> triangulateFace(ObjFace face) {
        ObjVertexData[] vertices = face.getVertices();

        return switch (face.getVertices().length) {
            case 3 -> Stream.of(face);
            case 4 -> Stream.of(
                    new ObjFace(face.getMaterial(), new ObjVertexData[]{vertices[0], vertices[1], vertices[2]}),
                    new ObjFace(face.getMaterial(), new ObjVertexData[]{vertices[2], vertices[3], vertices[0]})
            );
            default -> Stream.empty(); // no mod loader that I know supports them
        };
    }

    private static Vector3f calculateNormal(Vector3f p0, Vector3f p1, Vector3f p2) {
        Vector3f cross = p1.sub(p0).cross(p2.sub(p0));
        if (cross.lengthSquared() == 0) {
            return null;
        }
        return cross.normalize();
    }

    public Vector3f getVertex(int index) {
        if (index < 0) {
            return vertices[vertices.length + index];
        }
        return vertices[index - 1];
    }

    public Vector2f getTextureCoord(int index) {
        if (index < 0) {
            return textureCoords[vertices.length + index];
        }
        return textureCoords[index - 1];
    }
}
