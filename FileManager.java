import java.io.*;

/** 
 *  File format:
"
   3 Vertices
   0,40.0,71.0,0.0
   1,646.0,71.0,0.0
   2,61.0,77.0,0.0

   2 Edges
   1,0
   2,1

   3 Curves
   0,1
   0,0
   0,2

   1 Furniture
   itemNameID,80,160,40,80,0.0       **as specified in Furniture.getSaveString()
"
 */
public class FileManager {

   /** Writes the given arrays to file in the expected format */
   public static void save(File saveAs, float[][] vertices, int[][] edges, Furniture[] furniture)
         throws IOException, IllegalArgumentException {
      if (saveAs == null) throw new IllegalArgumentException("SaveAs is null");
      FileWriter fileW = new FileWriter(saveAs);
      BufferedWriter bw = new BufferedWriter(fileW);

      saveVertices(bw, vertices);
      bw.newLine();
      saveEdges(bw, edges);
      bw.newLine();
      saveFurniture(bw, furniture);

      bw.flush();
   }

   /** Returns a Coords or throws an exception if there is a problem reading */
   public static Coords load(File file) throws IOException, Exception, IllegalArgumentException {
      if (file == null) throw new IllegalArgumentException("SaveAs is null");
      FileReader fileR = new FileReader(file);
      BufferedReader br = new BufferedReader(fileR);


      /* Get number of vertices and load vertices */
      int numVertices = loadHeaderNum(br);
      if (numVertices < 0) {
         throw new Exception("The number of vertices is either invalid or not g"
            + "iven in the file");
      }
      float[][] vertices = loadVertices(br, numVertices);
      if (vertices == null) throw new Exception("Unable to load all vertices");


      /* Get Number of Edges and load Edges */
      int numEdges = loadHeaderNum(br);
      if (numEdges < 0) {
         throw new Exception("The number of edges is either invalid or not give"
            + "n in the file");
      }
      int[][] edges = loadEdges(br, numEdges);
      if (edges == null) throw new Exception("Unable to load all edges");


      /* Get Number of Furniture and load Furniture */
      int numFurniture = loadHeaderNum(br);
      if (numEdges < 0) {
         throw new Exception("The number of furniture items is either invalid or"
            + " not given in the file");
      }
      Furniture[] furniture = loadFurniture(br, numFurniture);
      if (furniture == null) throw new Exception("Unable to load all furniture");

      
      /* Loading might not reach this stage */
      return new Coords(file, vertices, edges, furniture);
   }


   



   /** Writes the given vertices to the file */
   private static void saveVertices(BufferedWriter bw, float[][] vertices) throws IOException {
      if (vertices == null) throw new IllegalArgumentException("Vertices is null");

      String line = vertices.length + " Vertices";
      bw.write(line, 0, line.length());
      bw.newLine();

      for (int i = 0; i < vertices.length; i++) {
         if (vertices[i] == null || vertices[i].length != 3) {
            throw new IllegalArgumentException("Vertices array needs to be of size n by 3");
         }

         line = i + "," + vertices[i][0] + "," + vertices[i][1] + "," + vertices[i][2];
         bw.write(line, 0, line.length());
         bw.newLine();
      }
   }

   /** Writes the given edges to the file */
   private static void saveEdges(BufferedWriter bw, int[][] edges) throws IOException {
      if (edges == null) throw new IllegalArgumentException("Edges is null");

      String line = edges.length + " Edges";
      bw.write(line, 0, line.length());
      bw.newLine();

      for (int i = 0; i < edges.length; i++) {
         if (edges[i] == null || edges[i].length != 2) {
            throw new IllegalArgumentException("Edges array needs to be of size m by 2");
         }

         line = edges[i][0] + "," + edges[i][1];
         bw.write(line, 0, line.length());
         bw.newLine();
      }
   }

   /** Writes the given furniture objects to the file */
   private static void saveFurniture(BufferedWriter bw, Furniture[] furniture) throws IOException {
      if (furniture == null) throw new IllegalArgumentException("Furniture is null");

      String line = furniture.length + " Furniture";
      bw.write(line, 0, line.length());
      bw.newLine();

      for (int i = 0; i < furniture.length; i++) {
         if (furniture[i] == null) {
            throw new IllegalArgumentException("Furniture array element is null");
         }

         line = furniture[i].getSaveString();
         bw.write(line, 0, line.length());
         bw.newLine();
      }
   }

   /** less than 0 if none could be found */
   private static int loadHeaderNum(BufferedReader br) throws IOException {
      String line;
      String[] split;
      int numObjects = -1;

      while ((line = br.readLine()) != null && (split = line.split("\\s")) != null) {

         if (split.length > 0) {
            try {
               numObjects = Integer.parseInt(split[0]);
               break;

            } catch (NumberFormatException e) {
               // this line isn't in the form "# Vertices", keep searching
            }
         }
      }

      return numObjects;
   }

   /** null if its unable to load all numVertices # vertices */
   private static float[][] loadVertices(BufferedReader br, int numVertices) throws IOException {
      if (numVertices < 0) return null;

      String line;
      String[] split;
      float[][] vertices = new float[numVertices][3];
      int vertexLoadCount = 0;

      while (vertexLoadCount < numVertices && (line = br.readLine()) != null && (split = line.split(",")) != null) {
         if (split.length == 4) {
            try {
               int index = Integer.parseInt(split[0]);
               if (index < 0 || index >= vertices.length) continue;

               float x, y, z;
               x = Float.parseFloat(split[1]);
               y = Float.parseFloat(split[2]);
               z = Float.parseFloat(split[3]);

               vertices[index][0] = x;
               vertices[index][1] = y;
               vertices[index][2] = z;

               vertexLoadCount++;

            } catch (NumberFormatException e) {
               // skip this line, it is corrupt
            }
         }
      }

      if (vertexLoadCount != numVertices) return null;
      else return vertices;
   }

   /** null if its unable to load all numEdges # edges */
   private static int[][] loadEdges(BufferedReader br, int numEdges) throws IOException {
      if (numEdges < 0) return null;

      String line;
      String[] split;
      int[][] edges = new int[numEdges][2];
      int edgeLoadCount = 0;

      while (edgeLoadCount < numEdges && (line = br.readLine()) != null && (split = line.split(",")) != null) {
         if (split.length == 2) {
            try {
               int v1, v2;
               v1 = Integer.parseInt(split[0]);
               v2 = Integer.parseInt(split[1]);

               edges[edgeLoadCount][0] = v1;
               edges[edgeLoadCount][1] = v2;

               edgeLoadCount++;

            } catch (NumberFormatException e) {
               // skip this line, it is corrupt
            }

         }
      }

      if (edgeLoadCount != numEdges) return null;
      else return edges;
   }

   /** null if its unable to load all numFurniture # furniture objects */
   private static Furniture[] loadFurniture(BufferedReader br, int numFurniture) throws IOException {
      if (numFurniture < 0) return null;

      String line;
      Furniture[] furniture = new Furniture[numFurniture];
      int furnitureLoadCount = 0;

      while (furnitureLoadCount < numFurniture && (line = br.readLine()) != null) {
         try {
            furniture[furnitureLoadCount] = new Furniture(line);
            furnitureLoadCount++;
         } catch (IllegalArgumentException e) {
            // skip this line, it is corrupt
         }
      }

      if (furnitureLoadCount != numFurniture) return null;
      else return furniture;
   }
}
