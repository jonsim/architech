import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.geom.Line2D.*;
import java.util.*;

/** BUG: If we add a curve on drag tool to a different mouse button than the line
 *       drag tool then you can draw both at once.
 *
 * @author James
 */
public class Viewport2D extends JPanel implements MouseListener, MouseMotionListener {

   private Main main;
   private Graphics2D g2; // so paintComponent() doesn't have to create it each time
   private Edge dragEdge;
   private Vertex hoverVertex;

   Viewport2D(Main main) {
      this.main = main;
      initPane();
   }

   private void initPane() {
      this.setBackground(Color.WHITE);
      this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
   }

   /** UNUSED */
   private void graphicsInit(Graphics2D g2) {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON);
      
      int thickness = 1;
      BasicStroke stroke = new BasicStroke(thickness, BasicStroke.CAP_ROUND,
         BasicStroke.JOIN_ROUND);
      g2.setStroke(stroke);
      
   }

   private Vertex screenToWorldCoords(Point screen) {
      return new Vertex(screen.x, screen.y, 0);
   }

   /* returns the first vertex that the point lies within, or null if none */
   private Vertex hoverVertex(Point p) {
      ListIterator<Vertex> iterator = main.coordStore.getVerticesIterator();
      while (iterator.hasNext()) {
         Vertex v = iterator.next();
         if (v.topDownView().contains(p)) return v;
      }
      return null;
   }

   private void lineDragStarted(Point p) {
      if (p == null) p = new Point(0,0);
      dragEdge = new Edge(screenToWorldCoords(p), screenToWorldCoords(p), main.coordStore);
   }

   /** No reason other than stopping erroneous  */
   private void lineDragFinished() {
      dragEdge = null;
   }

   /** edge might be null if the user started a drag with a different mouse
       button than BUTTON1 and then while dragging changed to the line tool */
   private void lineDragEvent(Point draggedTo) {
      if (dragEdge != null) dragEdge.vertexMoveOrSplit(main.coordStore, false, draggedTo.x, draggedTo.y, 0);
   }

   /**! START MOUSELISTENER */
   /** Invoked when a mouse button has been pressed on a component. */
   public void mousePressed(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {
//hoverVertex(e.getPoint())
         if (main.designButtons.isLineTool()) {
            lineDragStarted(e.getPoint());
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            repaint();
         }

         // other tools go here
      }
   }

   /** Invoked when a mouse button has been released on a component. */
   public void mouseReleased(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {
         lineDragFinished();

         setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         
         // other tools go here
      }
   }

   /** Invoked when the mouse button has been clicked (pressed and released) on a component. */
   public void mouseClicked(MouseEvent e) {
   }

   /** Invoked when the mouse enters a component. */
   public void mouseEntered(MouseEvent e) {

   }

   /** Invoked when the mouse exits a component. */
   public void mouseExited(MouseEvent e) {

   }
   /**! END MOUSELISTENER */

   /**! START MOUSEMOTIONLISTENER */
   /** Invoked when a mouse button is pressed on a component and then dragged. */
   public void mouseDragged(MouseEvent e) {
      if (main.designButtons.isLineTool()) lineDragEvent(e.getPoint());
      repaint();
   }

   /** Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed. */
   public void mouseMoved(MouseEvent e) {
      hoverVertex = hoverVertex(e.getPoint());
      repaint();
   }
   /**! END MOUSEMOTIONLISTENER */

   /** Currently draws lines twice, as vertices at each end are traversed.
    *  Its not a performance issue at the moment */
   @Override
   public void paintComponent(Graphics g) {
      g2 = (Graphics2D) g;
      super.paintComponent(g2); // clear screen
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON);

      ListIterator<Vertex> ite = main.coordStore.getVerticesIterator();
      while (ite.hasNext()) {
         Vertex v = ite.next();

         ListIterator vi = v.getUsesIterator();
         while (vi.hasNext()) {
            Object use = vi.next();
            if (Edge.isEdge(use)) {
               g2.draw(((Edge) use).topDownView());

            } else {
               // Shapes other than edges are not visualised at the moment.
            }
         }
         if (v == hoverVertex) {
            g2.setColor(Color.red);
            g2.fill(v.topDownView());
            g2.setColor(Color.black);
         } else g2.fill(v.topDownView());
      }
   }
}
