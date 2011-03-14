
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.DropTarget;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
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
    //private Coords.Vertex hoverVertex;
    private Object inProgressHandler = null;
    private final HandlerEdgeCurve handlerEdgeCurve;
    private final HandlerEdgeDraw handlerEdgeDraw;
    private final HandlerVertexMove handlerVertexMove;
    private final HandlerFurnitureMove handlerFurnitureMove;
    private final HandlerVertexSelect handlerVertexSelect;
    private Furniture hoverFurniture = null;

    private ColourPalette colourPalette;

    private ArrayList<Polygon> polygons = new ArrayList<Polygon>();
    private ArrayList<Color> polygonFills = new ArrayList<Color>();
    public ArrayList<ArrayList<Edge>> polygonEdges = new ArrayList<ArrayList<Edge>>();
    private String saveLocation = getClass().getResource("img").getPath() + "/";
    private String saveName = "3DFloor.jpg";
    private double fillFlatness = 0.001;
    private boolean gettingScreenshot = false;
    private String currname = null;

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

        coords = file == null ? new Coords(nameIfNullFile) : FileManager.load(file);
        coords.addCoordsChangeListener(new TwoDPanelCoordsChangeListener());

        handlerEdgeCurve = new HandlerEdgeCurve(coords);
        handlerEdgeDraw = new HandlerEdgeDraw(coords);
        handlerVertexMove = new HandlerVertexMove(coords);
        handlerFurnitureMove = new HandlerFurnitureMove(coords);
        handlerVertexSelect = new HandlerVertexSelect(coords, objectBrowser);

        addKeyListener(new TwoDPanelKeyListener());
        addMouseListener(new TwoDPanelMouseListener());
        addMouseMotionListener(new TwoDPanelMouseMotionListener());

        colourPalette = new ColourPalette();
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent Me) {
                if (Me.isPopupTrigger()) {
                    colourPalette.show(null, Me.getXOnScreen()-30, Me.getYOnScreen()-150);
                    repaint();
                } else {
                    colourPalette.hide();
                    repaint();
                }
            }
        });
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

        int i = 0;
	while (i < polygons.size()) {
            g2.setColor(polygonFills.get(i));
            g2.fill(polygons.get(i));
            i++;
        }

        if(!gettingScreenshot) {

        if (designButtons.isGridOn()) {
            coords.drawGrid(g2,
                    (int) Math.round(getWidth() / zoomScale), // if zoomScale gets small, make the grid big.
                    (int) Math.round(getHeight() / zoomScale));
        }


        // EDGES
        coords.paintEdges(g2, designButtons.isCurveTool());


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

        }
    }

    private void fillRoom(ArrayList<Edge> edgeList) {
        if(edgeList == null) {
            JOptionPane.showMessageDialog(null, "There is no closed path");
            repaint();
            return;
        }
        int i = 0;
        double x;
        double y;
        Polygon thisPoly = new Polygon();
        PathIterator ite;
        double[] segment;
        while (i < edgeList.size()) {
            ite = edgeList.get(i).topDownViewCurve.getPathIterator(null, fillFlatness);
            segment = new double[2];
            ite.currentSegment(segment);
            ite.next();
            x = segment[0];
            y = segment[1];
            while(!ite.isDone()) {
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
        repaint();
    }

    private ArrayList<Edge> sortEdges(ArrayList<Edge> edgeList) {
        if(edgeList.isEmpty()) return null;
        int i = 0;
        boolean removed = false;
        boolean changed = false;
        ArrayList<Edge> sortedList = new ArrayList<Edge>();
        Edge e;
        Point ctrl = new Point();
        Coords.Vertex v2 = edgeList.get(0).getV2();
        sortedList.add(edgeList.get(0));
        edgeList.remove(0);
        while(!edgeList.isEmpty()) {
            while(i < edgeList.size()) {
                e = edgeList.get(i);
                if(e.getV1().equals(v2)) {
                    sortedList.add(e);
                    edgeList.remove(e);
                    removed = true;
                    changed = true;
                    break;
                }
                i++;
            }
            if(!removed) {
                i = 0;
                while(i < edgeList.size()) {
                    e = edgeList.get(i);
                    if(e.getV2().equals(v2)) {
                        ctrl.setLocation(e.getCtrlX(), e.getCtrlY());
                        sortedList.add(new Edge(e.getV2(), e.getV1(), ctrl));
                        edgeList.remove(e);
                        changed = true;
                        break;
                    }
                    i++;
                }
            }
            i = 0;
            removed = false;
            if(!changed) return null;
            changed = false;
            v2 = sortedList.get(sortedList.size()-1).getV2();
        }
        if(!sortedList.get(0).getV1().equals(sortedList.get(sortedList.size()-1).getV2()))
            return null;
        return sortedList;
    }

    private void getFloorScreenshot() {
        Dimension size = this.getSize();
        BufferedImage image = (BufferedImage) this.createImage(size.width, size.height);
        Graphics g = image.getGraphics();
        gettingScreenshot = true;
        String dir = getClass().getResource("img").getPath();
        File direc = new File(dir);
		String[] children = direc.list();
		for(int l = 0 ; l<children.length;l++){
		if(children[l].lastIndexOf(".")!=-1){
		if(children[l].substring(children[l].lastIndexOf(".")).equals(".jpg")){
				if(children[l].contains("ss")){
					File file = new File(dir + "/" + children[l]);
					file.delete();
				}
		}}}
		DateFormat df = new SimpleDateFormat("ddMMhhmmss");
        Date now = Calendar.getInstance().getTime();
        String nows = df.format(now);		
        this.paint(g);
        g.dispose();
        try {
            ImageIO.write(image, "jpg", new File(saveLocation + "ss"+nows+".jpg"));
        } catch (IOException IOE) {
        }
        gettingScreenshot = false;
        repaint();
        currname = "ss"+nows+".jpg";        	
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
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                inProgressHandler = handlerEdgeDraw;
                handlerEdgeDraw.start(p, designButtons.isGridOn());
                handlerFurnitureMove.forgetRememberedFurniture();
                handlerVertexSelect.forgetSelectedVertices();

            } else if (designButtons.isSelectTool()) {
                if (coords.vertexAt(p) != null) {
                    // forget about selected furniture, we're remember vertices now
                    handlerFurnitureMove.forgetRememberedFurniture();

                    inProgressHandler = handlerVertexMove;
                    handlerVertexMove.start(p);
                    repaint();

                } else if (coords.furnitureAt(p.getX(), p.getY()) != null) {

                    inProgressHandler = handlerFurnitureMove;
                    handlerFurnitureMove.start(p);
                    repaint();
                } else {
                    handlerFurnitureMove.forgetRememberedFurniture();
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
                handlerVertexSelect.forgetSelectedVertices();
            }
            // other tools go here
        }

        /** Invoked when a mouse button has been released on a component. */
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }

            Point p = new Point();
            p.setLocation(e.getPoint().getX() / zoomScale, e.getPoint().getY() / zoomScale);

            boolean callVertexSelect = false;
            if (inProgressHandler == null) {
                callVertexSelect = true;
            }

            if (inProgressHandler == handlerEdgeCurve) {
                int i = 0;
                int j = 0;
                boolean breaker = false;
                while(i < polygonEdges.size()) {
                    while(j < polygonEdges.get(i).size()) {
                        if(polygonEdges.get(i).get(j).equals(handlerEdgeCurve.getEdge())) {
                            fillRoom(polygonEdges.get(i));
                            polygons.remove(i);
                            polygonFills.remove(i);
                            polygonEdges.remove(i);
                            breaker = true;
                            break;
                        }
                        j++;
                    }
                    if(breaker) break;
                    j = 0;
                    i++;
                }

                inProgressHandler = null;
                handlerEdgeCurve.stop(p);

                getFloorScreenshot();

            } else if (inProgressHandler == handlerEdgeDraw) {
                inProgressHandler = null;
                handlerEdgeDraw.stop(p, e.isShiftDown(), designButtons.isGridOn());

            } else if (inProgressHandler == handlerVertexMove) {
                int i = 0;
                int j = 0;
                boolean breaker = false;
                while(i < polygonEdges.size()) {
                    while(j < polygonEdges.get(i).size()) {
                        if(polygonEdges.get(i).get(j).getV1().equals(handlerVertexMove.getVertex())
                           || polygonEdges.get(i).get(j).getV2().equals(handlerVertexMove.getVertex())) {
                            Color temp = polygonFills.get(i);
                            fillRoom(polygonEdges.get(i));
                            polygonFills.set(polygonFills.size()-1, temp);
                            polygons.remove(i);
                            polygonFills.remove(i);
                            polygonEdges.remove(i);
                            breaker = true;
                            break;
                        }
                        j++;
                    }
                    if(breaker) break;
                    j = 0;
                    i++;
                }
                
                inProgressHandler = null;
                handlerVertexMove.stop(p, designButtons.isGridOn());

                callVertexSelect = true;

                getFloorScreenshot();

            } else if (inProgressHandler == handlerFurnitureMove) {
                inProgressHandler = null;
                handlerFurnitureMove.stop();
            }

            // Bren't stuff, mouse release is a lot easier than mouse press as moving
            // vertices also uses mouse press to recognise and that and this shouldnt
            // be doing things at the same time
            if (callVertexSelect) {
                handlerVertexSelect.click(p);
            }

            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

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

                } else {
                    // this case is user has select tool, clicks in space, drags and releases.
                    // need to stop the previous one, if one is already running
                    // can't really properly do curve middle as it hasn't been started
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
                handlerVertexSelect.deleteSelected();

                if (inProgressHandler == null || inProgressHandler == handlerFurnitureMove) {
                    handlerFurnitureMove.delete();
                    inProgressHandler = null;
                }
            }

            if(c == KeyEvent.VK_F) {
                ArrayList<Edge> sortedEdges = sortEdges(handlerVertexSelect.getSelectedE());
                fillRoom(sortedEdges);
                int i = 0;
                int j = 0;
                int k = 0;
                int count = 0;
                boolean breaker = false;
                ArrayList<Coords.Vertex> vList = handlerVertexSelect.getSelectedV();
                // Brents function returns the last vertex twice for some reason
                // either resolve that in his code (probably better), or ignore
                // it and hope for the best (definitely lazier).
                vList.remove(vList.size()-1);
                // This just makes it remove a fill if it is just overwriting one
                while(i < polygonEdges.size()-1) {
                    if(polygonEdges.get(i).size() == vList.size()) {
                        while(j < polygonEdges.get(i).size()) {
                            while(k < vList.size()) {
                                if(vList.get(k).equals(polygonEdges.get(i).get(j).getV1())) {
                                    count++;
                                    break;
                                }
                                k++;
                            }
                            if(count == vList.size()) {
                                polygons.remove(i);
                                polygonFills.remove(i);
                                polygonEdges.remove(i);
                                breaker = true;
                                break;
                            }
                            k = 0;
                            j++;
                        }
                        if(breaker) break;
                        j = 0;
                        count = 0;
                    }
                    i++;
                }
                getFloorScreenshot();
                System.out.println("NUNN");
                designButtons.viewport3D.getapp().reloadfloor(currname);
            }

            if(c == KeyEvent.VK_SPACE) {
                handlerVertexSelect.forgetSelectedVertices();
                repaint();
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
        if (scale <= MIN_ZOOM_SCALE) {
            zoomScale = MIN_ZOOM_SCALE;
        } else if (scale >= MAX_ZOOM_SCALE) {
            zoomScale = MAX_ZOOM_SCALE;
        } else {
            zoomScale = scale;
        }

        repaint();
    }

    public void setFurnitureAsHandlerAndStart(Furniture f) {
        if (inProgressHandler != null) {
            System.err.println("Attempt to set handler when something else was going on");
            return;
        }

        // handlerVertexSelect.forgetRememberedVertices();

        inProgressHandler = handlerFurnitureMove;
        handlerFurnitureMove.start(f);
        repaint();
    }

    public void dropFurnitureMiddleHandlerCall(Point p) {
        if (inProgressHandler != handlerFurnitureMove) {
            System.err.println("Attempt to set handler when something else was going on");
            return;
        }

        handlerFurnitureMove.middle(p, false);
    }

    public void dropFurnitureStopHandlerCall() {
        inProgressHandler = null;
        handlerFurnitureMove.stop();
    }

    public void dropFurnitureHandlerForgetFurniture() {
        handlerFurnitureMove.forgetRememberedFurniture();
    }
}
