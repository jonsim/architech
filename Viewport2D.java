import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D.*;
import java.util.*;

/** BUG: If we add a curve on drag tool to a different mouse button than the line
 *       drag tool then you can draw both at once.
 *
 * @author James
 */
public class Viewport2D extends JPanel implements Scrollable, MouseListener, MouseMotionListener {

   private Main main;
   private Graphics2D g2; // so paintComponent() doesn't have to create it each time
   private Edge dragEdge;
   private Coords.Vertex hoverVertex;
   private JScrollPane scrollPane;

   Viewport2D(Main main) {
      this.main = main;
      initPane();
      scrollPane = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
   }

   private void initPane() {
      setBackground(Color.WHITE);
      //setBorder(BorderFactory.createLineBorder(Color.GRAY));
      setPreferredSize(new Dimension(2000,1000));
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

   public JScrollPane getScrollPane() {
      return scrollPane;
   }

   /** Currently draws lines twice, as vertices at each end are traversed.
    *  Its not a performance issue at the moment */
   @Override
   public void paintComponent(Graphics g) {
      g2 = (Graphics2D) g;
      super.paintComponent(g2); // clear screen
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON);

      ListIterator<Coords.Vertex> ite = main.coordStore.getVerticesIterator();
      while (ite.hasNext()) {
         Coords.Vertex v = ite.next();

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
   
   private Coords.Vertex screenToWorldCoords(Point screen) {
      return main.coordStore.new Vertex(screen.x, screen.y, 0);
   }

   /* returns the first vertex that the point lies within, or null if none */
   private Coords.Vertex hoverVertex(Point p) {
      ListIterator<Coords.Vertex> iterator = main.coordStore.getVerticesIterator();
      while (iterator.hasNext()) {
         Coords.Vertex v = iterator.next();
         if (v.topDownView().contains(p)) return v;
      }
      return null;
   }

   private void lineDragStarted(Point p) {
      if (p == null) p = new Point(0,0);
      dragEdge = new Edge(main.coordStore, screenToWorldCoords(p), screenToWorldCoords(p));
   }

   /** No reason other than stopping erroneous  */
   private void lineDragFinished() {
      dragEdge = null;
      setPreferredSize(new Dimension(200,200));
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

   /**! START SCROLLABLE */
   /** Returns the preferred size of the viewport for a view component. For
    * example the preferredSize of a JList component is the size required to
    * accommodate all of the cells in its list however the value of
    * preferredScrollableViewportSize is the size required for JList.getVisibleRowCount()
    * rows. A component without any properties that would effect the viewport
    * size should just return getPreferredSize() here. */
   public Dimension getPreferredScrollableViewportSize() {
      int width = 1;//400;
      int height = 1;//180;
      

      return new Dimension(width, height);
      //return getSize();
   }

   /** Components that display logical rows or columns should compute the scroll
    * increment that will completely expose one block of rows or columns,
    * depending on the value of orientation.
    *
    * Scrolling containers, like JScrollPane, will use this method each time the
    * user requests a block scroll. */
   public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      // visibleRect - The view area visible within the viewport
      // orientation - Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
      // direction - Less than zero to scroll up/left, greater than zero for down/right.

      return 150;
   }

   /** Return true if a viewport should always force the height of this Scrollable
    * to match the height of the viewport. For example a columnar text view that
    * flowed text in left to right columns could effectively disable vertical
    * scrolling by returning true here.
    *
    * Scrolling containers, like JViewport, will use this method each time they
    * are validated. */
   public boolean getScrollableTracksViewportHeight() {

      return false;
   }

   /** Return true if a viewport should always force the width of this Scrollable
    * to match the width of the viewport. For example a normal text view that
    * supported line wrapping would return true here, since it would be undesirable
    * for wrapped lines to disappear beyond the right edge of the viewport. Note
    * that returning true for a Scrollable whose ancestor is a JScrollPane
    * effectively disables horizontal scrolling.
    *
    * Scrolling containers, like JViewport, will use this method each time they
    * are validated. */
   public boolean getScrollableTracksViewportWidth() {

      return false;
   }

   /** Components that display logical rows or columns should compute the scroll
    * increment that will completely expose one new row or column, depending on
    * the value of orientation. Ideally, components should handle a partially
    * exposed row or column by returning the distance required to completely
    * expose the item.
    *
    * Scrolling containers, like JScrollPane, will use this method each time the
    * user requests a unit scroll. */
   public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      // visibleRect - The view area visible within the viewport
      // orientation - Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
      // direction - Less than zero to scroll up/left, greater than zero for down/right.

      return 15;
   }
   /**! END SCROLLABLE */
}
