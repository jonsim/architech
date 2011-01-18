
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.Line2D.*;
import java.io.*;

/** Contains TwoDScrollPane, TwoDViewport, TwoDPanel, TwoDDropListener */
public class TwoDScrollPane extends JScrollPane {
   private TwoDPanel twoDPanel;
   /** Sets up all the classes mentioned above in the one ScrollPane */
   TwoDScrollPane(DesignButtons designButtons) {
      super(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      twoDPanel = new TwoDPanel(designButtons);
      setViewportView(twoDPanel);
   }
   /** Subclasses may override this method to return a subclass of JViewport.  */
   @Override
   protected JViewport createViewport() {
      return new TwoDViewport();
   }
   /** Public getter for the 3D view */
   public Coords getCoords() {
      return twoDPanel.getCoords();
   }
   /** Requests the focus be on the associated underlying JPanel */
   public void requestFocusToCurrentTwoDScrollPane() {
      twoDPanel.requestFocus();
      twoDPanel.repaint();
   }



   /** Overrides 2 JViewport methods in order to help zoom work right */
   private static class TwoDViewport extends JViewport {
      /** Converts a size in pixel coordinates to view coordinates. */
      @Override
      public Dimension toViewCoordinates(Dimension size) {
         return super.toViewCoordinates(size);
      }

      /** Converts a point in pixel coordinates to view coordinates. */
      @Override
      public Point toViewCoordinates(Point p) {
         return super.toViewCoordinates(p);
      }
   }



   /** This class holds the panel for the 2D view, it extends JPanel so has repaint()
    *  and all the other methods to do with JPanel. It draws the vertices and edges
    *  from the coordinate system as required and responds to button pressed from
    *  the DesignButtons class.
    *
    *  Future bug to watch out for: If we add a curve on drag tool to a different
    *  mouse button than the line drag tool then you can draw both at once. */
   private static class TwoDPanel extends JPanel implements KeyListener, Scrollable,
         MouseListener, MouseMotionListener, CoordsChangeListener {
      private Edge dragEdge, selectEdge;
      private Coords.Vertex hoverVertex, selectVertex;
      private DesignButtons designButtons;
      private Coords coords;

      /** Initialises the 2D pane and makes it scrollable too */
      TwoDPanel(DesignButtons designButtons) {
         this.designButtons = designButtons;

         setBackground(Color.WHITE);
         setPreferredSize(new Dimension(2000, 1000));
         setFocusable(true);
         
         TwoDDropListener twoDDropListener = new TwoDDropListener(this);
         DropTarget dropTarget = new DropTarget(this, TwoDDropListener.acceptableActions, twoDDropListener, false);
         dropTarget.setActive(true);

         try {
            coords = FileManager.load(new File("testSave.atech"));
         } catch (Exception e) {
            coords = new Coords("New Tab");
         }
         coords.addCoordsChangeListener(this);

         addKeyListener(this);
         addMouseListener(this);
         addMouseMotionListener(this);
      }

      /** Removes all the listeners from this component */
      /*private void disableListeners() {
         main.coordStore.removeCoordsChangeListener(this);
         this.removeKeyListener(this);
         this.removeMouseListener(this);
         this.removeMouseMotionListener(this);
         dropTarget.setActive(false);
      }*/

      public void CoordsChangeOccurred(CoordsChangeEvent e) {
         System.out.println("Coords Change Event: " + e);
         repaint();
      }

      /** Draws grid, objects and vertices, highlights currently aimed at vertex */
      @Override
      public void paintComponent(Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
         super.paintComponent(g2); // clear screen
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);

         if (designButtons.isGridOn()) coords.drawGrid(g2, getWidth(), getHeight());

         coords.paintEdges(g2);
         coords.paintVertices(g2);
         coords.paintFurniture(g2);

         if (dragEdge != null) dragEdge.paintLengthText(g2);

         if (selectEdge != null && designButtons.isSelectTool()) {
            g2.setColor(Color.blue);
            selectEdge.paint(g2);
         }

         if (hoverVertex != null) {
            g2.setColor(Color.red);
            hoverVertex.paint(g2);
         }

         if (selectVertex != null && designButtons.isSelectTool()) {
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
         if (selectVertex == null) return; // coordStore won't do anything if selectVertex == null

         coordStore.set(selectVertex, p.x, p.y, 0, snapToGrid);
      }

      /** Invoked when a mouse button has been pressed on a component. */
      public void mousePressed(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1) {

            if (designButtons.isLineTool()) {
               lineDragStarted(coords, e.getPoint(), designButtons.isGridOn());
               setCursor(new Cursor(Cursor.HAND_CURSOR));
               // repaint(); - done by the coordStore change listener if anything changes

            } else if (designButtons.isSelectTool()) {
               selectVertex = coords.vertexAt(e.getPoint());

               if (selectVertex == null) {
                  selectEdge = coords.edgeAt(e.getPoint());
               } else {
                  selectEdge = null;
               }

               requestFocus(); // makes the keys work if the user clicked on a vertex and presses delete
               repaint(); // gets rid of the blue selected vertex or edge
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
         if (designButtons.isLineTool()) {
            lineDragEvent(coords, dragEdge, e.getPoint(), e.isShiftDown(), designButtons.isGridOn());
         } else if (designButtons.isSelectTool()) {
            vertexDragEvent(coords, selectVertex, e.getPoint(), designButtons.isGridOn());
         }

         // repaint(); - done by the coordStore change listener if anything changes
      }

      /** Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed. */
      public void mouseMoved(MouseEvent e) {
         Coords.Vertex before = hoverVertex;
         hoverVertex = coords.vertexAt(e.getPoint());
         if (before != hoverVertex) repaint();
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

         if ((c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) && designButtons.isSelectTool()) {
            coords.delete(selectVertex);
            coords.delete(selectEdge);

            // the vertex currently being hovered over will only update if the person
            // moves the mouse. If they don't move the mouse and the vertex has been
            // deleted, then it will stay behind in red unless this is done!
            if (!coords.exists(hoverVertex)) hoverVertex = null;

            selectVertex = null;
            selectEdge = null;
            repaint(); // removes the blue selected vertex and edge colour
         }
      }

      /** Invoked when a key is released */
      public void keyReleased(KeyEvent kevt) {
      }

      /** Gets the coords being displayed on this JPanel */
      public Coords getCoords() {
         return coords;
      }

      /** Changes the size of the drawing canvas, updates scroll bars too */
      public void resizeDrawingArea(Dimension newSize) {
         setPreferredSize(newSize);
         revalidate();
      }
   }



   /** Drop Listener for 2D end of the drag and drop furniture system */
   private static class TwoDDropListener implements DropTargetListener {
      public  static final int acceptableActions = DnDConstants.ACTION_COPY;
      private TwoDPanel twoDPanel;
      private Furniture inProgress;

      TwoDDropListener(TwoDPanel twoDPanel) {
         if (twoDPanel == null) throw new IllegalArgumentException("null parameter");
         this.twoDPanel = twoDPanel;
      }

      /** Called by isDragOk. Checks to see if the drag flavor is acceptable */
      private boolean isDragFlavorSupported(DropTargetDragEvent e) {
         return e.isDataFlavorSupported(TransferData.furniture);
         // This drop target only supports one data flavor
      }

      /** Called by drop Checks the flavors */
      private DataFlavor chooseDropFlavor(DropTargetDropEvent e) {
         if (e.isLocalTransfer() && e.isDataFlavorSupported(TransferData.furniture)) {
            return TransferData.furniture;
         } else return null;
         // return DataFlavor.stringFlavor if supported, etc...
      }

      /** Called by drop Checks the flavors */
      private DataFlavor chooseDropFlavor(DropTargetDragEvent e) {
         if (/*e.isLocalTransfer() &&*/ e.isDataFlavorSupported(TransferData.furniture)) {
            return TransferData.furniture;
         } else return null;
         // return DataFlavor.stringFlavor if supported, etc...
      }

      /** Called by dragEnter and dragOver. Checks the flavors and acceptable actions */
      private boolean isDragOk(DropTargetDragEvent e) {
         if (!isDragFlavorSupported(e)) return false;

         //BUG: THIS MIGHT NOT BE FUCKING RIGHT
         if ((e.getDropAction() & acceptableActions) == 0) return false;
         if ((e.getSourceActions() & acceptableActions) == 0) return false;

         return true;
      }

      /** Checks if the final drop action should be allowed */
      private boolean isDropOk(DropTargetDropEvent e) {
         // if (no action match found)
         if ((e.getDropAction() & acceptableActions) == 0) return false;
         if ((e.getSourceActions() & acceptableActions) == 0) return false;

         if (chooseDropFlavor(e) == null) return false;

         return true;
      }

      /** Called while a drag operation is ongoing, when the mouse pointer enters the
       *  operable part of the drop site for the DropTarget registered with this listener. */
      public void dragEnter(DropTargetDragEvent e) {
         DataFlavor chosen;

         if (!isDragOk(e) || (chosen = chooseDropFlavor(e)) == null) {
            e.rejectDrag();
         } else {
            try {
               Object data = e.getTransferable().getTransferData(chosen);
               if (data instanceof FurnitureSQLData) {
                  inProgress = new Furniture((FurnitureSQLData) data, e.getLocation());
                  twoDPanel.getCoords().addFurniture(inProgress);
                  e.acceptDrag(e.getDropAction());
               }
            } catch (UnsupportedFlavorException ufe) {
               /* if the requested data flavor is not supported. */
            } catch (IOException ioe) {
               /* if the data is no longer available in the requested flavor. */
            }

            e.rejectDrag();
         }
      }

      /** Called while a drag operation is ongoing, when the mouse pointer has exited
       *  the operable part of the drop site for the DropTarget registered with this listener. */
      public void dragExit(DropTargetEvent e) {
         // user is no longer dragging over the window, get rid of any redrawn things
         twoDPanel.getCoords().delete(inProgress);
         inProgress = null;
      }

      /** Called when a drag operation is ongoing, while the mouse pointer is still over
       * the operable part of the drop site for the DropTarget registered with this listener. */
      public void dragOver(DropTargetDragEvent e) {
         // assumes that dropActionChanged(DropTargetDragEvent e) is called if something
         // changed about the drag, so it must still be ok since dragEnter()...
         twoDPanel.getCoords().moveFurniture(inProgress, e.getLocation());
         e.acceptDrag(e.getDropAction());
      }

      /** Called when the drag operation has terminated with a drop on the operable
       *  part of the drop site for the DropTarget registered with this listener. */
      public void drop(DropTargetDropEvent e) {
         if (!isDropOk(e)) {
            e.rejectDrop();
            twoDPanel.getCoords().delete(inProgress);
         } else {
            // if you give ACTION_COPY_OR_MOVE, then source will receive MOVE!
            e.acceptDrop(acceptableActions);
         }

         twoDPanel.getCoords().moveFurniture(inProgress, e.getLocation());
         e.dropComplete(true);
      }

      /** Called if the user has modified the current drop gesture. */
      public void dropActionChanged(DropTargetDragEvent e) {
         if (!isDragOk(e)) {
            twoDPanel.getCoords().delete(inProgress);
            e.rejectDrag();
         } else {
            e.acceptDrag(e.getDropAction());
         }
      }
   }



   private static class Unused {
      
   }
}
