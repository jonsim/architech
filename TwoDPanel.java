import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.DropTarget;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

/** This class holds the panel for the 2D view, it extends JPanel so has repaint()
 *  and all the other methods to do with JPanel. It draws the vertices and edges
 *  from the coordinate system as required and responds to button pressed from
 *  the DesignButtons class. */
class TwoDPanel extends JPanel implements ChangeListener {
   public static final double MIN_ZOOM_SCALE = 0.1;
   public static final double MAX_ZOOM_SCALE = 2.0;
   private double zoomScale = 1.0;
   private DesignButtons designButtons;
   private Coords coords;
   
   private Coords.Vertex hoverVertex, selectVertex;
   private Edge dragEdge;

   public Furniture selectFurniture;
   private double deltaX, deltaY;
   private double rotation;
   public boolean isCollision = false;
   private boolean isCollisionVertex = false;
   private Point revertPoint;
   private double revertRotation = 0;
   private boolean isDragging = false;

   /** If file is null creates a blank coords with the tab name nameIfNullFile,
    *  otherwise it tries to open the given file and load a coords from it, if
    *  it fails an Exception is thrown */
   TwoDPanel(File file, String nameIfNullFile, DesignButtons designButtons) throws Exception {
      if (file == null && nameIfNullFile == null) {
         throw new IllegalArgumentException("If file is null, must give nameIfNullFile");
      }
      if (designButtons == null) throw new IllegalArgumentException("null designbuttons");

      this.designButtons = designButtons;

      setBackground(Color.WHITE);
      setPreferredSize(new Dimension(2000, 1000));
      setFocusable(true);

      DropTarget dropTarget = new DropTarget(this,
         TwoDDropListener.acceptableActions, new TwoDDropListener(this), false);
      dropTarget.setActive(true);

      coords = file == null ? new Coords(nameIfNullFile) : FileManager.load(file);
      coords.addCoordsChangeListener(new TwoDPanelCoordsChangeListener());

      addKeyListener(new TwoDPanelKeyListener());
      addMouseListener(new TwoDPanelMouseListener());
      addMouseMotionListener(new TwoDPanelMouseMotionListener());
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

   /** Returns the current zoom multiplier for y axis 1.0 is normal */
   public double getZoomScale() {
      return zoomScale;
   }

   /** Draws grid, objects and vertices, highlights currently aimed at vertex */
   @Override
   public void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      super.paintComponent(g2); // clear screen
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);

      int vertexDiameter = 10;
      float lineWidth = 1;

      BasicStroke lineStroke = new BasicStroke(lineWidth / (float) zoomScale);
      g2.setStroke(lineStroke);

      g2.scale(zoomScale, zoomScale);
      
      if (designButtons.isGridOn()) {
         coords.drawGrid(g2,
            (int) Math.round(getWidth() / zoomScale), // if zoomScale gets small, make the grid big.
            (int) Math.round(getHeight() / zoomScale));
      }

      coords.paintEdges(g2, designButtons.isCurveTool());
      coords.paintVertices(g2, (int) Math.round(vertexDiameter / zoomScale));
      coords.paintFurniture(g2);

      if (dragEdge != null) {
		 dragEdge.paintLengthText(g2);
		 if(isCollisionVertex) {
		    g2.setColor(Color.red);
			dragEdge.getV1().paint(g2, (int) Math.round(vertexDiameter / zoomScale));
			dragEdge.getV2().paint(g2, (int) Math.round(vertexDiameter / zoomScale));
			dragEdge.paint(g2, false);
		 }
	  }

      if (hoverVertex != null) {
         g2.setColor(Color.red);
         hoverVertex.paint(g2, (int) Math.round(vertexDiameter / zoomScale));
      }

      if (selectVertex != null && designButtons.isSelectTool()) {
		 if(isCollisionVertex) {
			Edge e;
		    ListIterator<Edge> edgeIterator = selectVertex.getEdges().listIterator();
		    g2.setColor(Color.red);
			while(edgeIterator.hasNext()) {
               e = edgeIterator.next();
			   e.paint(g2, false);
			}
		 } else {
			g2.setColor(Color.blue);
		 }
		 selectVertex.paint(g2, (int) Math.round(vertexDiameter / zoomScale));
      }

      if(selectFurniture != null) {
	     if(isCollision) {
		    g2.setColor(Color.red);
		 } else if(designButtons.isSelectTool()){
            g2.setColor(Color.green);
		 }
         selectFurniture.paint(g2);
      }
   }

   /** if the user clicks and drags with the line tool on, call this, it will
    *  make the new edge so that they can see it as they drag! */
   private void lineDragStarted(Coords coordStore, Point p, boolean snapToGrid) {
      if (p == null) return;

      Coords.Vertex v = new Coords.Vertex(p.x, p.y, 0);
      dragEdge = coordStore.newEdge(v, v, snapToGrid);
	  isDragging = false;
   }

   /** No reason other than stopping erroneous  */
   private void lineDragFinished(Point p, boolean snapToGrid) {
	  if(isCollisionVertex) {
	     if(isDragging) {
		    coords.setEdgeCtrl(dragEdge, revertPoint);
		 } else {
	        coords.delete(dragEdge);
			coords.mergeVertices(dragEdge.getV2(), p.x, p.y, 0, snapToGrid);
		 }
	  } else if(dragEdge != null) {
		if(dragEdge.length() == 0) coords.delete(dragEdge);
		else coords.mergeVertices(dragEdge.getV2(), p.x, p.y, 0, snapToGrid);
	  }
	  
	  isDragging = false;
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
	  
	  isCollisionVertex = coords.detectVertexCollisions(dragEdge.getV2());
   }

   private void vertexDragEvent(Coords coordStore, Coords.Vertex selectVertex, Point p, boolean snapToGrid, boolean finished) {
      if (selectVertex == null) return; // coordStore won't do anything if selectVertex == null

	  if(finished) {
		// put it at the right xy-coordinates but different z so it wont be considered to be at that point
	    coordStore.set(selectVertex, p.x, p.y, 1000, snapToGrid);
	    coordStore.mergeVertices(selectVertex, p.x, p.y, 0, snapToGrid);
	  }
      coordStore.set(selectVertex, p.x, p.y, 0, snapToGrid);
	  isCollisionVertex = coords.detectVertexCollisions(selectVertex);
   }

   private class TwoDPanelMouseListener implements MouseListener {
      /** Invoked when a mouse button has been pressed on a component. */
      public void mousePressed(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1) {
            Point p = new Point();
            p.setLocation(e.getPoint().getX() / zoomScale, e.getPoint().getY() / zoomScale);
			selectVertex = null;

            if (designButtons.isLineTool()) {
			   if (!coords.edgeFurnitureCollision(p.getX(), p.getY())) {
				  lineDragStarted(coords, p, designButtons.isGridOn());
                  setCursor(new Cursor(Cursor.HAND_CURSOR));
			   }
               // repaint(); - done by the coordStore change listener if anything changes
            } else if (designButtons.isSelectTool()) {
               selectVertex = coords.vertexAt(p);

			   if(selectVertex != null) {
			      revertPoint = new Point();
				  revertPoint.setLocation(selectVertex.getX(), selectVertex.getY());
			   } else {
				  selectFurniture = coords.furnitureAt(p.getX(), p.getY());
               }			   
			   if(selectFurniture != null) {
			  	  revertPoint = new Point();
				  revertPoint.setLocation(selectFurniture.getRotationCenter());
				  revertRotation = selectFurniture.getRotation();
		       }



               requestFocus(); // makes the keys work if the user clicked on a vertex and presses delete
               repaint(); // gets rid of the blue selected vertex







            } else if (designButtons.isCurveTool()) {
               dragEdge = coords.ctrlAt(p);
			   if (dragEdge != null) {
			      revertPoint = new Point();
				  revertPoint.setLocation(dragEdge.getCtrlX(), dragEdge.getCtrlY());
			   }
            }

            // other tools go here
         }
      }

      /** Invoked when a mouse button has been released on a component. */
      public void mouseReleased(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1) {
			Point p = new Point();
			p.setLocation(e.getPoint().getX() / zoomScale, e.getPoint().getY() / zoomScale);
			lineDragFinished(p, designButtons.isGridOn());

            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			if(selectVertex != null) {
				if (designButtons.isSelectTool() && !isCollisionVertex) {
					// False while still moving, true when mouse released
					vertexDragEvent(coords, selectVertex, p, designButtons.isGridOn(), true);
				}
				Edge edge;
				ListIterator<Edge> edgeIterator = selectVertex.getEdges().listIterator();
				while(edgeIterator.hasNext()) {
					edge = edgeIterator.next();
					if(edge.getV1().equalsLocation(edge.getV2())) {
						coords.delete(edge);
						if(hoverVertex == selectVertex) hoverVertex = null;
						selectVertex = null;
						break;
					}
				}
			}

		    if(selectFurniture != null && isCollision) {
		       coords.moveFurniture(selectFurniture, revertPoint);
			   coords.rotateFurniture(selectFurniture, revertRotation);
			   isCollision = false;
			   selectFurniture = null;
			   repaint(); // go from green to blue
		    }
			if(isCollisionVertex) {
				if(selectVertex != null) {
					coords.set(selectVertex, (float)revertPoint.getX(), (float)revertPoint.getY(), 0, false);
				}
				isCollisionVertex = false;
			}
			Furniture beforeF = selectFurniture;
			if(hoverVertex == null) {
			  selectFurniture = coords.furnitureAt(e.getX(), e.getY());
			} else {
			  selectFurniture = null;
			}
			if (beforeF != selectFurniture) repaint();
			requestFocus();
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
   }

   private class TwoDPanelMouseMotionListener implements MouseMotionListener {
      /** Invoked when a mouse button is pressed on a component and then dragged. */
      public void mouseDragged(MouseEvent e) {
         Point p = new Point();
         p.setLocation(e.getPoint().getX() / zoomScale, e.getPoint().getY() / zoomScale);

         if (designButtons.isLineTool()) {
            lineDragEvent(coords, dragEdge, p, e.isShiftDown(), designButtons.isGridOn());

         } else if (designButtons.isSelectTool()) {
			if(selectVertex != null) {
				// False while still moving, true when mouse released
               vertexDragEvent(coords, selectVertex, p, designButtons.isGridOn(), false);
			} else {
		       if(selectFurniture != null) {
		          if(e.isControlDown()) {
				      deltaX = p.getX() - selectFurniture.getRotationCenterX();
   				      deltaY = p.getY() - selectFurniture.getRotationCenterY();
				      if(deltaX == 0) rotation = (deltaY >= 0) ? Math.PI/2: -Math.PI/2;
				      else rotation = Math.atan2(deltaY, deltaX);
				      coords.rotateFurniture(selectFurniture, rotation);
			          isCollision = coords.detectCollisions(selectFurniture);
			      } else {
				      coords.moveFurniture(selectFurniture, p);
				      isCollision = coords.detectCollisions(selectFurniture);
			      }
               }
		    }

         } else if (designButtons.isCurveTool()) {
            if (dragEdge != null) {
               coords.setEdgeCtrl(dragEdge, p);
			   isDragging = true;
			   isCollisionVertex = coords.detectVertexCollisions(dragEdge.getV1()) ||
			                       coords.detectVertexCollisions(dragEdge.getV2());
			}
         }

         // repaint(); - done by the coordStore change listener if anything changes
      }

      /** Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed. */
      public void mouseMoved(MouseEvent e) {
         Point p = new Point();
         p.setLocation(e.getPoint().getX() / zoomScale, e.getPoint().getY() / zoomScale);

         Coords.Vertex before = hoverVertex;
         hoverVertex = coords.vertexAt(p);
		 
		 
		 Furniture beforeF = selectFurniture;
		 if(hoverVertex == null) {
			selectFurniture = coords.furnitureAt(p.getX(), p.getY());
		 } else {
			selectFurniture = null;
		 }
         if (before != hoverVertex || beforeF != selectFurniture) repaint();
		 requestFocus();
      }
   }

   private class TwoDPanelKeyListener implements KeyListener {
      /** Invoked when a key is pressed and released */
      public void keyTyped(KeyEvent kevt) {
      }

      /** Invoked when a key is pressed */
      public void keyPressed(KeyEvent kevt) {
         int c = kevt.getKeyCode();

         if ((c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) && designButtons.isSelectTool()) {
			coords.delete(selectVertex);

            // the vertex currently being hovered over will only update if the person
            // moves the mouse. If they don't move the mouse and the vertex has been
            // deleted, then it will stay behind in red unless this is done!
            if (!coords.exists(hoverVertex)) hoverVertex = null;

            selectVertex = null;
			
			coords.delete(selectFurniture);
			selectFurniture = null;
            
			repaint(); // removes the blue selected vertex colour
         }
      }

      /** Invoked when a key is released */
      public void keyReleased(KeyEvent kevt) {
      }
   }

   private class TwoDPanelCoordsChangeListener implements CoordsChangeListener {
      /** Called when the something changes in the coordinate system */
      public void coordsChangeOccurred(CoordsChangeEvent e) {
         repaint();
      }
   }

   public void stateChanged(ChangeEvent e) {
      if (e.getSource() instanceof JSlider) {
         JSlider source = (JSlider) e.getSource();
         setZoomScale((double) source.getValue() / 10);
      }
   }

   /** Returns the current zoom multiplier for x axis 1.0 is normal */
   private void setZoomScale(double scale) {
      if (scale <= MIN_ZOOM_SCALE) zoomScale = MIN_ZOOM_SCALE;
      else if (scale >= MAX_ZOOM_SCALE) zoomScale = MAX_ZOOM_SCALE;
      else zoomScale = scale;

      repaint();
   }
}
