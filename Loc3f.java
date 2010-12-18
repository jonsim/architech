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
}
