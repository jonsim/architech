import java.awt.geom.*;
import java.awt.geom.Line2D.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.ListIterator;

/** Holds an edge. When you make a new Edge() it gets automatically added to the
 *  internal coordinate system. This class remembers the two vertices at either
 *  end of the edge and each of those 2 vertices remembers that they are
 *  associated with this edge.
 */
public class Edge {
   private Coords.Vertex v1;
   private Coords.Vertex v2;
   private final Point2D.Float ctrl = new Point2D.Float();
   public final QuadCurve2D.Float topDownViewCurve = new QuadCurve2D.Float();
   private final static int curveCtrlSize = 6;
   private final Ellipse2D.Float curveCtrl = new Ellipse2D.Float();
   private final Line2D.Float tangent1 = new Line2D.Float();
   private final Line2D.Float tangent2 = new Line2D.Float();
   private final LinkedList<Furniture> doorWindow = new LinkedList<Furniture>();

   /** Creates a new edge from the given vertices. Doesn't add it to the coordStore.
    *  If null is given for a vertex then that vertex will be made at 0,0,0
    *  If ctrl is null the ctrl point will be at the middle, otherwise it is set */
   Edge(Coords.Vertex v1, Coords.Vertex v2, Point ctrl) {
      if (v1 == null || v2 == null) throw new IllegalArgumentException("null vertex");

      this.v1 = v1;
      this.v2 = v2;

      if (ctrl == null) resetCtrlPositionToHalfway();
      else setCtrl(ctrl);
   }

   /** Returns the vertex at one end of this line */
   public Coords.Vertex getV1() {
      return v1;
   }

   /** Returns the vertex at the other end of this line */
   public Coords.Vertex getV2() {
      return v2;
   }
   
   public QuadCurve2D.Float getqcurve()
   {
	   return topDownViewCurve;
   }

   /** Updates v1, refuses to update if you give it null. Also updates the ctrl
    *  point to halfway between the two points. When a new line is drawn this is
    *  called, and the new line has ctrl point 0,0 so it should be updated. */
   public void setV1(Coords.Vertex v1) {
      if (v1 == null) return;

      this.v1 = v1;
      resetCtrlPositionToHalfway();
   }

   /** Updates v2, refuses to update if you give it null. Also updates the ctrl
    *  point to halfway between the two points. When a new line is drawn this is
    *  called, and the new line has ctrl point 0,0 so it should be updated. */
   public void setV2(Coords.Vertex v2) {
      if (v2 == null) return;

      this.v2 = v2;
      resetCtrlPositionToHalfway();
   }

   /** Returns true if the point lies within the coordinates of the control point */
   public boolean curveCtrlContains(Point p) {
      Point temp = new Point();
      temp.setLocation(p.getX(), p.getY());
      return curveCtrl.contains(temp);
   }

   public boolean curveContains(Furniture f) {
      return topDownViewCurve.intersects( f.rectangle.getMinX(), f.rectangle.getMinY(), f.rectangle.getWidth(), f.rectangle.getHeight() );
   }

   /** Use setEdgeCtrl() in Coords! Updates ctrl, refuses to update if you give it null */
   public final void setCtrl(Point ctrl) {
      if (ctrl == null) return;

      this.ctrl.setLocation(ctrl.getX(), ctrl.getY());
      recalcTopDownView();
   }

   public final void addDoorWindow(Furniture f) {
      if( f == null || doorWindow.contains(f) ) return;

      doorWindow.add(f);
   }

   public final void deleteDoorWindow(Furniture f) {
      if( f == null || !doorWindow.contains(f) ) return;

      doorWindow.remove(f);
   }

   public void moveDoorWindow(Furniture f, Point newCenter) {
      if( f == null || !doorWindow.contains(f) ) return;

      f.set(newCenter);
   }

   public boolean containsDoorWindow(Furniture f) {
      return doorWindow.contains(f);
   }

   /** Returns the x coordinate of the ctrl point */
   public int getCtrlX() {
      return (int) Math.round(ctrl.getX());
   }

   /** Returns the y coordinate of the ctrl point */
   public int getCtrlY() {
      return (int) Math.round(ctrl.getY());
   }

   public Furniture[] getDoorWindow() {
      // return null if empty

      return doorWindow.toArray( new Furniture[0] );
   }

   public Furniture getDoorWindowAt(Point p) {
      ListIterator<Furniture> ite = doorWindow.listIterator();
      
      while( ite.hasNext() ) {
         Furniture dw = ite.next();
         if( dw.contains(p) ) return dw;
      }

      return null;
   }

   public boolean isStraight() {
      int xDiff = 0, yDiff = 0;

      if ( ctrl.getX() == ( ( v1.getX() + v2.getX() ) / 2 ) )
         xDiff++;
      else if ( ctrl.getX()+1 == ( ( v1.getX() + v2.getX() ) / 2 ) )
         xDiff++;
      else if ( ctrl.getX()-1 == ( ( v1.getX() + v2.getX() ) / 2 ) )
         xDiff++;

      if ( ctrl.getY() == ( ( v1.getY() + v2.getY() ) / 2 ) )
         yDiff++;
      else if ( ctrl.getY()+1 == ( ( v1.getY() + v2.getY() ) / 2 ) )
         yDiff++;
      else if ( ctrl.getY()-1 == ( ( v1.getY() + v2.getY() ) / 2 ) )
         yDiff++;

      if( xDiff == 1 & yDiff == 1 )
         return true;

      return false;
   }

   public boolean doorWindowAt(Point p) {
      ListIterator<Furniture> ite = doorWindow.listIterator();
      
      while( ite.hasNext() ) {
         if( ite.next().contains(p) ) return true;
      }

      return false;
   }

   /** Places the ctrl point halfway between v1 and v2 */
   public final void resetCtrlPositionToHalfway() {
      ctrl.setLocation( ( v1.getX() + v2.getX() ) / 2, ( v1.getY() + v2.getY() ) / 2 );
      recalcTopDownView();
   }

   /** Keeps the top down (2D) view of this line up to date */
   public final void recalcTopDownView() {
      topDownViewCurve.setCurve(v1.getX(), v1.getY(), ctrl.getX(), ctrl.getY(), v2.getX(), v2.getY());

      curveCtrl.setFrame(ctrl.getX() - (curveCtrlSize/2), ctrl.getY() - (curveCtrlSize/2), curveCtrlSize, curveCtrlSize);

      tangent1.setLine(v1.getX(), v1.getY(), ctrl.getX(), ctrl.getY());
      tangent2.setLine(v2.getX(), v2.getY(), ctrl.getX(), ctrl.getY());
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

      ListIterator<Furniture> ite = doorWindow.listIterator();
      while ( ite.hasNext() ) {
         ite.next().paint(g2);
      }
   }

   /** Writes the length of this edge next to it on the given graphics canvas */
   public void paintLengthText(Graphics2D g2) {
      int x = Math.round((int) (v1.getX() + v2.getX()) / 2);
      int y = Math.round((int) (v1.getY() + v2.getY()) / 2);

      //Font sanSerifFont = new Font("SanSerif", Font.PLAIN, 12);
      //g2.setFont(sanSerifFont);
      g2.drawString(length() + "mm", x, y);
   }

   /** Returns the length of this line */
   public float length() {
      //float a = Math.abs(v1.getX() - v2.getX());
      //float b = Math.abs(v1.getY() - v2.getY());
      //return (float) Math.sqrt(a*a + b*b);
      double length = 0;
      double x;
      double y;
      PathIterator ite = topDownViewCurve.getPathIterator(null, 2.0);
      double[] segment = new double[2];
      ite.currentSegment(segment);
      ite.next();
      x = segment[0];
      y = segment[1];
      while(!ite.isDone()) {
         ite.currentSegment(segment);
         length += Math.sqrt(Math.pow((segment[0] - x),2)
                   + Math.pow((segment[1] - y),2));
         x = segment[0];
         y = segment[1];
         ite.next();
      }
      return (float)length;
   }

   /** Returns some vaguely useful form of string so you can println() an edge */
   @Override
   public String toString() {
      //return "new Edge(new Vertex("+v1.getX()+","+v1.getY()+","+v1.getZ()+"), new Vertex("+v2.getX()+","+v2.getY()+","+v2.getZ()+"), this);";
      String str = "Edge " + this.hashCode() + " - v1:(" + v1.getX() + ", " + v1.getY() + ", " + v1.getZ() + ") v2:(" + v2.getX() + ", " + v2.getY() + ", " + v2.getZ() + ")";
      for( Furniture f : doorWindow )
         str = str + f;

      return str;
   }
}