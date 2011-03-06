/** Vecmath was annoying to put in the classpath so this code replaces it
 */
public class Loc3f {
   private float x;
   private float y;
   private float z;

   Loc3f(float x, float y, float z) {
      set(x, y, z);
   }

   public final void set(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public float x() {
      return x;
   }

   public float y() {
      return y;
   }

   public float z() {
      return z;
   }

   public boolean equalsLocation(Loc3f loc) {
      return loc.x == x && loc.y == y && loc.z == z;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof Loc3f)) return false;
      return equalsLocation((Loc3f) obj);
   }

   @Override
   public int hashCode() {
      return (int) x ^ (int) y ^ (int) z;
   }
}