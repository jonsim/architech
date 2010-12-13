import java.awt.*;
import javax.vecmath.Point3f;
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

      private Point3f p;
      private LinkedList uses = new LinkedList();

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
         p = new Point3f(x, y, z);
      }

      /** Sets this vertex's points to the new ones */
      private void set(float x, float y, float z) {
         p.set(x, y, z);
      }

      /** Adds the given object to the list of objects using this vertex */
      private void setUse(Object o) {
         if (o == null) return;
         if (uses.contains(o)) return; // already used by that object
         uses.add(o);
      }

      /** Removes the given object from the list of objects using this vertex */
      private void removeUse(Object o) {
         if (o == null) return;
         uses.remove(o);
      }

      /** Returns true if the list of objects using this vertex is not empty */
      public boolean isUsed() {
         if (uses.size() > 0) return true;
         else return false;
      }

      /** Get the X component */
      public float getX() {
         return p.x;
      }

      /** Get the Y component */
      public float getY() {
         return p.y;
      }

      /** Get the Z component */
      public float getZ() {
         return p.z;
      }

      /** Returns the top down (2D) representation of this vertex, i.e. a circle.
       *  This class can decide how big a circle representation it wants to give */
      public Ellipse2D.Float topDownView() {
         return new Ellipse2D.Float(p.x - diameter / 2, p.y - diameter / 2, diameter, diameter);
      }

      /** Returns the list iterator of this vertex's uses. It can be used to
       *  iterate through all the objects using it, i.e. edges or curves */
      private ListIterator getUsesIterator() {
         return uses.listIterator();
      }

      /** Returns true iff the x,y,z coords of the given vertex match this
       *  vertex's coords */
      public boolean equals(Vertex v) {
         if (v == null) return false;
         return equals(v.p.x, v.p.y, v.p.z);
      }

      /** Returns true iff the x,y,z coords of v match the parameters */
      public boolean equals(float x, float y, float z) {
         if (p.x == x && p.y == y && p.z == z) return true;
         else return false;
      }
   }

   /*!-START OF COORDS--------------------------------------------------------*/
   
   private LinkedList<Vertex> vertices = new LinkedList<Vertex>();
   //private LinkedList<Object> objects = new LinkedList<Object>();

   /** Makes a blank coordinate system */
   Coords() {
   }

   /** Might look out of bounds, need to fix this later */
   Coords(float[][] vertices, int[][] edges) {
      if (vertices == null || edges == null) throw new IllegalArgumentException("Null argument");

      Vertex[] vertexA = new Vertex[vertices.length];
      for (int i=0; i < vertices.length; i++) {
         if (vertices[i].length != 3) {
            throw new IllegalArgumentException("Vertices array needs to be of size n by 3");
         }

         vertexA[i] = new Vertex(vertices[i][0],vertices[i][1],vertices[i][2]);
         this.vertices.add(vertexA[i]);
      }

      for (int i=0; i < edges.length; i++) {
         if (edges[i].length != 2) {
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
      }
   }

   /** returns the vertex that the Point p lies within, or null if none */
   public Vertex vertexAt(Point p) {
      ListIterator<Vertex> ite = vertices.listIterator();

      while (ite.hasNext()) {

         Coords.Vertex v = ite.next();
         if (v.topDownView().contains(p)) return v;
      }
      
      return null;
   }

   /** If there is already a vertex with the given coords then that vertex is
    *  returned otherwise null is returned */
   private Vertex vertexInUse(float x, float y, float z) {
      ListIterator<Vertex> ite = vertices.listIterator();
      
      while (ite.hasNext()) {

         Coords.Vertex v = ite.next();
         if (v.equals(x, y, z)) return v;
      }
      
      return null;
   }
   
   /** Draws things like lines and curves etc. on the given Graphics canvas */
   public void drawObjects(Graphics2D g2) {
      g2.setColor(Color.BLACK);

      ListIterator<Vertex> ite = vertices.listIterator();
      while (ite.hasNext()) {
         Vertex v = ite.next();
         ListIterator vi = v.getUsesIterator();

         while (vi.hasNext()) {
            Object use = vi.next();

            if (Edge.isEdge(use)) {
               g2.draw(((Edge) use).topDownView());

            } else {
               Main.showFatalExceptionTraceWindow(new Exception("BUG: Additi"
                  + "onal Shapes have been added, code is designed for Edge "
                  + "class only!"));
            }
         }
      }
   }

   /** Draws the small vertex circles on the given Graphics canvas */
   public void drawVertices(Graphics2D g2) {
      g2.setColor(Color.BLACK);

      ListIterator<Vertex> ite = vertices.listIterator();
      while (ite.hasNext()) {
         g2.fill(ite.next().topDownView());
      }
   }

   /** Updates the given vertex's coordinates to the given ones and returns the
    *  new vertex that should be used now. It may return the same vertex or a
    *  new one if you try to set the coordinates to a vertex that already exists,
    *  it will merge the two to avoid duplicate entries. This is essentially
    *  snapping, but to a very precise level!! */
   public Vertex set(Vertex v, float x, float y, float z) {
      if (v == null) return null;

      Vertex vAlt = vertexInUse(x, y, z);

      if (vAlt != null && vAlt != v) {
         // there is already a vertex with these coordinates, and its not itself!
         ListIterator<Object> oldVertexUses = v.getUsesIterator();
         while (oldVertexUses.hasNext()) {
            vAlt.setUse(oldVertexUses.next());
         }

         vertices.remove(v);

         return vAlt;

      } else {
         v.set(x, y, z);
         return v;
      }
   }

   /** Updates the given vertex so that it no longer remembers Object o as something
    *  that uses it. If the vertex no longer has any uses then it gets deleted! */
   public void removeUse(Vertex v, Object o) {
      if (v == null) return;

      v.removeUse(o);
      if (!v.isUsed()) vertices.remove(v);
   }

   /** If the vertex exists already it prevents duplicate entries.
    *  usefor is the object that will be added to the (perhaps new) vertex's
    *  list of objects that are using it */
   public Vertex addVertex(float x, float y, float z, Object useFor) {
      if (useFor == null) return null;

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

   /** Returns true if the given vertex is in the coordStore. If not, it has been
    *  deleted */
   public boolean exists(Vertex v) {
      return vertices.contains(v);
   }

   /** Calls delete() on each of the objects attached in some way to the given
    *  vertex. This means that everything "snapped" to the vertex will be deleted */
   public void delete(Vertex v) {
      if (!vertices.contains(v)) return;

      Object[] usesArray = v.uses.toArray();

      for (int i=0; i < usesArray.length; i++) {
         if (Edge.isEdge(usesArray[i])) {
            ((Edge) usesArray[i]).delete(this);

         } else {
            Main.showFatalExceptionTraceWindow(new Exception("BUG: Additi"
               + "onal Shapes have been added, code is designed for Edge "
               + "class only!"));
         }
      }

      // if this doesn't completely remove the vertex then there is a bug
      // somewhere else!!!!


      // THE FOLLOWING CODE CAUSES A CONCURRENT MODIFICATION EXCEPTION BECAUSE:
      // WHILST THE FOLLOWING ITERATOR IS RUNNING, EDGE.DELETE() CALLS
      // COORDS.REMOVEUSE WHICH IN TURN CALLS V.REMOVEUSE AND TRAVERSES THE LIST
      // AGAIN AT THE SAME TIME WHICH ISN'T ALLOWED :)
//      ListIterator<Object> vertexUses = v.getUsesIterator();
//      while (vertexUses.hasNext()) {
//         Object o = vertexUses.next();
//
//         if (Edge.isEdge(o)) {
//            ((Edge) o).delete(this);
//
//         } else {
//            Main.showFatalExceptionTraceWindow(new Exception("BUG: Additi"
//               + "onal Shapes have been added, code is designed for Edge "
//               + "class only!"));
//         }
//      }
   }

   /** Currently hardwired, should return whether or not the user needs to save */
   public boolean saveRequired() {
      return true;
   }

   /** Should save the stuff to file */
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
      LinkedList allEdges = new LinkedList();
      for (int i=0; i < vArray.length; i++) {
         Vertex v = vArray[i];
         Object[] uses = v.uses.toArray();
         for (int j=0; j < uses.length; j++) {
            if (!allEdges.contains(uses[j]) && Edge.isEdge(uses[j])) {
               allEdges.add(uses[j]);
            }
         }
      }

      int[][] saveEdges = new int[allEdges.size()][2];
      ListIterator li = allEdges.listIterator();
      while (li.hasNext()) {
         Edge e = (Edge) li.next();
         // find the index in vArray that the two endpoint vertices are
         int v1 = -1;
         int v2 = -1;
         for (int i=0; i < vArray.length; i++) {
            if (vArray[i] == e.getV1()) v1 = i;
            if (vArray[i] == e.getV2()) v2 = i;
         }
         saveEdges[li.previousIndex()][0] = v1;
         saveEdges[li.previousIndex()][1] = v2;
      }

      FileManager.save(saveAs, saveVerts, saveEdges);
   }
}
