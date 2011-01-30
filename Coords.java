import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.io.*;

/** This is the class that holds all the coordinates, vertices, edges, planes
 *  etc. for one "design". If there are two designs loaded, i.e. in tabs, just
 *  make a new CoordStore() with the relevant vertices for the second design;
 *  
 */
public class Coords {

   /** This is inside the CoordStore as it needs access to some methods that
    *  should really be private to the Vertex. This way is a bit odd but much
    *  better in the long run prevention of bugs wise!
    *  
    */
   public static class Vertex {
      private int diameter = 10;

      private final Loc3f p = new Loc3f(0,0,0);
      private final LinkedList<Edge> edgeUses = new LinkedList<Edge>();
      private final Ellipse2D.Float topDownView = new Ellipse2D.Float();

      /** Convenience: Creates a blank, unused vertex at 0,0,0 */
      Vertex(double zoomScale) {
         this(0,0,0,zoomScale);
      }

      /** Convenience: does no rounding, simply casts the doubles back down to float */
      Vertex(double x, double y, double z, double zoomScale) {
         this((float) x, (float) y, (float) z, zoomScale);
      }

      /** Creates and initialises an unused vertex at the given coordinates */
      Vertex(float x, float y, float z, double zoomScale) {
         set(x, y, z, zoomScale);
      }

      /** Sets this vertex's points to the new ones and update anything connected */
      private void set(float x, float y, float z, double zoomScale) {
         p.set(x, y, z);
         recalcTopDownView(zoomScale);

         // update all the edges, simply calling the set() method will update them
         ListIterator<Edge> ite = edgeUses.listIterator();
         while (ite.hasNext()) {
            ite.next().recalcTopDownView(zoomScale);
         }
      }

      /** Adds the given object to the list of objects using this vertex */
      private void setUse(Edge e) {
         if (e == null) return;
         if (edgeUses.contains(e)) return; // already used by that object
         edgeUses.add(e);
      }

      /** Returns true if the list of objects using this vertex is not empty */
      public boolean isUsed() {
         if (edgeUses.size() > 0) return true;
         else return false;
      }

      /** Get the X component */
      public float getX() {
         return p.x();
      }

      /** Get the Y component */
      public float getY() {
         return p.y();
      }

      /** Get the Z component */
      public float getZ() {
         return p.z();
      }

      /** Recalculates the size and location of the topDownView representation */
      private void recalcTopDownView(double zoomScale) {
         topDownView.setFrame(zoomScale*p.x() - diameter / 2, zoomScale*p.y() - diameter / 2, diameter, diameter);
      }

      /** Returns true if the vertex at its current diameter contains the point */
      public boolean contains(Point p, double zoomScale) {
         Point temp = new Point();
         temp.setLocation(p.getX()*zoomScale, p.getY()*zoomScale);
         return topDownView.contains(temp);
      }

      /** Draws the vertex at its current diameter, and the current graphics colour */
      public void paint(Graphics2D g2) {
         g2.fill(topDownView);
      }

      /** Takes all the edges that are "in use" by the given vertex and adds
       *  them to the current vertex (if not already added). It updates the
       *  vertex(s) remember by edges/objects etc. to this (the new) vertex */
      private void addUsesCutFrom(Vertex v, double zoomScale) {
         if (v == null || this == v) return;

         ListIterator<Edge> vIte = v.edgeUses.listIterator();
         while (vIte.hasNext()) {
            Edge e = vIte.next();

            if (e.getV1() == v) {
               e.setV1(this, zoomScale);
            } else if (e.getV2() == v) {
               e.setV2(this, zoomScale);
            } else Main.showFatalExceptionTraceWindow(new Exception("Never Happen Case"));

            this.setUse(e);
         }

         v.edgeUses.clear();
      }

      /** Each Vertex remembers the Coords class that it was created with. This
       *  method returns that Coords instance */
      /* public Coords getOuterInstance() {
         return Coords.this;
      }*/

      /** Returns true iff the x,y,z coords of v match the parameters */
      public boolean equalsLocation(float x, float y, float z) {
         return p.x() == x && p.y() == y && p.z() == z;
      }

      /** Returns true iff the x,y,z coords of the given vertex match this
       *  vertex's coords */
      public boolean equalsLocation(Vertex v) {
         if (v == null) return false;
         return equalsLocation(v.p.x(), v.p.y(), v.p.z());
      }

      /** @see Coords.Vertex.equalsLocation */
      @Override
      public boolean equals(Object obj) {
         if (!(obj instanceof Coords.Vertex)) return false;
         return equalsLocation((Coords.Vertex) obj);
      }

      /** hashCode is for only the location of this vertex, not edges using it etc. */
      @Override
      public int hashCode() {
         return p.hashCode();
      }
   }

   /*!-START OF COORDS--------------------------------------------------------*/
   private String associatedSaveName = null;
   private File associatedSave = null;
   private boolean saveRequired = false;
   
   private final LinkedList<Vertex> vertices = new LinkedList<Vertex>();
   private final LinkedList<Edge> edges = new LinkedList<Edge>();
   private final LinkedList<Furniture> furniture = new LinkedList<Furniture>();
   private int gridWidth = 60; // makes grid lines at 0,60,120,...
   private double zoomScale = 1.0;

   /** Creates a blank coordinate system */
   Coords(String associatedSaveName) {
      this.associatedSaveName = associatedSaveName;
   }

   /** Blindly makes vertices and edges, there might be orphaned/dup vertices */
   Coords(File loadedFrom, float[][] vertices, int[][] edges, Furniture[] furniture) throws IllegalArgumentException {
      if (loadedFrom == null || vertices == null || edges == null || furniture == null)
         throw new IllegalArgumentException("null argument");
      if (!loadedFrom.isFile()) {
         throw new IllegalArgumentException("File is a directory or it doesn't exist");
      }
      this.associatedSave = loadedFrom;
      this.associatedSaveName = loadedFrom.getName();

      Vertex[] vertexA = new Vertex[vertices.length];

      for (int i=0; i < vertices.length; i++) {
         if (vertices[i] == null || vertices[i].length != 3) {
            throw new IllegalArgumentException("Vertices array needs to be of size n by 3");
         }

         vertexA[i] = new Vertex(vertices[i][0],vertices[i][1],vertices[i][2],zoomScale);
         this.vertices.add(vertexA[i]);
      }

      for (int i=0; i < edges.length; i++) {
         if (edges[i] == null || edges[i].length != 4) {
            throw new IllegalArgumentException("Edges array needs to be of size m by 2");
         }

         int v1Index = edges[i][0];
         int v2Index = edges[i][1];
         Point ctrl = new Point(edges[i][2], edges[i][3]);
         
         if (v1Index < 0 || v1Index >= vertexA.length || v2Index < 0 || v2Index >= vertexA.length) {
            throw new IllegalArgumentException("A given edge indexes a vertex that doesn't exist (OOB)");
         }

         Vertex v1 = vertexA[v1Index];
         Vertex v2 = vertexA[v2Index];

         Edge e = new Edge(v1, v2, ctrl, zoomScale);
         v1.setUse(e);
         v2.setUse(e);
         this.edges.add(e);

         //no point throwing events as noone can register as a listener, we're still in constructor!
      }

      for (Furniture f : furniture) {
         // check for collisions
         this.furniture.add(f);
      }
   }

   /** Returns a string with the file name i.e. if the file is C:\hello.txt this
    *  will return hello.txt. Alternatively if the associated save file has not
    *  been set (this coords was not loaded from a file, it was created blank),
    *  then it will return the string that was given to the constructor */
   public String getAssociatedSaveName() {
      return associatedSaveName;
   }

   /** Adds the furniture item if it doesn't already exist */
   public void addFurniture(Furniture f) {
      if (f == null || furniture.contains(f)) return;
      furniture.add(f);
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.FURNITURE_ADDED, f));
   }

   /** Moves the furniture item to the new location */
   public void moveFurniture(Furniture f, Point newCenter) {
      if (f == null || !furniture.contains(f)) return;
      f.set(newCenter, zoomScale);
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.FURNITURE_CHANGED, f));
   }

   /** Removes the furniture item */
   public void delete(Furniture f) {
      if (f == null || !furniture.contains(f)) return;
      furniture.remove(f);
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.FURNITURE_REMOVED, f));
   }

   /** Returns a new edge object, already added to the coordStore */
   public Edge newEdge(Vertex v1, Vertex v2, boolean snapToGrid) {
      v1 = addVertex(v1.getX(), v1.getY(), v1.getZ(), null, snapToGrid);
      v2 = addVertex(v2.getX(), v2.getY(), v2.getZ(), null, snapToGrid);

      Edge e = new Edge(v1, v2, null, zoomScale);
      v1.setUse(e);
      v2.setUse(e);

      edges.add(e);
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_ADDED, e));

      return e;
   }

   /** returns an array containing all the Edges in the current design */
   public Edge[] getEdges() {
      return edges.toArray(new Edge[0]);
   }
   
   /** returns an array containing all the Furniture in the current design */
   public Furniture[] getFurniture(){
      return furniture.toArray(new Furniture[0]);
   }
   

   /** returns the vertex that the Point p lies within, or null if none */
   public Vertex vertexAt(Point p) {
      ListIterator<Vertex> ite = vertices.listIterator();

      while (ite.hasNext()) {
         Vertex v = ite.next();
         if (v.contains(p, zoomScale)) return v;
      }
      
      return null;
   }

   /** Returns the edge that is associated with the curve control circle at p,
    *  or null if p doesn't lie within a ctrl point */
   public Edge ctrlAt(Point p) {
      ListIterator<Edge> ite = edges.listIterator();
		
      while (ite.hasNext()) {
         Edge e = ite.next();
         if (e.curveCtrlContains(p, zoomScale)) return e;
      }
		
      return null;
   }

   /** If there is already a vertex with the given coords then that vertex is
    *  returned otherwise null is returned */
   private Vertex vertexInUse(float x, float y, float z) {
      ListIterator<Vertex> ite = vertices.listIterator();
      
      while (ite.hasNext()) {
         Vertex v = ite.next();
         if (v.equalsLocation(x, y, z)) return v;
      }
      
      return null;
   }
   
   /** Draws things like lines and curves etc. on the given Graphics canvas */
   public void paintEdges(Graphics2D g2, boolean isCurveTool) {
      g2.setColor(Color.BLACK);

      ListIterator<Edge> ite = edges.listIterator();
      while (ite.hasNext()) {
         ite.next().paint(g2, isCurveTool);
      }
   }

   /** Draws the small vertex circles on the given Graphics canvas */
   public void paintVertices(Graphics2D g2) {
      g2.setColor(Color.BLACK);

      ListIterator<Vertex> ite = vertices.listIterator();
      while (ite.hasNext()) {
         ite.next().paint(g2);
      }
   }

   /** Draws all the furniture objects, highlights collisions with walls */
   public void paintFurniture(Graphics2D g2) {
      g2.setColor(Color.BLUE);

      ListIterator<Furniture> ite = furniture.listIterator();
      while (ite.hasNext()) {
         ite.next().paint(g2);
      }
   }

   /** Updates the given vertices coordinates to the given ones and returns the
    *  new vertex that should be used now. It may return the same vertex or a
    *  new one if you try to set the coordinates to a vertex that already exists,
    *  it will merge the two to avoid duplicate entries. This is essentially
    *  snapping, but to a very precise level!! */
   public void set(Vertex v, float x, float y, float z, boolean snapToGrid) {
      if (v == null || !vertices.contains(v)) return;

      if (snapToGrid) {
         x = snapToGrid(x);
         y = snapToGrid(y);
         z = snapToGrid(z);
      }

      Vertex vAlt = vertexInUse(x, y, z);

      if (vAlt != null && vAlt != v) {
         v.addUsesCutFrom(vAlt, zoomScale);
         vertices.remove(vAlt);
      }

      // if the vertex is currently snapped, it might not be changing position, don't fire events
      if (!(v.p.x()==x && v.p.y()==y && v.p.z()==z)) {
         v.set(x, y, z, zoomScale);

         // fire a shit load of events
         Edge[] affectedEdges = v.edgeUses.toArray(new Edge[0]);
         for (Edge e : affectedEdges) {
            fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, e));
         }
      }
   }

   public void setZoomScale(double scale) {
      zoomScale = scale;
         
      recalcAll();
   }

   public void recalcAll() {
      ListIterator<Edge> iteE = edges.listIterator();
      while (iteE.hasNext())
         iteE.next().recalcTopDownView(zoomScale);
      
      ListIterator<Vertex> iteV = vertices.listIterator();
      while (iteV.hasNext())
         iteV.next().recalcTopDownView(zoomScale);
      
      ListIterator<Furniture> iteF = furniture.listIterator();
      while (iteF.hasNext())
         iteF.next().recalcRectangle(zoomScale);
   }

   /** Updates the given vertex so that it no longer remembers Object o as something
    *  that uses it. If the vertex no longer has any uses then it gets deleted! */
   private void removeUse(Vertex v, Edge e) {
      if (v == null) return;

      v.edgeUses.remove(e);

      if (!v.isUsed()) vertices.remove(v);
   }

   /** Returns the snapped to grid version of the given coord part (x,y,z) */
   private float snapToGrid(float coord) {
      float distIntoCell = coord % gridWidth;

      if (Math.abs(distIntoCell) < 0.5 * gridWidth) {
         coord -= distIntoCell;
      } else if (coord < 0) {
         coord -= gridWidth + (coord % gridWidth);
      } else { // x >= 0
         coord += gridWidth - (coord % gridWidth);
      }

      return coord;
   }

   /** If the vertex exists already it prevents duplicate entries.
    *  usefor is the object that will be added to the (perhaps new) vertex's
    *  list of objects that are using it. If Edge useFor is null, then it will
    *  not be added to the vertex's list of uses */
   private Vertex addVertex(float x, float y, float z, Edge useFor, boolean snapToGrid) {
      if (snapToGrid) {
         x = snapToGrid(x);
         y = snapToGrid(y);
         z = snapToGrid(z);
      }

      Vertex inUse = vertexInUse(x, y, z);
      if (inUse != null) {
         // there is already a vertex with these points
         inUse.setUse(useFor); // will do nothing if useFor is null
         return inUse;

      } else {
         // vertex doesn't exist yet
         Vertex newV = new Vertex(x, y, z, zoomScale);
         newV.setUse(useFor); // will do nothing if useFor is null
         vertices.add(newV);
         return newV;
      }
   }

   /** Moves one end of this line to the new coordinates given. If the vertex
    *  end being moved is used by a different object too (i.e. the end of this
    *  line is snapped to the other object) then it will create a new vertex for
    *  the end of this line, to essentially unsnap (split) from that other
    *  object's vertex */
   public void vertexMoveOrSplit(Edge e, boolean isV1, float x, float y, float z, boolean snapToGrid) {
      Vertex toMove = (isV1 ? e.getV1() : e.getV2());

      // tell vertex its no longer used by this edge. if v1 == v2 then we
      // certainly don't want to remove the only vertex thats in use!
      if (e.getV1() != e.getV2()) removeUse(toMove, e);
      
      Vertex newV = addVertex(x, y, z, e, snapToGrid);
      boolean fireChangeEventNecessary = !toMove.equals(newV);

      if (isV1) {
         e.setV1(newV, zoomScale);
      } else {
         e.setV2(newV, zoomScale);
      }

      if (fireChangeEventNecessary) {
         fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, e));
      }
   }

   /** Returns true if the given vertex is in the coordStore. */
   public boolean exists(Coords.Vertex v) {
      return vertices.contains(v);
   }

   /** Looks at each of the objects attached in some way to the given
    *  vertex. This means that everything "snapped" to the vertex will be deleted. */
   public void delete(Coords.Vertex v) {
      if (v == null || !vertices.contains(v)) return;

      // delete edges. This is not a listIterator to stop concurrent exceptions!
      Edge[] edgeUsesArray = v.edgeUses.toArray(new Edge[0]);
      for (int i=0; i < edgeUsesArray.length; i++) {
         Edge e = edgeUsesArray[i];
         delete(e);
      }

      //if this doesn't remove the vertex, then there is a bug elsewhere
   }

   /** Deletes the given edge from its two endpoint vertices and the edges list */
   public void delete(Edge e) {
      if (e == null || !edges.contains(e)) return;

      Coords.Vertex v1 = e.getV1(), v2 = e.getV2();
      if (v1 == null || v2 == null) return;

      removeUse(v1, e);
      removeUse(v2, e);

      edges.remove(e);

      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_REMOVED, e));
   }

   /** calls setCtrl on the edge with the new point and fires an edge changed event */
   public void setEdgeCtrl(Edge dragEdge, Point loc) {
      if (dragEdge == null || !edges.contains(dragEdge) || loc == null) {
         throw new IllegalArgumentException("null parameter");
      }
      
      dragEdge.setCtrl(loc, zoomScale);
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, dragEdge));
   }

   /** Draws the grid on the given Graphics canvas, from 0,0 to width,height */
   public void drawGrid(Graphics2D g2, int width, int height) {
      g2.setColor(Color.LIGHT_GRAY);

      for (int i = gridWidth; i < width; i += gridWidth) {
         g2.drawLine(i, 0, i, height);
      }

      for (int i = gridWidth; i < height; i += gridWidth) {
         g2.drawLine(0, i, width, i);
      }
   }

   //-SAVING-STUFF--------------------------------------------------------------

   /** Returns true if the coords were modified since it was created, or if save
    *  failed or false if nothing has changed or a save was successful */
   public boolean saveRequired() {
      return saveRequired;
   }

   /** Returns the associated save file (which might be null) */
   public File getAssociatedSaveFile() {
      return associatedSave;
   }

   public String getAssociatedSaveFileAsString() {
      if (associatedSave == null) return associatedSaveName;

      try {
         return associatedSave.getCanonicalPath();

      } catch (IOException e) {
         // If an I/O error occurs, which is possible because the construction of the canonical pathname may require filesystem queries
         return "unable to access file location (IO error)";

      } catch (SecurityException e) {
         // If a required system property value cannot be accessed.
         return "unable to access file location (Security Error)";
      }
   }

   /** Saves with the currently associated save file. Throws an IOException in the
    *  case that there is no associated save file yet, in this case you should be
    *  using saveAs(). It also sets requiredSave=false if everything saved OK */
   public void save() throws IOException {
      if (associatedSave == null) {
         throw new IOException("Save file has not been set, you should use saveAs in this case");
      }
      save(associatedSave, true, false);
   }

   /** Saves as the given file. Updates the associated save file if its different
    *  and also sets requiredSave=false if everything saved OK */
   public void saveAs(File saveAs) throws IOException {
      save(saveAs, true, true);
   }

   /** Saves as the given file. Does not change the associated save file, it will
    *  essentially make a copy in the background. Does not set requiredSave=false
    *  so if the user made changes they will still be required to save them to this
    *  Coords instance */
   public void saveCopyAs(File saveAs) throws IOException {
      save(saveAs, false, false);
   }

   /** save the stuff to the given file, if you save to a different file than the
    *  associated one, this coords instance has a new associated file set */
   private void save(File saveAs, boolean updateSaveRequired, boolean updateAssociatedSave) throws IOException {
      // make the list of vertices that will be saved
      double temp = zoomScale;
      setZoomScale(1.0);

      Vertex[] vArray = vertices.toArray(new Vertex[0]);
      float[][] saveVerts = new float[vArray.length][3];
      for (int i=0; i < vArray.length; i++) {
         saveVerts[i][0] = vArray[i].getX();
         saveVerts[i][1] = vArray[i].getY();
         saveVerts[i][2] = vArray[i].getZ();
      }

      // make the list of edges that will be saved
      Edge[] eArray = edges.toArray(new Edge[0]);
      int[][] saveEdges = new int[eArray.length][4];
      for (int i=0; i < eArray.length; i++) {
         // find the index of the two endpoint vertices. If there is an error
         // finding v1 or v2, the save file will be corrupt, but it might be
         // recoverable, so continue anyway!
         saveEdges[i][0] = saveIndexIn(vArray, eArray[i].getV1());
         saveEdges[i][1] = saveIndexIn(vArray, eArray[i].getV2());
         saveEdges[i][2] = eArray[i].getCtrlX();
         saveEdges[i][3] = eArray[i].getCtrlY();
      }

      Furniture[] fArray = new Furniture[furniture.size()];
      ListIterator<Furniture> li = furniture.listIterator();
      while (li.hasNext()) {
         fArray[li.nextIndex()] = li.next();
      }

      FileManager.save(saveAs, saveVerts, saveEdges, fArray);
      // if save() throws an IllegalArgumentException, saveRequired is not reset
      if (updateSaveRequired) saveRequired = false;
      if (updateAssociatedSave) {
         associatedSaveName = saveAs.getName();
         associatedSave = saveAs;
      }

      setZoomScale(temp);
   }

   /** Returns the index in vArray that v is at, or -1 if it is not found */
   private int saveIndexIn(Vertex[] vArray, Vertex v) {
      for (int i=0; i < vArray.length; i++) {
         if (vArray[i] == v) return i;
      }
      return -1;
   }

   //-CHANGE-EVENT-STUFF--------------------------------------------------------
   
   private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
   void addCoordsChangeListener(CoordsChangeListener listener) {
      listenerList.add(CoordsChangeListener.class, listener);
   }
   void removeCoordsChangeListener(CoordsChangeListener listener) {
      listenerList.remove(CoordsChangeListener.class, listener);
   }
   private void fireCoordsChangeEvent(CoordsChangeEvent event) {
      saveRequired = true;
      CoordsChangeListener[] listeners = listenerList.getListeners(CoordsChangeListener.class);
      for (CoordsChangeListener listener : listeners) {
         try {
            listener.coordsChangeOccurred(event);
         } catch (Exception e) {
            /* Catch so we can still fire events if one of the listeners crash */
         }
      }
   }
   public void somethingChanged(Edge e) {
      if (e == null || !edges.contains(e)) return;
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, e));
   }
   public void somethingChanged(Furniture f) {
      if (f == null || !furniture.contains(f)) return;
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.FURNITURE_CHANGED, f));
   }
}
