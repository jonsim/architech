import javax.vecmath.Point3f;
import java.util.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Float;
import java.awt.Point;

/**
 *
 * @author James
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
      if (uses.contains(o)) return; // already used by that object
      uses.add(o);
   }

   /** Removes the given object from the list of objects using this vertex */
   private void removeUse(Object o) {
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
   public ListIterator getUsesIterator() {
      return uses.listIterator();
   }

   /** Returns true iff the x,y,z coords of the given vertex match this
    *  vertex's coords */
   public boolean equals(Vertex v) {
      return equals(v.p.x, v.p.y, v.p.z);
   }

   /** Returns true iff the x,y,z coords of v match the parameters */
   public boolean equals(float x, float y, float z) {
      if (p.x == x && p.y == y && p.z == z) return true;
      else return false;
   }
}
