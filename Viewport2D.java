
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D.*;
import java.awt.dnd.*;

/** This class holds the panel for the 2D view, it extends JPanel so has repaint()
 *  and all the other methods to do with JPanel. It draws the vertices and edges
 *  from the coordinate system as required and responds to button pressed from
 *  the DesignButtons class.
 *
 *  Future bug to watch out for: If we add a curve on drag tool to a different
 *  mouse button than the line drag tool then you can draw both at once.
 */
public class Viewport2D extends JPanel implements KeyListener, Scrollable,
      MouseListener, MouseMotionListener {

   private Main main;
   private Graphics2D g2; // so paintComponent() doesn't have to create it each time
   private Edge dragEdge, selectEdge;
   private Coords.Vertex hoverVertex, selectVertex;
   private JScrollPane scrollPane;

   private Viewport2DDropListener viewport2DDropListener;
   private DropTarget dropTarget;

   /** Initialises the 2D pane and makes it scrollable too */
   Viewport2D(Main main) {
      this.main = main;

      setBackground(Color.WHITE);
      setPreferredSize(new Dimension(2000, 1000));
      setFocusable(true);

      scrollPane = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);


      viewport2DDropListener = new Viewport2DDropListener(main);
      dropTarget = new DropTarget(this, Viewport2DDropListener.acceptableActions, viewport2DDropListener, false);
   }

   /** Adds itself as a listener for keys, mouse and mouseMotion */
   public void activateListeners() {
      addKeyListener(this);
      addMouseListener(this);
      addMouseMotionListener(this);
      dropTarget.setActive(true);
   }

   /** Removes all the listeners from this component */
   public void disableListeners() {
      this.removeKeyListener(this);
      this.removeMouseListener(this);
      this.removeMouseMotionListener(this);
      dropTarget.setActive(false);
   }
   
   /** Returns the scrollable version of this class. */
   public JScrollPane getScrollPane() {
      return scrollPane;
   }

   /** Draws grid, objects and vertices, highlights currently aimed at vertex */
   @Override
   public void paintComponent(Graphics g) {
      g2 = (Graphics2D) g;
      super.paintComponent(g2); // clear screen
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
      Coords coordStore = main.coordStore;

      if (main.designButtons.isGridOn()) coordStore.drawGrid(g2, getWidth(), getHeight());

      coordStore.paintEdges(g2);
      coordStore.paintVertices(g2);

      if (dragEdge != null) dragEdge.paintLengthText(g2);
      
      if (selectEdge != null && main.designButtons.isSelectTool()) {
         g2.setColor(Color.blue);
         selectEdge.paint(g2);
      }

      if (hoverVertex != null) {
         g2.setColor(Color.red);
         hoverVertex.paint(g2);
      }

      if (selectVertex != null && main.designButtons.isSelectTool()) {
         g2.setColor(Color.blue);
         selectVertex.paint(g2);
      }
   }

   /** if the user clicks and drags with the line tool on, call this, it will
    *  make the new edge so that they can see it as they drag! */
   private void lineDragStarted(Coords coordStore, Point p, boolean snapToGrid) {
      if (p == null) return;

      Coords.Vertex v = coordStore.new Vertex(p.x, p.y, 0);
      dragEdge = coordStore.newEdge(v, v, snapToGrid);
   }

   /** No reason other than stopping erroneous  */
   private void lineDragFinished() {
      dragEdge = null;
      repaint(); // gets rid of the line length text drawn on screen
   }

   /** edge might be null if the user started a drag with a different mouse
    *  button than BUTTON1 and then while dragging changed to the line tool. This
    *  updates the edge to a new position, that the user has just dragged it to */
   private void lineDragEvent(Coords coordStore, Edge dragEdge, Point draggedTo,
         boolean snapToAxis, boolean snapToGrid) {
      if (dragEdge == null) return;

      float newX = draggedTo.x;
      float newY = draggedTo.y;

      if (snapToAxis) {
         Coords.Vertex origin = dragEdge.getV1();

         float hrizDifference = Math.abs(origin.getX() - newX);
         float vertDifference = Math.abs(origin.getY() - newY);

         if (hrizDifference > vertDifference) {
            newY = origin.getY();
         } else {
            newX = origin.getX();
         }
      }

      coordStore.vertexMoveOrSplit(dragEdge, false, newX, newY, 0, snapToGrid);
   }

   private void vertexDragEvent(Coords coordStore, Coords.Vertex selectVertex, Point p, boolean snapToGrid) {
      // coordStore won't do anything if selectVertex == null
      coordStore.set(selectVertex, p.x, p.y, 0, snapToGrid);
   }

   /** Invoked when a mouse button has been pressed on a component. */
   public void mousePressed(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {

         if (main.designButtons.isLineTool()) {
            lineDragStarted(main.coordStore, e.getPoint(), main.designButtons.isGridOn());
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            repaint();

         } else if (main.designButtons.isSelectTool()) {
            selectVertex = main.coordStore.vertexAt(e.getPoint());

            if (selectVertex == null) {
               selectEdge = main.coordStore.edgeAt(e.getPoint());
            } else {
               selectEdge = null;
            }

            main.viewport2D.requestFocus();
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

   /** Invoked when a mouse button is pressed on a component and then dragged. */
   public void mouseDragged(MouseEvent e) {
      if (main.designButtons.isLineTool()) {
         lineDragEvent(main.coordStore, dragEdge, e.getPoint(), e.isShiftDown(), main.designButtons.isGridOn());
      } else if (main.designButtons.isSelectTool()) {
         vertexDragEvent(main.coordStore, selectVertex, e.getPoint(), main.designButtons.isGridOn());
      }

      repaint();
   }

   /** Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed. */
   public void mouseMoved(MouseEvent e) {
      hoverVertex = main.coordStore.vertexAt(e.getPoint());
      repaint();
   }

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

   /** Invoked when a key is pressed and released */
   public void keyTyped(KeyEvent kevt) {
   }

   /** Invoked when a key is pressed */
   public void keyPressed(KeyEvent kevt) {
      int c = kevt.getKeyCode();

      if ((c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)
            && main.designButtons.isSelectTool() && (selectVertex != null || selectEdge != null)) {
         main.coordStore.delete(selectVertex);
         main.coordStore.delete(selectEdge);

         // the vertex currently being hovered over will only update if the person
         // moves the mouse. If they don't move the mouse and the vertex has been
         // deleted, then it will stay behind in red unless this is done!
         if (!main.coordStore.exists(hoverVertex)) hoverVertex = null;

         selectVertex = null;
         selectEdge = null;
         repaint();
      }
   }

   /** Invoked when a key is released */
   public void keyReleased(KeyEvent kevt) {
   }
}
