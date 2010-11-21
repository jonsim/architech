import java.util.*;
import java.awt.geom.*;
import java.awt.geom.Line2D.*;

/**
 *
 * @author James
 */
class Edge {
   private static Edge thisClass = new Edge();
   private Vertex v1;
   private Vertex v2;

   /** Creates a new edge from the given vertices and adds it to the coordStore.
    *  If null is given for a vertex then that vertex will be made at 0,0,0 */
   Edge(Vertex v1, Vertex v2, CoordStore coordStore) {
      if (v1 == null) v1 = new Vertex();
      if (v2 == null) v2 = new Vertex();

      this.v1 = coordStore.addVertex(v1.getX(), v1.getY(), v1.getZ(), this);
      this.v2 = coordStore.addVertex(v2.getX(), v2.getY(), v2.getZ(), this);
   }

   /** Creates a new blank edge and doesn't add it to the coordStore */
   Edge() {
      v1 = new Vertex();
      v2 = new Vertex();
   }

   /** Returns the line that represents the top down (2D) view of this line */
   public Line2D.Float topDownView() {
      return new Line2D.Float(v1.getX(), v1.getY(), v2.getX(), v2.getY());
   }

   /** Returns true if the given object is the same class type as this (Edge) */
   public static boolean isEdge(Object o) {
      if (o == null) return false;

      if (o.getClass().equals(thisClass.getClass())) return true;
      else return false;
   }

   /** Moves one end of this line to the new coordinates given. If the vertex
    *  end being moved is used by a different object too (i.e. the end of this
    *  line is snapped to the other object) then it will create a new vertex for
    *  the end of this line, to essentially unsnap (split) from that other
    *  object's vertex */
   public void vertexMoveOrSplit(CoordStore coordStore, boolean isV1, float x, float y, float z) {
      Vertex toMove = (isV1 ? v1 : v2);
      if (toMove.equals(x, y, z)) return;

      if (toMove.isUsed()) {
         // tell vertex its no longer used by this edge. if v1 == v2 then we
         // certainly don't want to remove the only vertex thats in use!
         if (v1 != v2) coordStore.removeUse(toMove, this);

         // make a new vertex & add new vertex to CoordStore if not already exists
         // set additional vertex use as this & replace edge vertex holder with new version
         if (isV1) v1 = coordStore.addVertex(x, y, z, this);
         else v2 = coordStore.addVertex(x, y, z, this);

      } else {
         // update coordinates, no other components are joined to this vertex
         coordStore.set(toMove, x, y, z);
      }
   }

   @Override
   public String toString() {
      //MANUAL EXPORT - return "new Edge(new Vertex("+v1.x+","+v1.y+","+v1.z+"), new Vertex("+v2.x+","+v2.y+","+v2.z+"), this);";
      return "v1:(" + v1.getX() + ", " + v1.getY() + ", " + v1.getZ() + ") v2:(" + v2.getX() + ", " + v2.getY() + ", " + v2.getZ() + ")";
   }
}