import java.awt.geom.*;
import java.awt.geom.Line2D.*;

/** Holds an edge. When you make a new Edge() it gets automatically added to the
 *  internal coordinate system. This class remembers the two vertices at either
 *  end of the edge and each of those 2 vertices remembers that they are
 *  associated with this edge.
 */
class Edge {
   // as this is static it only gets defined once to save space. Its used to
   // detect if a Object has class type Edge by comparing with this variable.
   private static final Edge thisClass = new Edge(new Coords(), null, null);

   private Coords.Vertex v1;
   private Coords.Vertex v2;

   /** Creates a new edge from the given vertices and adds it to the coordStore.
    *  If null is given for a vertex then that vertex will be made at 0,0,0 */
   Edge(Coords coordStore, Coords.Vertex v1, Coords.Vertex v2) {
      if (v1 == null) v1 = coordStore.new Vertex();
      if (v2 == null) v2 = coordStore.new Vertex();

      this.v1 = coordStore.addVertex(v1.getX(), v1.getY(), v1.getZ(), this);
      this.v2 = coordStore.addVertex(v2.getX(), v2.getY(), v2.getZ(), this);
   }

   public void delete(Coords coordStore) {
      coordStore.removeUse(v1, this);
      coordStore.removeUse(v2, this);
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
   public void vertexMoveOrSplit(Coords coordStore, boolean isV1, float x, float y, float z) {
      Coords.Vertex toMove = (isV1 ? v1 : v2);
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
         if (isV1) v1 = coordStore.set(toMove, x, y, z);
         else v2 = coordStore.set(toMove, x, y, z);
      }
   }

   /** Returns some vaguely useful form of string so you can println() an edge */
   @Override
   public String toString() {
      //MANUAL EXPORT - return "new Edge(new Vertex("+v1.x+","+v1.y+","+v1.z+"), new Vertex("+v2.x+","+v2.y+","+v2.z+"), this);";
      return "v1:(" + v1.getX() + ", " + v1.getY() + ", " + v1.getZ() + ") v2:(" + v2.getX() + ", " + v2.getY() + ", " + v2.getZ() + ")";
   }
}