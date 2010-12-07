
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D.*;
import java.util.*;

/** This class holds the panel for the 2D view, it extends JPanel so has repaint()
 *  and all the other methods to do with JPanel. It draws the vertices and edges
 *  from the coordinate system as required and responds to button pressed from
 *  the DesignButtons class.
 *
 *  Future bug to watch out for: If we add a curve on drag tool to a different
 *  mouse button than the line drag tool then you can draw both at once.
 */
public class Viewport2D extends JPanel implements KeyListener, Scrollable,
      MouseListener, MouseMotionListener, ComponentListener {

   private Main main;
   private Graphics2D g2; // so paintComponent() doesn't have to create it each time
   private Edge dragEdge;
   private Coords.Vertex hoverVertex, selectVertex = null;
   private JScrollPane scrollPane;

   /** Initialises the 2D pane and makes it scrollable too */
   Viewport2D(Main main) {
      this.main = main;
      initPane();
      setFocusable(true);
      addKeyListener(this);
      scrollPane = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      addMouseListener(this);
      addMouseMotionListener(this);
   }

   /** Does little at the moment */
   private void initPane() {
      setBackground(Color.WHITE);
      setPreferredSize(new Dimension(2000, 1000));
   }

   /** Returns the scrollable version of this class. */
   public JScrollPane getScrollPane() {
      return scrollPane;
   }

   /** Draws the grid on the given Graphics canvas */
   private void drawGrid(Graphics2D g2) {
      if (!main.designButtons.isGridOn()) {
         return;
      }

      g2.setColor(Color.LIGHT_GRAY);

      int cellWidth = 60;

      for (int i = cellWidth; i < getWidth(); i += cellWidth) {
         g2.drawLine(i, 0, i, getHeight());
      }

      for (int i = cellWidth; i < getHeight(); i += cellWidth) {
         g2.drawLine(0, i, getWidth(), i);
      }
   }

   /** Draws grid, objects and vertices, highlights currently aimed at vertex */
   @Override
   public void paintComponent(Graphics g) {
      g2 = (Graphics2D) g;
      super.paintComponent(g2); // clear screen
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);

      drawGrid(g2);

      main.coordStore.drawObjects(g2);
      main.coordStore.drawVertices(g2);

      if (hoverVertex != null) {
         g2.setColor(Color.red);
         g2.fill(hoverVertex.topDownView());
      }

      if (selectVertex != null && main.designButtons.isSelectTool()) {
         g2.setColor(Color.blue);
         g2.fill(selectVertex.topDownView());
      }
   }

   /** I don't like this method :) but anyway is converts from screen coordinates
    *  i.e. 0 to panelWidth into the full on coordinate system values between
    *  -inf and +inf. This method is currently just hard wired so it doesn't
    *  really do anything useful yet */
   private Coords.Vertex screenToWorldCoords(Point screen) {
      return main.coordStore.new Vertex(screen.x, screen.y, 0);
   }

   /** if the user clicks and drags with the line tool on, call this, it will
    *  make the new edge so that they can see it as they drag! */
   private void lineDragStarted(Point p) {
      if (p == null) {
         p = new Point(0, 0);
      }
      dragEdge = new Edge(main.coordStore, screenToWorldCoords(p), screenToWorldCoords(p));
   }

   /** No reason other than stopping erroneous  */
   private void lineDragFinished() {
      dragEdge = null;
      setPreferredSize(new Dimension(200, 200));
   }

   /** edge might be null if the user started a drag with a different mouse
    *  button than BUTTON1 and then while dragging changed to the line tool. This
    *  updates the edge to a new position, that the user has just dragged it to */
   private void lineDragEvent(Point draggedTo) {
      if (dragEdge != null) {
         dragEdge.vertexMoveOrSplit(main.coordStore, false, draggedTo.x, draggedTo.y, 0);
      }
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
      if (e.getButton() == MouseEvent.BUTTON1) {
         if (main.designButtons.isSelectTool()) {
            selectVertex = main.coordStore.vertexAt(e.getPoint());
            repaint();
         }
      }

      main.viewport2D.requestFocus();
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
      if (main.designButtons.isLineTool()) {
         lineDragEvent(e.getPoint());
      }
      repaint();
   }

   /** Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed. */
   public void mouseMoved(MouseEvent e) {
      hoverVertex = main.coordStore.vertexAt(e.getPoint());
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
   /** Invoked when a key is pressed and released */
   public void keyTyped(KeyEvent kevt) {
      char c = kevt.getKeyChar();

      if ((c == '\b' || c == '\u007F') && selectVertex != null) {
         main.coordStore.delete(selectVertex);
         repaint();
      }
   }

   /** Invoked when a key is pressed */
   public void keyPressed(KeyEvent kevt) {
   }

   /** Invoked when a key is released */
   public void keyReleased(KeyEvent kevt) {
   }

   /**! START COMPONENTLISTENER */
   /** Invoked when the component has been made invisible. */
   public void componentHidden(ComponentEvent e) {

   }

   /** Invoked when the component's position changes. */
   public void componentMoved(ComponentEvent e) {

   }

   /** Invoked when the component's size changes. */
   public void componentResized(ComponentEvent e) {

   }

   /** Invoked when the component has been made visible. */
   public void componentShown(ComponentEvent e) {
      
   }
   /**! END COMPONENTLISTENER */
}
