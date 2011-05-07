import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.awt.*;

/** Save file is a jar, Coods save file format:
"
   3 Vertices
   0,40.0,71.0,0.0                **vertex 0, x=40, y=71, z=0
   1,646.0,71.0,0.0
   2,61.0,77.0,0.0

   2 Edges
   1,0,43,24                      **edge uses vertex 1 and 0, ctrl point (43,24)
   2,1,21,21

   1 Furniture
   itemNameID,80,160,40,80,0.0    **as specified in Furniture.getSaveString()

   2 Polygons
   1,2,3 3,4,5 6
   1,2,3 3,4,5 6

   3 PolygonFills
   1213453
   434542
   534635

   2 PolygonEdges
   1,2,3
   1,2,3

   2 PolygonReverse
   true,false,true
   true,false,true
"
 */
public class FileManager {

   /** Writes the given arrays to file in the expected format */
   public static void save(File saveAs, float[][] vertices, int[][] edges, Furniture[] furniture,
         ArrayList<Polygon> polygons,
         ArrayList<Color> polygonFills,
         int[][] polygonEdges,
         ArrayList<ArrayList<Boolean>> polygonReverse)
         throws IOException, IllegalArgumentException {

      // write to save text file
      if (saveAs == null) throw new IllegalArgumentException("SaveAs is null");
      FileWriter fileW = new FileWriter(saveAs);
      BufferedWriter bw = new BufferedWriter(fileW);
      saveVertices(bw, vertices);
      bw.newLine();
      saveEdges(bw, edges);
      bw.newLine();
      saveFurniture(bw, furniture);
      bw.newLine();
      savePolygons(bw, polygons);
      bw.newLine();
      savePolygonFills(bw, polygonFills);
      bw.newLine();
      savePolygonEdges(bw, polygonEdges);
      bw.newLine();
      savePolygonReverse(bw, polygonReverse);
      bw.flush();
      bw.close();
      fileW.close();
   }

   /** Returns true if the rename was successful */
   private static boolean renameCoordsSaveFile(File saveFile, File renameTo) {
      try {
         boolean result = saveFile.renameTo(renameTo);
         if (!result) throw new SecurityException("write failed");
         
      } catch (SecurityException e) {
         /*write failed*/
         try {
            boolean deleteResult = renameTo.delete();
            if (!deleteResult) {
               System.err.println("Failure to build save file (jar) e1 " + e.getMessage());
               return false;
            }
            boolean retryresult = saveFile.renameTo(renameTo);
            if (!retryresult) {
                System.err.println("Failure to rename after delete");
                return false;
            }
         } catch (SecurityException f) {
            System.err.println("Failure to build save file (jar) e2");
            return false;
         }
      }

      return true;
   }

   /** lock images directory
     * coords save file has been written, rename it to temp
     * create jar with the old save file name
     * copy save text file in
     * copy images in
     * I need saveLocation, currname, the two images (if there are more throw error) */
   public static void buildJar(File saveFile, File fsScreenshot, File csScreenshot) {
      if (saveFile == null) {
          throw new IllegalArgumentException("null parameter");
      }

      File jarFile = new File(saveFile.getAbsolutePath());
      File renameTo = new File(saveFile.getParentFile(), saveFile.getName() + ".temp");
      File[] toJar;
      if (fsScreenshot == null || csScreenshot == null) {
         toJar = new File[] { renameTo };
      } else {
         toJar = new File[] { renameTo, fsScreenshot, csScreenshot };
      }
      
      boolean result = renameCoordsSaveFile(saveFile, renameTo);
      if (!result) {
         System.err.println("Error renaming save file to temp");
         return;
      }

      try {
         int BUFFER_SIZE = 10240;
         byte buffer[] = new byte[BUFFER_SIZE];
         
         FileOutputStream stream = new FileOutputStream(jarFile);
         JarOutputStream out = new JarOutputStream(stream, new Manifest());

         for (int i = 0; i < toJar.length; i++) {
           if (toJar[i] == null || !toJar[i].exists()
               || toJar[i].isDirectory())
             continue; // Just in case...
           //System.out.println("Adding " + toJar[i].getAbsolutePath());

           // Add archive entry

           JarEntry jarAdd = new JarEntry(toJar[i].getName());
           jarAdd.setTime(toJar[i].lastModified());
           out.putNextEntry(jarAdd);

           // Write file to archive
           FileInputStream in = new FileInputStream(toJar[i]);
           while (true) {
             int nRead = in.read(buffer, 0, buffer.length);
             if (nRead <= 0) break;
             out.write(buffer, 0, nRead);
           }
           in.close();
         }

         out.close();
         stream.close();

     } catch (Exception ex) {
        ex.printStackTrace();
        System.err.println("Error saving to jar: " + ex.getMessage());
     }

     try {
        boolean deleteresult = renameTo.delete();
        if (!deleteresult) {
           System.err.println("temp file has been left hanging around, it doesn't really matter.");
        }
     } catch (SecurityException e) {
        System.err.println("temp file has been left hanging around, it doesn't really matter.");
     }
   }

   /** Returns a Coords or throws an exception if there is a problem reading */
   public static Coords load(File file, File dirForImages,
         ObjectBrowser ob,
         String[] imagename,
         ArrayList<Polygon> polygons,
         ArrayList<Color> polygonFills,
         ArrayList<ArrayList<Edge>> polygonEdges,
         ArrayList<ArrayList<Boolean>> polygonReverse)
         throws IOException, Exception, IllegalArgumentException {
      if (file == null) throw new IllegalArgumentException("file is null");

       JarFile jarFile = new JarFile(file);

       if (jarFile.size() != 4 && jarFile.size() != 2) {
          throw new IllegalArgumentException("Given file is not the correct jar save file");
       }

       JarEntry img1 = null, img2 = null, coords = null;

       Enumeration enums = jarFile.entries();
       while (enums.hasMoreElements()) {
          JarEntry entry = (JarEntry) enums.nextElement();
          String name = entry.getName();

          if (name.equals("META-INF/MANIFEST.MF")) continue;

          if (name.endsWith(".png")) {
             if (img1 == null) {
                img1 = entry;
             } else if (img2 == null) {
                img2 = entry;
             } else {
                throw new IllegalArgumentException("Given file is not the correct jar save file (more than 2 images)");
             }
          } else {
             if (coords == null) coords = entry;
             else throw new IllegalArgumentException("Given file is not the correct jar save file (more than 2 coords)");
          }
       }

       if (coords == null) throw new IllegalArgumentException("Given file is not the correct jar save file (coords save missing)");
       if (!((img1 == null && img2 == null) || (img1 != null && img2 != null))) throw new IllegalArgumentException("Given file is not the correct jar save file (neither 0 or 2)");




       // LOAD COORDS information
       InputStream in = jarFile.getInputStream(coords);
       InputStreamReader isr = new InputStreamReader(in);
       BufferedReader br = new BufferedReader(isr);

       /*FileReader f = new FileReader(file);
       BufferedReader br = new BufferedReader(f);*/

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
       if (numFurniture < 0) {
          throw new Exception("The number of furniture items is either invalid or"
             + " not given in the file");
       }
       Furniture[] furniture = loadFurniture(br, numFurniture, ob);
       if (furniture == null) throw new Exception("Unable to load all furniture");


       /* Get Number of Polygons and load polygons */
       int numPolygons = loadHeaderNum(br);
       if (numPolygons < 0) {
          throw new Exception("The number of polygons is either invalid or"
             + " not given in the file");
       }
       boolean success = loadPolygons(br, numPolygons, polygons);
       if (!success) throw new Exception("Unable to load all polygons");


       /* Get Number of Fill Colours and load Fill Colours */
       int numPolygonFills = loadHeaderNum(br);
       if (numPolygonFills < 0) {
          throw new Exception("The number of polygonFills is either invalid or"
             + " not given in the file");
       }
       success = loadPolygonFills(br, numPolygonFills, polygonFills);
       if (!success) throw new Exception("Unable to load all polygonFills");


       /* Get Number of PolygonEdges and load PolygonEdges */
       int numPolygonEdges = loadHeaderNum(br);
       if (numPolygonEdges < 0) {
          throw new Exception("The number of polygonEdges is either invalid or"
             + " not given in the file");
       }
       int[][] polygonEdgesTemp = loadPolygonEdges(br, numPolygonEdges);
       if (polygonEdgesTemp == null) throw new Exception("Unable to load all polygonEdges");


       /* Get Number of PolygonReverse and load PolygonReverse */
       int numPolygonReverse = loadHeaderNum(br);
       if (numPolygonReverse < 0) {
          throw new Exception("The number of polygonReverse is either invalid or"
             + " not given in the file");
       }
       success = loadPolygonReverse(br, numPolygonReverse, polygonReverse);
       if (!success) throw new Exception("Unable to load all polygonReverse");


       br.close();
       isr.close();
       in.close();


       // LOAD IMAGES
       int BUFFER_SIZE = 10240;
       byte buffer[] = new byte[BUFFER_SIZE];

       if (img1 != null && img2 != null) {
          JarEntry[] images = {img1, img2};

          for (JarEntry e : images) {
             File writeto = new File(dirForImages, e.getName());
             FileOutputStream out = new FileOutputStream(writeto);
             in = jarFile.getInputStream(e);

             while (true) {
                int nRead = in.read(buffer, 0, buffer.length);
                if (nRead <= 0) break;
                out.write(buffer, 0, nRead);
             }

             in.close();
             out.close();
          }

          imagename[0] = img1.getName().substring(2, img1.getName().length());
       }


      /* Loading might not reach this stage */
      return new Coords(file, vertices, edges, furniture, ob, polygonEdgesTemp, polygonEdges);
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
         if (edges[i] == null || edges[i].length != 4) {
            throw new IllegalArgumentException("Edges array needs to be of size m by 2");
         }

         line = edges[i][0] + "," + edges[i][1] + "," + edges[i][2] + "," + edges[i][3];
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

   /** Writes the given polygon objects to the file */
   private static void savePolygons(BufferedWriter bw, ArrayList<Polygon> polygons) throws IOException {
      if (polygons == null) throw new IllegalArgumentException("Polygons is null");

      String line = polygons.size() + " Polygons";
      bw.write(line, 0, line.length());
      bw.newLine();

      for (int i = 0; i < polygons.size(); i++) {
         if (polygons.get(i) == null) {
            throw new IllegalArgumentException("Polygons array element is null");
         }

         Polygon p = polygons.get(i);
         line = "";

         int[] xpoints = p.xpoints;
         for (int k=0; k < xpoints.length; k++) {
            if (k != 0) line += ",";
            line += xpoints[k];
         }
         line += " ";

         int[] ypoints = p.ypoints;
         for (int k=0; k < ypoints.length; k++) {
            if (k != 0) line += ",";
            line += ypoints[k];
         }

         line += " " + p.npoints;

         bw.write(line, 0, line.length());
         bw.newLine();
      }
   }

   /** Writes the given polygonFills objects to the file */
   private static void savePolygonFills(BufferedWriter bw, ArrayList<Color> polygonFills) throws IOException {
      if (polygonFills == null) throw new IllegalArgumentException("PolygonFills is null");

      String line = polygonFills.size() + " PolygonFills";
      bw.write(line, 0, line.length());
      bw.newLine();

      for (int i = 0; i < polygonFills.size(); i++) {
         if (polygonFills.get(i) == null) {
            throw new IllegalArgumentException("PolygonFills array element is null");
         }

         line = Integer.toString(polygonFills.get(i).getRGB());
         
         bw.write(line, 0, line.length());
         bw.newLine();
      }
   }

   /** Writes the given polygonEdges objects to the file */
   private static void savePolygonEdges(BufferedWriter bw, int[][] polygonEdgesTemp) throws IOException {
      if (polygonEdgesTemp == null) throw new IllegalArgumentException("PolygonEdgesTemp is null");

      String line = polygonEdgesTemp.length + " PolygonEdges";
      bw.write(line, 0, line.length());
      bw.newLine();

      for (int i = 0; i < polygonEdgesTemp.length; i++) {
         if (polygonEdgesTemp[i] == null) {
            throw new IllegalArgumentException("PolygonEdgesTemp array element is null");
         }

         line = "";
         for (int k=0; k < polygonEdgesTemp[i].length; k++) {
            if (k != 0) line += ",";
            line += polygonEdgesTemp[i][k];
         }
         
         bw.write(line, 0, line.length());
         bw.newLine();
      }
   }

   /** Writes the given polygonReverse objects to the file */
   private static void savePolygonReverse(BufferedWriter bw, ArrayList<ArrayList<Boolean>> polygonReverse) throws IOException {
      if (polygonReverse == null) throw new IllegalArgumentException("polygonReverse is null");

      String line = polygonReverse.size() + " PolygonReverse";
      bw.write(line, 0, line.length());
      bw.newLine();

      for (int i = 0; i < polygonReverse.size(); i++) {
         if (polygonReverse.get(i) == null) {
            throw new IllegalArgumentException("PolygonReverse array element is null");
         }

         line = "";
         for (int k=0; k < polygonReverse.get(i).size(); k++) {
            if (k != 0) line += ",";
            line += polygonReverse.get(i).get(k);
         }

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
      int[][] edges = new int[numEdges][4];
      int edgeLoadCount = 0;

      while (edgeLoadCount < numEdges && (line = br.readLine()) != null && (split = line.split(",")) != null) {
         if (split.length == 4) {
            try {
               int v1 = Integer.parseInt(split[0]);
               int v2 = Integer.parseInt(split[1]);
               int ctrlX = Integer.parseInt(split[2]);
               int ctrlY = Integer.parseInt(split[3]);

               edges[edgeLoadCount][0] = v1;
               edges[edgeLoadCount][1] = v2;
               edges[edgeLoadCount][2] = ctrlX;
               edges[edgeLoadCount][3] = ctrlY;

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
   private static Furniture[] loadFurniture(BufferedReader br, int numFurniture, ObjectBrowser ob) throws IOException {
      if (numFurniture < 0) return null;

      String line;
      Furniture[] furniture = new Furniture[numFurniture];
      int furnitureLoadCount = 0;

      while (furnitureLoadCount < numFurniture && (line = br.readLine()) != null) {
         try {
            furniture[furnitureLoadCount] = new Furniture(line, ob);
            furnitureLoadCount++;
         } catch (IllegalArgumentException e) {
            // skip this line, it is corrupt
        	 System.out.println("Corrupt item found in file with error: " + e);
         }
      }

      if (furnitureLoadCount != numFurniture) return null;
      else return furniture;
   }

   /* Returns true if the load into given arraylist was successful */
   private static boolean loadPolygons(BufferedReader br, int numPolygons, ArrayList<Polygon> polygons) throws IOException {
      if (numPolygons < 0) return false;

      String line;
      int polygonLoadCount = 0;

      while (polygonLoadCount < numPolygons && (line = br.readLine()) != null) {
         try {
            String[] bigsplit = line.split(" ");
            if (bigsplit.length != 3) return false;

            String[] xpointssplit = bigsplit[0].split(",");
            String[] ypointssplit = bigsplit[1].split(",");

            int[] xpoints;
            int[] ypoints;
            int npoints;

            try {
               xpoints = new int[xpointssplit.length];
               for (int i=0; i < xpoints.length; i++) {
                  xpoints[i] = Integer.parseInt(xpointssplit[i]);
               }

               ypoints = new int[ypointssplit.length];
               for (int i=0; i < ypoints.length; i++) {
                  ypoints[i] = Integer.parseInt(ypointssplit[i]);
               }

               npoints = Integer.parseInt(bigsplit[2]);
            } catch (NumberFormatException e) {
               return false;
            }

            polygons.add(new Polygon(xpoints, ypoints, npoints));
            polygonLoadCount++;
         } catch (IllegalArgumentException e) {
            // skip this line, it is corrupt
        	 System.out.println("Corrupt item found in file with error: " + e);
         }
      }

      if (polygonLoadCount != numPolygons) return false;
      else return true;
   }

   /* Returns true if the load into given arraylist was successful */
   private static boolean loadPolygonFills(BufferedReader br, int numPolygonFills, ArrayList<Color> polygonFills) throws IOException {
      if (numPolygonFills < 0) return false;

      String line;
      int polygonFillLoadCount = 0;

      while (polygonFillLoadCount < numPolygonFills && (line = br.readLine()) != null) {
         try {
            try {
               polygonFills.add(new Color(Integer.parseInt(line)));
            } catch (NumberFormatException e) {
               return false;
            }
            polygonFillLoadCount++;
         } catch (IllegalArgumentException e) {
            // skip this line, it is corrupt
        	 System.out.println("Corrupt item found in file with error: " + e);
         }
      }

      if (polygonFillLoadCount != numPolygonFills) return false;
      else return true;
   }

   /* Returns true if the load into given arraylist was successful */
   private static int[][] loadPolygonEdges(BufferedReader br, int numPolygonEdges) throws IOException {
      if (numPolygonEdges < 0) return null;

      String line;
      int[][] polygonEdges = new int[numPolygonEdges][];
      int polygonEdgesLoadCount = 0;

      while (polygonEdgesLoadCount < numPolygonEdges && (line = br.readLine()) != null) {
         String[] split = line.split(",");
         polygonEdges[polygonEdgesLoadCount] = new int[split.length];
         try {
            for (int i=0; i < split.length; i++) {
               polygonEdges[polygonEdgesLoadCount][i] = Integer.parseInt(split[i]);
            }
         } catch (NumberFormatException e) {
            return null;
         }
         polygonEdgesLoadCount++;
      }

      if (polygonEdgesLoadCount != numPolygonEdges) return null;
      else return polygonEdges;
   }

   /* Returns true if the load into given arraylist was successful */
   private static boolean loadPolygonReverse(BufferedReader br, int numPolygonReverse, ArrayList<ArrayList<Boolean>> polygonReverse) throws IOException {
      if (numPolygonReverse < 0) return false;

      String line;
      int polygonReverseLoadCount = 0;

      while (polygonReverseLoadCount < numPolygonReverse && (line = br.readLine()) != null) {
         String[] split = line.split(",");
         ArrayList<Boolean> node = new ArrayList<Boolean>();
         polygonReverse.add(node);
         for (int i=0; i < split.length; i++) {
            node.add(Boolean.parseBoolean(split[i]));
         }
         polygonReverseLoadCount++;
      }

      if (polygonReverseLoadCount != numPolygonReverse) return false;
      else return true;
   }
}
