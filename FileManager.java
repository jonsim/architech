
import java.io.*;

/** Isn't currently reusable!
 *
 */
public class FileManager {
   private BufferedWriter bw;

   /**  */
   FileManager(File saveAs) {
      if (saveAs == null) Main.showFatalExceptionTraceWindow(
              new Exception("The given file object is not usable to save or load"));

      try {
         FileWriter fileW = new FileWriter(saveAs);
         bw = new BufferedWriter(fileW);

      } catch (IOException e) {
         // IOException while saving to temporary file
      }
   }

   /** Returns a string in the form E,3,2 where 3 and 2 are the vertex indices.
    *  Returns a blank string if the vertex indices could not be found */
   private String buildEdgeString(Coords.Vertex[] vertices, Edge e) {
      int v1Index = -1, v2Index = -1;

      for (int i = 0; i < vertices.length; i++) {
         if (vertices[i] == e.getV1()) {
            v1Index = i;
            if (v2Index != -1) {
               break;
            }
         }

         if (vertices[i] == e.getV2()) {
            v2Index = i;
            if (v1Index != -1) {
               break;
            }
         }
      }

      if (v1Index == -1 || v2Index == -1) {
         Main.showFatalExceptionTraceWindow(
                 new Exception("The vertex ends for this edge could not be found"));
      }

      return "E," + v1Index + "," + v2Index;
   }

   /** Write a portion of a String. */
   public void write(Coords.Vertex[] vertices, Object[] objects) throws IOException {
      String line;

      bw.write("Vertices", 0, 8);
      bw.newLine();

      for (int i = 0; i < vertices.length; i++) {
         line = i + "," + vertices[i].getX() + "," + vertices[i].getY() + "," + vertices[i].getZ();
         bw.write(line, 0, line.length());
         bw.newLine();
      }

      bw.newLine();

      bw.write("Objects", 0, 7);
      bw.newLine();

      for (int i = 0; i < objects.length; i++) {
         if (Edge.isEdge(objects[i])) {
            line = buildEdgeString(vertices, (Edge) objects[i]);

         } else {
            // no need to write other object types to file yet
            continue;
         }

         bw.write(line, 0, line.length());
         bw.newLine();
      }

      bw.flush();
   }
}
