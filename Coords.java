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
   private LinkedList<Object> objects = new LinkedList<Object>();

   /** Just for demonstration this makes a few edges in the coordinate system */
   Coords() {

      // RANDOMS :)
      new Edge(this,new Vertex(40.0,71.0,0.0), new Vertex(646.0,71.0,0.0));
      new Edge(this,new Vertex(61.0,77.0,0.0), new Vertex(101.0,81.0,0.0));
      new Edge(this,new Vertex(200.0,200.0,0.0), new Vertex(10.0,10.0,0.0));
      new Edge(this,new Vertex(614.0,107.0,0.0), new Vertex(307.0,313.0,0.0));

      // STAR :)
      new Edge(this, new Vertex(363.0,165.0,0.0), new Vertex(476.0,291.0,0.0));
      new Edge(this, new Vertex(476.0,291.0,0.0), new Vertex(498.0,146.0,0.0));
      new Edge(this, new Vertex(476.0,291.0,0.0), new Vertex(619.0,215.0,0.0));
      new Edge(this, new Vertex(476.0,291.0,0.0), new Vertex(558.0,356.0,0.0));
      new Edge(this, new Vertex(476.0,291.0,0.0), new Vertex(377.0,374.0,0.0));
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
   public void save() {
      FileManager fileManager = new FileManager(new File("testSave.atech"));

      try {
         fileManager.write(vertices.toArray(new Vertex[0]), objects.toArray());
      } catch (IOException e) {
         Main.showFatalExceptionTraceWindow(new Exception("Failed to save"));
      }
   }
}
