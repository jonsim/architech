import java.awt.*;
/**
 *
 * @author James
 */
public class HandlerFurnitureMove {
   private final Coords coords;

   private Furniture furniture;
   private final Point revert = new Point();
   private double rotationRevert;
   private boolean isCollided = false;

   HandlerFurnitureMove(Coords coords) {
      if (coords == null) throw new IllegalArgumentException("null coords");
      this.coords = coords;
   }

   public void start(Point p) {
      if (furniture != null) {
         // already started
         System.err.println("START CALLED WHILST RUNNING");
      }

      furniture = coords.furnitureAt(p.getX(), p.getY());

      if (furniture != null) {
         revert.setLocation(furniture.getRotationCenter());
         rotationRevert = furniture.getRotation();
         isCollided = false;
      }
   }

   public void middle(Point p, boolean isControlDown) {
      if (furniture == null) return;

      if (isControlDown) {
         double deltaX = p.getX() - furniture.getRotationCenterX();
         double deltaY = p.getY() - furniture.getRotationCenterY();
         double rotation = deltaX == 0
            ? ((deltaY >= 0) ? Math.PI / 2 : -Math.PI / 2)
            : (Math.atan2(deltaY, deltaX));

         coords.rotateFurniture(furniture, rotation);

      } else {
         coords.moveFurniture(furniture, p);
      }

      isCollided = coords.detectCollisions(furniture);

      // update the revert point, so it doesn't jump back too far
      if (!isCollided) {
         revert.setLocation(furniture.getRotationCenter());
         rotationRevert = furniture.getRotation();
      }
   }

   public void stop(Point p, boolean isControlDown) {
      if (furniture == null) return;

      if (isControlDown) {
         double deltaX = p.getX() - furniture.getRotationCenterX();
         double deltaY = p.getY() - furniture.getRotationCenterY();
         double rotation = deltaX == 0
            ? ((deltaY >= 0) ? Math.PI / 2 : -Math.PI / 2)
            : (Math.atan2(deltaY, deltaX));

         coords.rotateFurniture(furniture, rotation);

      } else {
         coords.moveFurniture(furniture, p);
      }

      isCollided = coords.detectCollisions(furniture);

      if (isCollided) {
         isCollided = false;
         coords.rotateFurniture(furniture, rotationRevert);
         coords.moveFurniture(furniture, revert);
      }

      furniture = null;
   }

   public Furniture getFurniture() {
      return furniture;
   }

   public boolean isCollided() {
      return isCollided;
   }
}
