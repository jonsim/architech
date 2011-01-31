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
   private final Point2D.Float ctrl = new Point2D.Float();
   private final QuadCurve2D.Float topDownViewCurve = new QuadCurve2D.Float();
   private final static int curveCtrlSize = 6;
   private final Ellipse2D.Float curveCtrl = new Ellipse2D.Float();
   private final Line2D.Float tangent1 = new Line2D.Float();
   private final Line2D.Float tangent2 = new Line2D.Float();

   /** Creates a new edge from the given vertices. Doesn't add it to the coordStore.
    *  If null is given for a vertex then that vertex will be made at 0,0,0
    *  If ctrl is null the ctrl point will be at the middle, otherwise it is set */
   Edge(Coords.Vertex v1, Coords.Vertex v2, Point ctrl, double zoomScale) {
      if (v1 == null || v2 == null) throw new IllegalArgumentException("null vertex");

      this.v1 = v1;
      this.v2 = v2;

      if (ctrl == null) resetCtrlPositionToHalfway(zoomScale);
      else setCtrl(ctrl, zoomScale);
   }

   /** Returns the vertex at one end of this line */
   public Coords.Vertex getV1() {
      return v1;
   }

   /** Returns the vertex at the other end of this line */
   public Coords.Vertex getV2() {
      return v2;
   }

   /** Updates v1, refuses to update if you give it null. Also updates the ctrl
    *  point to halfway between the two points. When a new line is drawn this is
    *  called, and the new line has ctrl point 0,0 so it should be updated. */
   public void setV1(Coords.Vertex v1, double zoomScale) {
      if (v1 == null) return;

      this.v1 = v1;
      resetCtrlPositionToHalfway(zoomScale);
   }

   /** Updates v2, refuses to update if you give it null. Also updates the ctrl
    *  point to halfway between the two points. When a new line is drawn this is
    *  called, and the new line has ctrl point 0,0 so it should be updated. */
   public void setV2(Coords.Vertex v2, double zoomScale) {
      if (v2 == null) return;

      this.v2 = v2;
      resetCtrlPositionToHalfway(zoomScale);
   }

   /** Returns true if the point lies within the coordinates of the control point */
   public boolean curveCtrlContains(Point p, double zoomScale) {
      Point temp = new Point();
      temp.setLocation(p.getX()*zoomScale, p.getY()*zoomScale);
      return curveCtrl.contains(temp);
   }

   /** Use setEdgeCtrl() in Coords! Updates ctrl, refuses to update if you give it null */
   public final void setCtrl(Point ctrl, double zoomScale) {
      if (ctrl == null) return;

      this.ctrl.setLocation(ctrl.getX(), ctrl.getY());
      recalcTopDownView(zoomScale);
   }

   /** Returns the x coordinate of the ctrl point */
   public int getCtrlX() {
      return (int) Math.round(ctrl.getX());
   }

   /** Returns the y coordinate of the ctrl point */
   public int getCtrlY() {
      return (int) Math.round(ctrl.getY());
   }

   /** Places the ctrl point halfway between v1 and v2 */
   public final void resetCtrlPositionToHalfway(double zoomScale) {
      ctrl.setLocation( ( v1.getX() + v2.getX() ) / 2, ( v1.getY() + v2.getY() ) / 2 );
      recalcTopDownView(zoomScale);
   }

   /** Keeps the top down (2D) view of this line up to date */
   public final void recalcTopDownView(double zoomScale) {
      topDownViewCurve.setCurve(zoomScale*v1.getX(), zoomScale*v1.getY(), zoomScale*ctrl.getX(), zoomScale*ctrl.getY(), zoomScale*v2.getX(), zoomScale*v2.getY());

      curveCtrl.setFrame(zoomScale*ctrl.getX() - (curveCtrlSize/2), zoomScale*ctrl.getY() - (curveCtrlSize/2), curveCtrlSize, curveCtrlSize);

      tangent1.setLine(zoomScale*v1.getX(), zoomScale*v1.getY(), zoomScale*ctrl.getX(), zoomScale*ctrl.getY());
      tangent2.setLine(zoomScale*v2.getX(), zoomScale*v2.getY(), zoomScale*ctrl.getX(), zoomScale*ctrl.getY());
   }

   /** Draws the 2D representation of this line on the given Graphics canvas */
   public void paint(Graphics2D g2, boolean isCurveTool) {
      if (isCurveTool) {
         Color oldColour = g2.getColor();

         // paints the tangents
         g2.setColor(Color.RED);
         g2.draw(tangent1);
         g2.draw(tangent2);

         // paint the curve
         g2.setColor(oldColour);
         g2.draw(topDownViewCurve);

         // paint the control circle
         g2.setColor(Color.RED);
         g2.draw(curveCtrl);

         g2.setColor(oldColour);
         
      } else g2.draw(topDownViewCurve);
   }

   /** Writes the length of this edge next to it on the given graphics canvas */
   public void paintLengthText(Graphics2D g2, double zoomScale) {
      int x = Math.round((int) (v1.getX()*zoomScale + v2.getX()*zoomScale) / 2);
      int y = Math.round((int) (v1.getY()*zoomScale + v2.getY()*zoomScale) / 2);

      //Font sanSerifFont = new Font("SanSerif", Font.PLAIN, 12);
      //g2.setFont(sanSerifFont);
      g2.drawString(length() + "mm", x, y);
   }

   /** Returns the length of this line */
   public float length() {
      float a = Math.abs(v1.getX() - v2.getX());
      float b = Math.abs(v1.getY() - v2.getY());
      return (float) Math.sqrt(a*a + b*b);
   }

   /** Returns some vaguely useful form of string so you can println() an edge */
   @Override
   public String toString() {
      //return "new Edge(new Vertex("+v1.getX()+","+v1.getY()+","+v1.getZ()+"), new Vertex("+v2.getX()+","+v2.getY()+","+v2.getZ()+"), this);";
      return "Edge " + this.hashCode() + " - v1:(" + v1.getX() + ", " + v1.getY() + ", " + v1.getZ() + ") v2:(" + v2.getX() + ", " + v2.getY() + ", " + v2.getZ() + ")";
   }
}