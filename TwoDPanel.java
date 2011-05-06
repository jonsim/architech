import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.DropTarget;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.imageio.ImageIO;

/** This class holds the panel for the 2D view, it extends JPanel so has repaint()
 *  and all the other methods to do with JPanel. It draws the vertices and edges
 *  from the coordinate system as required and responds to button pressed from
 *  the DesignButtons class. */
class TwoDPanel extends JPanel implements ChangeListener {

   public static final double MIN_ZOOM_SCALE = 0.1;
   public static final double MAX_ZOOM_SCALE = 2.0;
   private double zoomScale = 1.0;
   private final DesignButtons designButtons;
   private final Coords coords;
   private final ObjectBrowser objectBrowser;
   private Object inProgressHandler = null;
   private final HandlerEdgeCurve handlerEdgeCurve;
   private final HandlerEdgeDraw handlerEdgeDraw;
   private final HandlerVertexMove handlerVertexMove;
   private final HandlerFurnitureMove handlerFurnitureMove;
   private final HandlerDoorWindowMove handlerDoorWindowMove;
   private final HandlerVertexSelect handlerVertexSelect;
   private Furniture hoverFurniture = null;
   private ColourPalette colourPalette;
   private boolean palonshow = false;
   private ArrayList<Polygon> polygons = new ArrayList<Polygon>();
   private ArrayList<Color> polygonFills = new ArrayList<Color>();
   private ArrayList<ArrayList<Edge>> polygonEdges = new ArrayList<ArrayList<Edge>>();
   private ArrayList<ArrayList<Boolean>> polygonReverse = new ArrayList<ArrayList<Boolean>>();
   private String saveLocation = getClass().getResource("img").getPath() + "/";
   private double fillFlatness = 0.001;
   private final Object gettingScreenshotLock = new Object();
   private String currname = null;
   private Rectangle selectionRectangle = new Rectangle();
   private Point rectStart = new Point();

   /** If file is null creates a blank coords with the tab name nameIfNullFile,
	*  otherwise it tries to open the given file and load a coords from it, if
	*  it fails an Exception is thrown */
   TwoDPanel(File file, String nameIfNullFile, DesignButtons designButtons, ObjectBrowser objectBrowser) throws Exception {
  	if (file == null && nameIfNullFile == null) {
     	throw new IllegalArgumentException("If file is null, must give nameIfNullFile");
  	}
  	if (designButtons == null) {
     	throw new IllegalArgumentException("null designbuttons");
  	}
  	if (objectBrowser == null) {
     	throw new IllegalArgumentException("null objectbrowser");
  	}

  	this.designButtons = designButtons;
  	this.objectBrowser = objectBrowser;

  	setBackground(Color.WHITE);
  	setPreferredSize(new Dimension(2000, 1000));
  	setFocusable(true);

  	DropTarget dropTarget = new DropTarget(this,
          	TwoDDropListener.acceptableActions, new TwoDDropListener(this, objectBrowser), false);
  	dropTarget.setActive(true);

  	coords = file == null ? new Coords(nameIfNullFile, objectBrowser) : FileManager.load(file, objectBrowser);
  	coords.addCoordsChangeListener(new TwoDPanelCoordsChangeListener());

  	handlerEdgeCurve = new HandlerEdgeCurve(coords);
  	handlerEdgeDraw = new HandlerEdgeDraw(coords);
  	handlerVertexMove = new HandlerVertexMove(coords);
  	handlerFurnitureMove = new HandlerFurnitureMove(coords);
  	handlerVertexSelect = new HandlerVertexSelect(coords, objectBrowser);
  	handlerDoorWindowMove = new HandlerDoorWindowMove(coords);

  	addKeyListener(new TwoDPanelKeyListener());
  	addMouseListener(new TwoDPanelMouseListener());
  	addMouseMotionListener(new TwoDPanelMouseMotionListener());

  	colourPalette = new ColourPalette();
  	addMouseListener(new MouseAdapter() {

     	@Override
     	public void mouseReleased(MouseEvent Me) {
     	}
  	});
   }

   public ColourPalette getpal() {
  	return colourPalette;
   }

   public void togglepal(int x, int y) {
  	if (palonshow) {
     	colourPalette.hide();
     	repaint();
     	palonshow = false;
  	} else {
     	colourPalette.show(null, x, y);
     	repaint();
     	palonshow = true;
  	}
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

   public HandlerVertexSelect gethvs() {
  	return handlerVertexSelect;
   }

   /** Draws grid, objects and vertices, highlights currently aimed at vertex */
   @Override
   public void paintComponent(Graphics g) {
      synchronized(gettingScreenshotLock) {

  	Graphics2D g2 = (Graphics2D) g;
  	super.paintComponent(g2); // clear screen
  	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          	RenderingHints.VALUE_ANTIALIAS_ON);

  	int vertexDiameter = 10;
  	float lineWidth = 1;

  	BasicStroke lineStroke = new BasicStroke(lineWidth / (float) zoomScale);
  	g2.setStroke(lineStroke);
  	g2.scale(zoomScale, zoomScale);

  	int i = 0;
  	while (i < polygons.size()) {
     	g2.setColor(polygonFills.get(i));
     	g2.fill(polygons.get(i));
     	i++;
  	}

     	if (designButtons.isGridOn()) {
        	coords.drawGrid(g2,
                	(int) Math.round(getWidth()/ zoomScale), // if zoomScale gets small, make the grid big.
                	(int) Math.round(getHeight()/ zoomScale));
     	}


     	// EDGES
     	coords.paintEdges(g2, designButtons.isCurveTool());
     	coords.paintInvalidDW(g2);

     	// VERTICES (first paint any edges that are colliding related to the vertex moving)
     	Coords.Vertex vertexMove = handlerVertexMove.getVertex();
     	if (inProgressHandler == handlerVertexMove && vertexMove != null) {
        	if (handlerVertexMove.isCollided()) {
           	ListIterator<Edge> edgeIterator = vertexMove.getEdges().listIterator();
           	g2.setColor(Color.red);
           	while (edgeIterator.hasNext()) {
              	Edge e = edgeIterator.next();
              	e.paint(g2, false);
           	}
        	}
     	}

     	coords.paintVertices(g2, (int) Math.round(vertexDiameter / zoomScale));

     	if (inProgressHandler == handlerVertexMove && vertexMove != null) {
        	if (handlerVertexMove.isCollided()) {
           	g2.setColor(Color.RED);
        	} else {
           	g2.setColor(Color.BLUE);
        	}
        	vertexMove.paint(g2, (int) Math.round(vertexDiameter / zoomScale));
     	}

     	/*if (hoverVertex != null) {
     	g2.setColor(Color.red);
     	hoverVertex.paint(g2, (int) Math.round(vertexDiameter / zoomScale));
     	}*/

     	ArrayList<Coords.Vertex> selectedVertices = handlerVertexSelect.getSelectedV();
     	if (!selectedVertices.isEmpty()) {
        	g2.setColor(Color.BLUE);
        	for (int j = 0; j < selectedVertices.size(); j++) {
           	Coords.Vertex v = selectedVertices.get(j);
           	v.paint(g2, (int) Math.round(vertexDiameter / zoomScale));
        	}
     	}

     	ArrayList<Edge> wallEdges = handlerVertexSelect.getSelectedE();
     	if (!wallEdges.isEmpty()) {
        	g2.setColor(Color.BLUE);
        	for (int k = 0; k < wallEdges.size(); k++) {
           	Edge edge = wallEdges.get(k);
           	edge.paint(g2, false);
        	}
     	}


     	// EDGE IF THE USER IS DRAWING ONE
     	Edge edgeDraw = handlerEdgeDraw.getEdge();
     	if (inProgressHandler == handlerEdgeDraw && edgeDraw != null) {
        	g2.setColor(Color.BLACK);
        	edgeDraw.paintLengthText(g2);

        	if (handlerEdgeDraw.isCollided()) {
           	g2.setColor(Color.RED);
           	edgeDraw.getV1().paint(g2, (int) Math.round(vertexDiameter / zoomScale));
           	edgeDraw.getV2().paint(g2, (int) Math.round(vertexDiameter / zoomScale));
           	edgeDraw.paint(g2, false);
        	}
     	}

     	Edge edgeCurve = handlerEdgeCurve.getEdge();
     	if (inProgressHandler == handlerEdgeCurve && edgeCurve != null) {
        	g2.setColor(Color.BLACK);
        	edgeCurve.paintLengthText(g2);

        	if (handlerEdgeCurve.isCollided()) {
           	g2.setColor(Color.RED);
           	edgeCurve.getV1().paint(g2, (int) Math.round(vertexDiameter / zoomScale));
           	edgeCurve.getV2().paint(g2, (int) Math.round(vertexDiameter / zoomScale));
           	edgeCurve.paint(g2, false);
        	}
     	}

     	coords.paintLineSplits(g2, (int) Math.round(vertexDiameter / zoomScale));

     	//FURNITURE
     	g2.setColor(Color.BLUE);
     	coords.paintFurniture(g2);

     	Furniture furnitureMove = handlerFurnitureMove.getFurniture();
     	if ((inProgressHandler == null || inProgressHandler == handlerFurnitureMove) && furnitureMove != null) {
        	if (handlerFurnitureMove.isCollided()) {
           	g2.setColor(Color.RED);
        	} else {
           	g2.setColor(Color.GREEN);
        	}
        	furnitureMove.paint(g2);
     	} else if (hoverFurniture != null && designButtons.isSelectTool()) {
        	g2.setColor(Color.CYAN);
        	hoverFurniture.paint(g2);
     	}

     	furnitureMove = handlerDoorWindowMove.getFurniture();
     	if ((inProgressHandler == null || inProgressHandler == handlerDoorWindowMove) && furnitureMove != null) {
        	if (coords.doorWindowInvalidPosition(furnitureMove)) {
           	g2.setColor(Color.RED);
        	} else {
           	g2.setColor(Color.GREEN);
        	}
        	furnitureMove.paint(g2);
     	}

     	Color fill = new Color(Color.BLUE.getRed(), Color.BLUE.getGreen(), Color.BLUE.getBlue(), 50);
     	g2.setColor(fill);
     	g2.fill(selectionRectangle);
     	g2.setColor(Color.BLUE);
     	g2.draw(selectionRectangle);
      }
   }

   private int fillRoom(ArrayList<Edge> edgeList, int index) {
  	if (edgeList == null) {
     	JOptionPane.showMessageDialog(null, "There is no closed room selected, so the floor can't be filled.", "No Closed Path Selected", 1);
     	repaint();
     	return -1;
  	}
  	int i = 0;
  	double x;
  	double y;
  	Polygon thisPoly = new Polygon();
  	PathIterator ite;
  	double[] segment;
  	QuadCurve2D quadCurve;
  	while (i < edgeList.size()) {
     	// This bit reverses walls to ensure the order is correct and the fill updates correctly when moved
     	if (polygonReverse.get(index).get(i) == false) {
        	ite = edgeList.get(i).topDownViewCurve.getPathIterator(null, fillFlatness);
     	} else {
        	quadCurve = new QuadCurve2D.Float(edgeList.get(i).topDownViewCurve.x2, edgeList.get(i).topDownViewCurve.y2,
                	edgeList.get(i).topDownViewCurve.ctrlx, edgeList.get(i).topDownViewCurve.ctrly,
                	edgeList.get(i).topDownViewCurve.x1, edgeList.get(i).topDownViewCurve.y1);
        	ite = quadCurve.getPathIterator(null, fillFlatness);
     	}
     	segment = new double[2];
     	ite.currentSegment(segment);
     	ite.next();
     	x = segment[0];
     	y = segment[1];
     	while (!ite.isDone()) {
        	ite.currentSegment(segment);
        	thisPoly.addPoint((int) x, (int) y);
        	x = segment[0];
        	y = segment[1];
        	ite.next();
     	}
     	i++;
  	}
  	polygons.add(thisPoly);
  	polygonFills.add(colourPalette.fillColour);
  	polygonEdges.add(edgeList);
  	// polygonReverse.add() is in sortEdges as it needs to be done before but only once
  	repaint();
  	return 0;
   }

   private ArrayList<Edge> sortEdges(ArrayList<Edge> edges) {
  	if (edges.isEmpty()) {
     	return null;
  	}
  	ArrayList<Edge> edgeList = new ArrayList<Edge>();
  	edgeList.addAll(edges);
  	int i = 0;
  	boolean changed = false;
  	ArrayList<Edge> sortedList = new ArrayList<Edge>();
  	ArrayList<Boolean> reverse = new ArrayList<Boolean>();
  	Edge e;
  	Coords.Vertex v = edgeList.get(0).getV2();
  	sortedList.add(edgeList.get(0));
  	reverse.add(false);
  	edgeList.remove(0);
  	while (!edgeList.isEmpty()) {
     	while (i < edgeList.size()) {
        	e = edgeList.get(i);
        	if (e.getV1().equals(v) || e.getV2().equals(v)) {
           	sortedList.add(e);
           	// Tell fillRoom which have been reversed
           	if (e.getV2().equals(v)) {
              	reverse.add(true);
           	} else {
              	reverse.add(false);
           	}
           	edgeList.remove(e);
           	changed = true;
           	break;
        	}
        	i++;
     	}
     	i = 0;
     	if (!changed) {
        	return null;
     	}
     	changed = false;
     	// Make sure that you're searching for the correct vertex next time round
     	if (reverse.get(reverse.size() - 1) == false) {
        	v = sortedList.get(sortedList.size() - 1).getV2();
     	} else {
        	v = sortedList.get(sortedList.size() - 1).getV1();
     	}
  	}
  	// This checks that the loop actually closes itself
  	if (reverse.get(reverse.size() - 1) == false) {
     	if (!sortedList.get(0).getV1().equals(sortedList.get(sortedList.size() - 1).getV2())) {
        	return null;
     	}
  	} else {
     	if (!sortedList.get(0).getV1().equals(sortedList.get(sortedList.size() - 1).getV1())) {
        	return null;
     	}
  	}
  	polygonReverse.add(reverse);
  	return sortedList;
   }

   public void getFloorScreenshot() {
  	//if (true) return; // stop the program completely crashing (for now) when a
                    	// vertex is deleted - 3d will give exceptions but will be ok

  	// this.paint(Graphics g) - "Invoked by Swing to draw components. Applica-
  	//	tions should not invoke paint directly, but should instead use the
  	//	repaint method to schedule the component for redrawing."

       synchronized(gettingScreenshotLock) {
	
            Dimension size = this.getSize();
            BufferedImage image = (BufferedImage) this.createImage(size.width, size.height);
            Graphics g = image.getGraphics();
            //gettingScreenshot = true;

            File file = new File(getClass().getResource("img").getPath() + "/cs" + currname);
            file.delete();
            file = new File(getClass().getResource("img").getPath() + "/fs" + currname);
            file.delete();

            DateFormat df = new SimpleDateFormat("ddMM_hh_mm_ss");
            Date now = Calendar.getInstance().getTime();
            String nows = df.format(now);
            this.paint(g);
            g.dispose();
            BufferedImage floor = image;
            ImageFilter ceilfilter = new RGBImageFilter() {

            public final int filterRGB(int x, int y, int rgb) {
                    //invert the colours
                    Color col = new Color(rgb, true);
                    if (col.equals(new Color(255, 255, 255))) {
                    col = new Color(0, 0, 0);
                    } else {
                    col = new Color(255, 255, 255);
                    }
                    int negative = col.getRGB();
                    //use binary shifts to make black -> transparent
                    int alpha = (negative << 8) & 0xFF000000;
                    //colour the other bits cream
                    if (alpha == Color.BLACK.getRGB()) {
                    alpha = (new Color(208, 191, 184)).getRGB();
                    }
                    return alpha;
            }
            };
            ImageProducer ip = new FilteredImageSource(image.getSource(), ceilfilter);
            Image fim = Toolkit.getDefaultToolkit().createImage(ip);
            image = new BufferedImage(fim.getWidth(null), fim.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics bg = image.getGraphics();
            bg.drawImage(fim, 0, 0, null);
            bg.dispose();
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-image.getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image = op.filter(image, null);
I need saveLocation, currname, the two images (if there are more throw error) 
            try {
            ImageIO.write(floor, "png", new File(saveLocation + "fs" + nows + ".png"));
            ImageIO.write(image, "png", new File(saveLocation + "cs" + nows + ".png"));
            } catch (IOException IOE) {
            }
            //gettingScreenshot = false;
            repaint();
            currname = nows + ".png";
            designButtons.viewport3D.getapp().reloadfloor(currname);
       }
   }

   private void selectDragBoxVertices() {
  	Edge[] edges = coords.getEdges();
  	ArrayList<Coords.Vertex> vertices = new ArrayList<Coords.Vertex>();
  	int i = 0;
  	Coords.Vertex v;
  	Point p = new Point();
  	while (i < edges.length) {
     	v = edges[i].getV1();
     	p.setLocation(v.getX(), v.getY());
     	if (selectionRectangle.contains(p)) {
        	vertices.add(v);
     	}
     	v = edges[i].getV2();
     	p.setLocation(v.getX(), v.getY());
     	if (selectionRectangle.contains(p)) {
        	vertices.add(v);
     	}
     	i++;
  	}
  	handlerVertexSelect.addToSelected((Coords.Vertex[]) vertices.toArray(new Coords.Vertex[vertices.size()]));
   }

   private void updateFilledRoom(int i) {
  	// Remove it, and refill it with the updated shape
  	Color temp = polygonFills.get(i);
  	polygonReverse.add(polygonReverse.get(i));
  	fillRoom(polygonEdges.get(i), i);
  	polygonFills.set(polygonFills.size() - 1, temp);
  	polygons.remove(i);
  	polygonFills.remove(i);
  	polygonEdges.remove(i);
  	polygonReverse.remove(i);
   }

   private void deleteFloorFill(ArrayList<Coords.Vertex> vertices) {
  	int a = 0;
  	int i = 0;
  	int j = 0;
  	Coords.Vertex v;
  	while (a < vertices.size()) {
     	v = vertices.get(a);
     	if (v != null) {
        	while (i < polygonEdges.size()) {
           	while (j < polygonEdges.get(i).size()) {
              	if (polygonEdges.get(i).get(j).getV1().equals(v)
                  	|| polygonEdges.get(i).get(j).getV2().equals(v)) {
                 	polygons.remove(i);
                 	polygonEdges.remove(i);
                 	polygonFills.remove(i);
                 	polygonReverse.remove(i);
                 	i--;
                 	break;
              	}
              	j++;
           	}
           	j = 0;
           	i++;
        	}
        	i = 0;
     	}
     	a++;
  	}
  	getFloorScreenshot();
   }

   private class TwoDPanelMouseListener implements MouseListener {

  	/** Invoked when a mouse button has been pressed on a component. */
  	public void mousePressed(MouseEvent e) {
     	if (e.getButton() != MouseEvent.BUTTON1) {
        	return;
     	}

     	Point p = new Point();
     	p.setLocation(e.getPoint().getX() / zoomScale, e.getPoint().getY() / zoomScale);

     	if (designButtons.isLineTool()) {
        	if (inProgressHandler != null) {
           	System.err.println("NEED TO STOP PREVIOUS HANDLER");
        	}
        	//setCursor(new Cursor(Cursor.HAND_CURSOR));
        	inProgressHandler = handlerEdgeDraw;
        	handlerEdgeDraw.start(p, designButtons.isGridOn());
        	handlerFurnitureMove.forgetRememberedFurniture();
        	handlerDoorWindowMove.forgetRememberedDoorWindow();
        	handlerVertexSelect.forgetSelectedVertices();

     	} else if (designButtons.isSelectTool()) {
        	if (coords.vertexAt(p) != null) {
           	// forget about selected furniture, we're remember vertices now
           	handlerFurnitureMove.forgetRememberedFurniture();
           	handlerDoorWindowMove.forgetRememberedDoorWindow();
           	inProgressHandler = handlerVertexMove;
           	handlerVertexMove.start(p);
           	repaint();

        	} else if (coords.furnitureAt(p.getX(), p.getY()) != null) {
           	handlerDoorWindowMove.forgetRememberedDoorWindow();
           	inProgressHandler = handlerFurnitureMove;
           	handlerFurnitureMove.start(p);
           	repaint();
        	} else if (coords.doorWindowAt(p) != null) {
           	handlerFurnitureMove.forgetRememberedFurniture();
           	inProgressHandler = handlerDoorWindowMove;
           	handlerDoorWindowMove.start(p);
           	repaint();
        	} else {
           	rectStart.setLocation(p);
           	selectionRectangle.setLocation(p);
           	handlerFurnitureMove.forgetRememberedFurniture();
           	handlerDoorWindowMove.forgetRememberedDoorWindow();
        	}

        	requestFocus(); // makes the keys work if the user clicked on a vertex and presses delete
        	repaint(); // gets rid of the blue selected vertex

     	} else if (designButtons.isCurveTool()) {
        	if (inProgressHandler != null) {
           	System.err.println("NEED TO STOP PREVIOUS HANDLER");
        	}
        	inProgressHandler = handlerEdgeCurve;
        	handlerEdgeCurve.start(p);
        	handlerFurnitureMove.forgetRememberedFurniture();
        	handlerDoorWindowMove.forgetRememberedDoorWindow();
        	handlerVertexSelect.forgetSelectedVertices();
     	}
     	// other tools go here
  	}

  	/** Invoked when a mouse button has been released on a component. */
  	public void mouseReleased(MouseEvent e) {
     	if (e.getButton() != MouseEvent.BUTTON1) {
        	return;
     	}

     	selectDragBoxVertices();
     	selectionRectangle.setSize(0, 0);
     	selectionRectangle.setLocation(-1, -1);
     	repaint();
     	Point p = new Point();
     	p.setLocation(e.getPoint().getX() / zoomScale, e.getPoint().getY() / zoomScale);

     	boolean callVertexSelect = false;
     	if (inProgressHandler == null) {
        	callVertexSelect = true;
     	}

     	if (inProgressHandler == handlerEdgeCurve) {
   		  boolean fill = false;
        	if (!handlerEdgeCurve.isCollided()) {
           	int i = 0;
           	int j = 0;
           	int k = 0;
           	Edge edge = handlerEdgeCurve.getEdge();
           	if (edge != null) {
              	while (i < polygonEdges.size() - k) {
                 	while (j < polygonEdges.get(i).size()) {
                    	if (polygonEdges.get(i).get(j).getV1().equals(edge.getV1())
                            	|| polygonEdges.get(i).get(j).getV2().equals(edge.getV1())
                            	|| polygonEdges.get(i).get(j).getV1().equals(edge.getV2())
                            	|| polygonEdges.get(i).get(j).getV2().equals(edge.getV2())) {
                       	updateFilledRoom(i);
                       	fill = true;
                       	i--;
                       	k++;
                       	break;
                    	}
                    	j++;
                 	}
                 	j = 0;
                 	i++;
              	}
           	}
        	}
        	if(fill) repaint();
        	inProgressHandler = null;
        	handlerEdgeCurve.stop(p);

        	ArrayList<Coords.Vertex> vList = handlerEdgeCurve.getVertexList();
        	//deleteFloorFill(vList);
       	 
     	} else if (inProgressHandler == handlerEdgeDraw) {
        	inProgressHandler = null;
        	handlerEdgeDraw.stop(p, e.isShiftDown(), designButtons.isGridOn());

        	ArrayList<Coords.Vertex> vList = handlerEdgeDraw.getVertexList();
        	//deleteFloorFill(vList);

     	} else if (inProgressHandler == handlerVertexMove) {
   		  boolean fill = false;
        	if (!handlerVertexMove.isCollided()) {
           	int i = 0;
           	int j = 0;
           	int k = 0;
           	while (i < polygonEdges.size() - k) {
              	while (j < polygonEdges.get(i).size()) {
                 	if (polygonEdges.get(i).get(j).getV1().equals(handlerVertexMove.getVertex())
                         	|| polygonEdges.get(i).get(j).getV2().equals(handlerVertexMove.getVertex())) {
                    	updateFilledRoom(i);
                    	fill = true;
                    	i--;
                    	k++;
                    	break;
                 	}
                 	j++;
              	}
              	j = 0;
              	i++;
           	}
        	}
        	if(fill) repaint();
       	 
        	inProgressHandler = null;
        	handlerVertexMove.stop(p, designButtons.isGridOn());

        	ArrayList<Coords.Vertex> vList = handlerVertexMove.getVertexList();
        	//deleteFloorFill(vList);

        	callVertexSelect = true;

     	} else if (inProgressHandler == handlerFurnitureMove) {
        	inProgressHandler = null;
        	handlerFurnitureMove.stop();
     	} else if (inProgressHandler == handlerDoorWindowMove) {
        	inProgressHandler = null;
        	handlerDoorWindowMove.stop();
     	}

     	// Brent's stuff, mouse release is a lot easier than mouse press as moving
     	// vertices also uses mouse press to recognise and that and this shouldnt
     	// be doing things at the same time
     	if (callVertexSelect) {
        	handlerVertexSelect.click(p);
     	}

     	//setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

     	requestFocus();
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
        	if (inProgressHandler == handlerEdgeDraw) {
           	handlerEdgeDraw.middle(p, e.isShiftDown(), designButtons.isGridOn());
        	} else {
           	// need to stop the previous one
           	// can't really properly do curve middle as it hasn't been started
           	System.err.println("HANDLER CHANGE IN MIDDLE OF LINE TOOL");
        	}

     	} else if (designButtons.isSelectTool()) {
        	if (inProgressHandler == handlerVertexMove) {
           	handlerVertexMove.middle(p, designButtons.isGridOn());

        	} else if (inProgressHandler == handlerFurnitureMove) {
           	handlerFurnitureMove.middle(p, e.isControlDown());

        	} else if (inProgressHandler == handlerDoorWindowMove) {
           	handlerDoorWindowMove.middle(p, e.isControlDown());

        	} else if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
           	int width = Math.abs(rectStart.x - p.x);
           	int length = Math.abs(rectStart.y - p.y);
           	selectionRectangle.setLocation(rectStart);
           	if (rectStart.getX() > p.getX()) {
              	selectionRectangle.setLocation(p.x, selectionRectangle.y);
           	}
           	if (rectStart.getY() > p.getY()) {
              	selectionRectangle.setLocation(selectionRectangle.x, p.y);
           	}
           	selectionRectangle.setSize(width, length);
           	repaint();
        	}

     	} else if (designButtons.isCurveTool()) {
        	if (inProgressHandler == handlerEdgeCurve) {
           	handlerEdgeCurve.middle(p);
        	} else {
           	// need to stop the previous one
           	// can't really properly do curve middle as it hasn't been started
           	System.err.println("HANDLER CHANGE IN MIDDLE OF CURVE TOOL");
        	}
     	}

     	// repaint(); - done by the coordStore change listener if anything changes
  	}

  	/** Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed. */
  	public void mouseMoved(MouseEvent e) {
     	// UNTIL CURVES ARE FIXED I DISABLED THIS
     	requestFocus();

     	Point p = new Point();
     	p.setLocation(e.getPoint().getX() / zoomScale, e.getPoint().getY() / zoomScale);

     	//Coords.Vertex before = hoverVertex;
     	//hoverVertex = coords.vertexAt(p);

     	Furniture beforeF = hoverFurniture;
     	//if (hoverVertex == null) {
     	hoverFurniture = coords.furnitureAt(p.getX(), p.getY());
     	if (hoverFurniture != beforeF) {
        	repaint();
     	}

     	/*} else {
     	selectFurniture = null;
     	}

     	if (before != hoverVertex || beforeF != selectFurniture) {
     	repaint();
     	}

     	requestFocus();*/
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
        	// This call might change a little as it depends on brent's module
        	deleteFloorFill(handlerVertexSelect.getSelectedV());
        	handlerVertexSelect.deleteSelected();
        	handlerDoorWindowMove.delete();

        	if (inProgressHandler == null || inProgressHandler == handlerFurnitureMove) {
           	handlerFurnitureMove.delete();
           	inProgressHandler = null;
        	}
        	for(int i=0;i<20;i++)
       		 designButtons.viewport3D.focus();
     	}

     	if (c == KeyEvent.VK_F) {
        	fillfloor();
     	}

     	if (c == KeyEvent.VK_SPACE) {
        	deselectall();
     	}
  	}

  	/** Invoked when a key is released */
  	public void keyReleased(KeyEvent kevt) {
  	}
   }

   public void deselectall() {
  	handlerVertexSelect.forgetSelectedVertices();
  	repaint();
   }

   public void fillfloor() {
	ArrayList<Edge> sortedEdges = sortEdges(handlerVertexSelect.getSelectedE());
  	if (fillRoom(sortedEdges, polygons.size()) != -1) {
     	ArrayList<Integer> blockList = new ArrayList<Integer>();
     	int i = 0;
     	int j = 0;
     	int k = 0;
     	boolean breaker = false;
     	// This just makes it remove a fill if it is just overwriting one
     	if (sortedEdges != null) {
        	while (i < polygonEdges.size() - 1) {
           	if (polygonEdges.get(i).size() == sortedEdges.size()) {
              	while (blockList.contains(j)) {
                 	j++;
              	}
              	while (j < polygonEdges.get(i).size()) {
                 	while (k < sortedEdges.size()) {
                    	if (polygonEdges.get(i).get(j) == sortedEdges.get(k)) {
                       	blockList.add(j);
                       	break;
                    	}
                    	k++;
                 	}
                 	if (blockList.size() == sortedEdges.size()) {
                    	polygons.remove(i);
                    	polygonFills.remove(i);
                    	polygonEdges.remove(i);
                    	breaker = true;
                    	break;
                 	}
                 	k = 0;
                 	j++;
              	}
           	}
           	if (breaker) {
              	break;
           	}
           	j = 0;
           	i++;
           	blockList.clear();
        	}
     	}
     	getFloorScreenshot();
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
  	if (scale <= MIN_ZOOM_SCALE) {
     	zoomScale = MIN_ZOOM_SCALE;
  	} else if (scale >= MAX_ZOOM_SCALE) {
     	zoomScale = MAX_ZOOM_SCALE;
  	} else {
     	zoomScale = scale;
  	}
  	coords.changegw((int)(50*zoomScale));
  	zoomScale = 1.0;
  	repaint();
   }

   public void setFurnitureAsHandlerAndStart(Furniture f) {
  	if (inProgressHandler != null) {
     	System.err.println("Attempt to set handler when something else was going on");
     	return;
  	}

  	// handlerVertexSelect.forgetRememberedVertices();
  	if (f.isDoorWindow()) {
     	inProgressHandler = handlerDoorWindowMove;
     	handlerDoorWindowMove.start(f);
  	} else {
     	inProgressHandler = handlerFurnitureMove;
     	handlerFurnitureMove.start(f);
  	}

  	repaint();
   }

   public void dropFurnitureMiddleHandlerCall(Point p) {
  	if (inProgressHandler != handlerFurnitureMove && inProgressHandler != handlerDoorWindowMove) {
     	System.err.println("Attempt to set handler when something else was going on");
     	return;
  	}

  	if (inProgressHandler == handlerDoorWindowMove) {
     	handlerDoorWindowMove.middle(p, false);
  	} else {
     	handlerFurnitureMove.middle(p, false);
  	}
   }

   public void dropFurnitureStopHandlerCall() {
  	if (inProgressHandler == handlerDoorWindowMove) {
     	handlerDoorWindowMove.stop();
  	} else {
     	handlerFurnitureMove.stop();
  	}

  	inProgressHandler = null;
   }

   public void dropFurnitureHandlerForgetFurniture() {
  	handlerFurnitureMove.forgetRememberedFurniture();
  	handlerDoorWindowMove.forgetRememberedDoorWindow();
   }
}