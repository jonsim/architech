import java.awt.geom.*;
import java.awt.geom.Line2D.*;
import java.awt.*;

/** Holds an edge. When you make a new Edge() it gets automatically added to the
 *  internal coordinate system. This class remembers the two vertices at either
 *  end of the edge and each of those 2 vertices remembers that they are
 *  associated with this edge.
 */
public class Edge {
   private Coords.Vertex v1;
   private Coords.Vertex v2;

   /** Creates a new edge from the given vertices and adds it to the coordStore.
    *  If null is given for a vertex then that vertex will be made at 0,0,0 */
   Edge(Coords coordStore, Coords.Vertex v1, Coords.Vertex v2, boolean snapToGrid) {
      if (v1 == null) v1 = coordStore.new Vertex();
      if (v2 == null) v2 = coordStore.new Vertex();

      this.v1 = coordStore.addVertex(v1.getX(), v1.getY(), v1.getZ(), this, snapToGrid);
      this.v2 = coordStore.addVertex(v2.getX(), v2.getY(), v2.getZ(), this, snapToGrid);
   }

   /** Creates an edge with the given endpoints, does not set uses or add it to
    *  the coordStore. Only for use by the Coords class */
   Edge(Coords.Vertex v1, Coords.Vertex v2) {
      if (v1 == null || v2 == null) throw new IllegalArgumentException("null argument");
      this.v1 = v1;
      this.v2 = v2;
   }

   public Coords.Vertex getV1() {
      return v1;
   }

   public Coords.Vertex getV2() {
      return v2;
   }

   /** Returns the line that represents the top down (2D) view of this line */
   public Line2D.Float topDownView() {
      return new Line2D.Float(v1.getX(), v1.getY(), v2.getX(), v2.getY());
   }

   /** Moves one end of this line to the new coordinates given. If the vertex
    *  end being moved is used by a different object too (i.e. the end of this
    *  line is snapped to the other object) then it will create a new vertex for
    *  the end of this line, to essentially unsnap (split) from that other
    *  object's vertex */
   public void vertexMoveOrSplit(Coords coordStore, boolean isV1, float x, float y, float z, boolean snapToGrid) {
      Coords.Vertex toMove = (isV1 ? v1 : v2);
      Coords.Vertex newV;

      // tell vertex its no longer used by this edge. if v1 == v2 then we
      // certainly don't want to remove the only vertex thats in use!
      if (v1 != v2) coordStore.removeUsexxx(toMove, this);

      // make a new vertex & add new vertex to CoordStore if not already exists
      // set additional vertex use as this & replace edge vertex holder with new version
      newV = coordStore.addVertex(x, y, z, this, snapToGrid);

      if (isV1) v1 = newV;
      else v2 = newV;
   }

   public void paintLengthText(Graphics2D g2) {
      int x = Math.round((v1.getX() + v2.getX()) / 2);
      int y = Math.round((v1.getY() + v2.getY()) / 2);

      Font sanSerifFont = new Font("SanSerif", Font.PLAIN, 12);
      g2.setFont(sanSerifFont);
      g2.drawString(java.lang.Float.toString(length()), x, y);
   }

   public float length() {
      float a = Math.abs(v1.getX() - v2.getX());
      float b = Math.abs(v1.getY() - v2.getY());
      return (float) Math.sqrt(a*a + b*b);
   }

   /** Returns some vaguely useful form of string so you can println() an edge */
   @Override
   public String toString() {
      //MANUAL EXPORT - return "new Edge(new Vertex("+v1.x+","+v1.y+","+v1.z+"), new Vertex("+v2.x+","+v2.y+","+v2.z+"), this);";
      //return "new Edge(new Vertex("+v1.getX()+","+v1.getY()+","+v1.getZ()+"), new Vertex("+v2.getX()+","+v2.getY()+","+v2.getZ()+"), this);";
      return "v1:(" + v1.getX() + ", " + v1.getY() + ", " + v1.getZ() + ") v2:(" + v2.getX() + ", " + v2.getY() + ", " + v2.getZ() + ")";
   }
}