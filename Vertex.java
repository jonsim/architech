import javax.vecmath.Point3f;
import java.util.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Float;
import java.awt.Point;

/**
 *
 * @author James
 */
public class Vertex extends Point3f {
   private LinkedList uses = new LinkedList();

   /** Creates a blank, unused vertex at 0,0,0 */
   Vertex() {
      super(0, 0, 0);
   }

   /** Creates and initialises an unused vertex at the given coordinates */
   Vertex(float x, float y, float z) {
      super(x, y, z);
   }

   /** Convenience: does no rounding, simply casts the doubles back down to float */
   Vertex(double x, double y, double z) {
      super((float) x, (float) y, (float) z);
   }

   /** Returns the top down (2D) representation of this vertex, i.e. a circle.
    *  This class can decide how big a circle representation it wants to give */
   public Ellipse2D.Float topDownView() {
      int diameter = 10;
      return new Ellipse2D.Float(x - diameter / 2, y - diameter / 2, diameter, diameter);
   }

   /** Returns the list iterator of this vertex's uses. It can be used to
    *  iterate through all the objects using it, i.e. edges or curves */
   public ListIterator getUsesIterator() {
      return uses.listIterator();
   }

   /** Sets this vertex's points to the new ones */
   public void modify(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   /** Returns true if the list of objects using this vertex is not empty */
   public boolean isUsed() {
      if (uses.size() > 0) return true;
      else return false;
   }

   /** Adds the given object to the list of objects using this vertex */
   public void setUse(Object o) {
      if (uses.contains(o)) return; // already used by that object
      uses.add(o);
   }

   /** Removes the given object from the list of objects using this vertex */
   public void removeUse(Object o) {
      uses.remove(o);
   }

   /** Returns true iff the x,y,z coords of v match the parameters */
   public boolean equals(float x, float y, float z) {
      if (this.x == x && this.y == y && this.z == z) return true;
      else return false;
   }

   /** Returns true iff the x,y,z coords of the given vertex match this
    *  vertex's coords */
   public boolean equals(Vertex v) {
      return equals(v.x, v.y, v.z);
   }
}
