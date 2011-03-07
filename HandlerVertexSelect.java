import java.awt.*;
import java.util.*;

/**
 *
 * @author James
 */
public class HandlerVertexSelect {
   private final Coords coords;

   private ArrayList<Coords.Vertex> selectedArray = new ArrayList<Coords.Vertex>();
   private ArrayList<Edge> wallEdges = new ArrayList<Edge>();
   private boolean cycleFormed = false;
   private int selCount = 0;

   HandlerVertexSelect(Coords coords) {
      if (coords == null) throw new IllegalArgumentException("null coords");
      this.coords = coords;
   }

   /** User has clicked (mouse released, doing it on mouse press is really hard
    *  as moving vertices also uses mouse press and the select tool so its a bit
    *  ambiguous! Mouse release was a lot easier) */
   public void click(Point p) {
      Coords.Vertex v = coords.vertexAt(p);
      
      System.out.println("click v="+v);

      if (v != null) {
         // add the vertex
         
      } else {
         // reset
      }
   }

   /** As your class handles selection it makes sense to have control of deletes :) */
   public void deleteSelected() {
      System.out.println("delete");

      // clear arraylists
      
      //coords.delete(selectVertices);
   }
}
