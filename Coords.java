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
   public class Vertex {
      public static final int diameter = 10;

      private Loc3f p;
      private LinkedList<Edge> edgeUses = new LinkedList<Edge>();

      /** Each Vertex remembers the Coords class that it was created with. This
       *  method returns that Coords instance */
      public Coords getOuterInstance() {
         return Coords.this;
      }

      /** Convenience: Creates a blank, unused vertex at 0,0,0 */
      Vertex() {
         this(0,0,0);
      }

      /** Convenience: does no rounding, simply casts the doubles back down to float */
      Vertex(double x, double y, double z) {
         this((float) x, (float) y, (float) z);
      }

      /** Creates and initialises an unused vertex at the given coordinates */
      Vertex(float x, float y, float z) {
         p = new Loc3f(x, y, z);
      }

      /** Sets this vertex's points to the new ones */
      private void set(float x, float y, float z) {
         p.set(x, y, z);
      }

      /** Adds the given object to the list of objects using this vertex */
      private void setUse(Edge e) {
         if (e == null) return;
         if (edgeUses.contains(e)) return; // already used by that object
         edgeUses.add(e);
      }

      /** Removes the given object from the list of objects using this vertex */
      private void forgetAbout(Edge e) {
         if (e == null) return;
         edgeUses.remove(e);
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

      /** Returns the top down (2D) representation of this vertex, i.e. a circle.
       *  This class can decide how big a circle representation it wants to give */
      public Ellipse2D.Float topDownView() {
         return new Ellipse2D.Float(p.x() - diameter / 2, p.y() - diameter / 2, diameter, diameter);
      }

      /** Takes all the edges that are "in use" by the given vertex and adds
       *  them to the current vertex (if not already added) */
      private void addUsesCopiedFrom(Vertex v) {
         if (this == v) return;

         ListIterator<Edge> vIte = v.edgeUses.listIterator();
         while (vIte.hasNext()) {
            Edge e = vIte.next();
            this.setUse(e);
         }
      }

      /** Returns true iff the x,y,z coords of the given vertex match this
       *  vertex's coords */
      public boolean equals(Vertex v) {
         if (v == null) return false;
         return equals(v.p.x(), v.p.y(), v.p.z());
      }

      /** Returns true iff the x,y,z coords of v match the parameters */
      public boolean equals(float x, float y, float z) {
         if (p.x() == x && p.y() == y && p.z() == z) return true;
         else return false;
      }
   }

   /*!-START OF COORDS--------------------------------------------------------*/
   
   private LinkedList<Vertex> vertices = new LinkedList<Vertex>();
   private LinkedList<Edge> edges = new LinkedList<Edge>();

   private int gridWidth = 60; // 0,60,120,...

   /** Makes a blank coordinate system */
   Coords() {
   }

   /** Blindly makes vertices and edges, there might be orphaned/dup vertices */
   Coords(float[][] vertices, int[][] edges) throws IllegalArgumentException {
      if (vertices == null || edges == null) throw new IllegalArgumentException("Null argument");

      Vertex[] vertexA = new Vertex[vertices.length];

      for (int i=0; i < vertices.length; i++) {
         if (vertices[i] == null || vertices[i].length != 3) {
            throw new IllegalArgumentException("Vertices array needs to be of size n by 3");
         }

         vertexA[i] = new Vertex(vertices[i][0],vertices[i][1],vertices[i][2]);
         this.vertices.add(vertexA[i]);
      }

      for (int i=0; i < edges.length; i++) {
         if (edges[i] == null || edges[i].length != 2) {
            throw new IllegalArgumentException("Edges array needs to be of size m by 2");
         }

         int v1Index = edges[i][0];
         int v2Index = edges[i][1];
         
         if (v1Index < 0 || v1Index >= vertexA.length || v2Index < 0 || v2Index >= vertexA.length) {
            throw new IllegalArgumentException("A given edge indexes a vertex that doesn't exist (OOB)");
         }

         Vertex v1 = vertexA[v1Index];
         Vertex v2 = vertexA[v2Index];

         Edge e = new Edge(v1, v2);
         v1.setUse(e);
         v2.setUse(e);
         this.edges.add(e);
      }
   }

   /** returns an array containing all the Edges in the current design */
   public Edge[] getEdges() {
      return edges.toArray(new Edge[0]);
   }

   /** returns the vertex that the Point p lies within, or null if none */
   public Vertex vertexAt(Point p) {
      ListIterator<Vertex> ite = vertices.listIterator();

      while (ite.hasNext()) {
         Vertex v = ite.next();
         if (v.topDownView().contains(p)) return v;
      }
      
      return null;
   }

   /** If there is already a vertex with the given coords then that vertex is
    *  returned otherwise null is returned */
   private Vertex vertexInUse(float x, float y, float z) {
      ListIterator<Vertex> ite = vertices.listIterator();
      
      while (ite.hasNext()) {
         Vertex v = ite.next();
         if (v.equals(x, y, z)) return v;
      }
      
      return null;
   }
   
   /** Draws things like lines and curves etc. on the given Graphics canvas */
   public void drawEdges(Graphics2D g2) {
      g2.setColor(Color.BLACK);

      ListIterator<Edge> ite = edges.listIterator();
      while (ite.hasNext()) {
         Edge e = ite.next();
         g2.draw(e.topDownView());
      }
   }

   /** Draws the small vertex circles on the given Graphics canvas */
   public void drawVertices(Graphics2D g2) {
      g2.setColor(Color.BLACK);

      ListIterator<Vertex> ite = vertices.listIterator();
      while (ite.hasNext()) {
         Vertex v = ite.next();
         g2.fill(v.topDownView());
      }
   }

   /** Updates the given vertices coordinates to the given ones and returns the
    *  new vertex that should be used now. It may return the same vertex or a
    *  new one if you try to set the coordinates to a vertex that already exists,
    *  it will merge the two to avoid duplicate entries. This is essentially
    *  snapping, but to a very precise level!! Ignoring the return value of this
    *  result will lead to difficult to fix bugs! */
   public Vertex setxxx(Vertex v, float x, float y, float z) {
      if (v == null) return null;

      Vertex vAlt = vertexInUse(x, y, z);

      // if there is already a vertex with these coordinates, and its not itself!
      if (vAlt != null && vAlt != v) {
         vAlt.addUsesCopiedFrom(v);
         vertices.remove(v);

      } else {
         v.set(x, y, z);
         vAlt = v;
      }

      return vAlt;
   }

   /** Updates the given vertex so that it no longer remembers Object o as something
    *  that uses it. If the vertex no longer has any uses then it gets deleted! */
   public void removeUsexxx(Vertex v, Edge e) {
      if (v == null) return;

      v.forgetAbout(e);
      if (!v.isUsed()) vertices.remove(v);
   }

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
    *  list of objects that are using it */
   public Vertex addVertex(float x, float y, float z, Edge useFor, boolean snapToGrid) {
      if (useFor == null) return null;

      if (!edges.contains(useFor)) edges.add(useFor);

      if (snapToGrid) {
         x = snapToGrid(x);
         y = snapToGrid(y);
         z = snapToGrid(z);
      }

      Vertex inUse = vertexInUse(x, y, z);
      if (inUse != null) {
         // there is already a vertex with these points
         inUse.setUse(useFor);
         return inUse;

      } else {
         // vertex doesn't exist yet
         Vertex newV = new Vertex(x, y, z);
         newV.setUse(useFor);
         vertices.add(newV);
         return newV;
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
      if (e == null) return;

      Coords.Vertex v1 = e.getV1(), v2 = e.getV2();
      if (v1 == null || v2 == null) return;
      
      removeUsexxx(v1, e);
      removeUsexxx(v2, e);

      edges.remove(e);
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

   /** Currently hardwired, should return whether or not the user needs to save */
   public boolean saveRequired() {
      return true;
   }

   /** save the stuff to the given file */
   public void save(File saveAs) throws IOException {
      // make the list of vertices that will be saved
      Vertex[] vArray = vertices.toArray(new Vertex[0]);
      float[][] saveVerts = new float[vArray.length][3];
      for (int i=0; i < vArray.length; i++) {
         saveVerts[i][0] = vArray[i].getX();
         saveVerts[i][1] = vArray[i].getY();
         saveVerts[i][2] = vArray[i].getZ();
      }

      // make the list of edges that will be saved
      Edge[] eArray = edges.toArray(new Edge[0]);
      int[][] saveEdges = new int[eArray.length][2];
      for (int i=0; i < eArray.length; i++) {
         // find the index of the two endpoint vertices. If there is an error
         // finding v1 or v2, the save file will be corrupt, but it might be
         // recoverable, so continue anyway!
         saveEdges[i][0] = saveIndexIn(vArray, eArray[i].getV1());
         saveEdges[i][1] = saveIndexIn(vArray, eArray[i].getV2());
      }

      FileManager.save(saveAs, saveVerts, saveEdges);
   }

   /** Returns the index in vArray that v is at, or -1 if it is not found */
   private int saveIndexIn(Vertex[] vArray, Vertex v) {
      for (int i=0; i < vArray.length; i++) {
         if (vArray[i] == v) return i;
      }
      return -1;
   }
}
