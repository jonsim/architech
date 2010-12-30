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
   private final Line2D.Float topDownView = new Line2D.Float();

   /** Creates a new edge from the given vertices. Doesn't add it to the coordStore.
    *  If null is given for a vertex then that vertex will be made at 0,0,0 */
   Edge(Coords coordStore, Coords.Vertex v1, Coords.Vertex v2) {
      if (v1 == null) v1 = coordStore.new Vertex();
      if (v2 == null) v2 = coordStore.new Vertex();

      this.v1 = v1;
      this.v2 = v2;

      //recalcTopDownView();
      topDownView.setLine(v1.getX(), v1.getY(), v2.getX(), v2.getY());
   }

   /** Returns the vertex at one end of this line */
   public Coords.Vertex getV1() {
      return v1;
   }

   /** Returns the vertex at the other end of this line */
   public Coords.Vertex getV2() {
      return v2;
   }

   /** Updates v1, refuses to update if you give it null */
   public void setV1(Coords.Vertex v1) {
      if (v1 == null) return;

      this.v1 = v1;
      recalcTopDownView();
   }

   /** Updates v2, refuses to update if you give it null */
   public void setV2(Coords.Vertex v2) {
      if (v2 == null) return;

      this.v2 = v2;
      recalcTopDownView();
   }

   /** Keeps the top down (2D) view of this line up to date */
   public void recalcTopDownView() {
      topDownView.setLine(v1.getX(), v1.getY(), v2.getX(), v2.getY());
   }

   /** Draws the 2D representation of this line on the given Graphics canvas */
   public void paint(Graphics2D g2) {
      g2.draw(topDownView);
   }

   /** Writes the length of this edge next to it on the given graphics canvas */
   public void paintLengthText(Graphics2D g2) {
      int x = Math.round((v1.getX() + v2.getX()) / 2);
      int y = Math.round((v1.getY() + v2.getY()) / 2);

      Font sanSerifFont = new Font("SanSerif", Font.PLAIN, 12);
      g2.setFont(sanSerifFont);
      g2.drawString(length() + "mm", x, y);
   }

   /** Returns the length of this line */
   public float length() {
      float a = Math.abs(v1.getX() - v2.getX());
      float b = Math.abs(v1.getY() - v2.getY());
      return (float) Math.sqrt(a*a + b*b);
   }

   public boolean contains(Point p, float lineWidth) {
      float a = (v2.getY() - v1.getY()) / (v2.getX() - v1.getX());
      float b = v1.getY() - a * v1.getX();

      if (Math.abs(p.y - (a * p.x + b)) < lineWidth) return true && topDownView.getBounds().contains(p);
      else return false;
   }

   /** Returns some vaguely useful form of string so you can println() an edge */
   @Override
   public String toString() {
      //MANUAL EXPORT - return "new Edge(new Vertex("+v1.x+","+v1.y+","+v1.z+"), new Vertex("+v2.x+","+v2.y+","+v2.z+"), this);";
      //return "new Edge(new Vertex("+v1.getX()+","+v1.getY()+","+v1.getZ()+"), new Vertex("+v2.getX()+","+v2.getY()+","+v2.getZ()+"), this);";
      return "v1:(" + v1.getX() + ", " + v1.getY() + ", " + v1.getZ() + ") v2:(" + v2.getX() + ", " + v2.getY() + ", " + v2.getZ() + ")";
   }
}