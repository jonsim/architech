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
//      if (furniture != null) {
//         // already started
//         System.err.println("START CALLED WHILST RUNNING");
//      }

      furniture = coords.furnitureAt(p.getX(), p.getY());

      if (furniture != null) {
         revert.setLocation(furniture.getRotationCenter());
         rotationRevert = furniture.getRotation();
         isCollided = false;
      }
   }

   /** Special method for TwoDDropListener, please don't use! */
   public void start(Furniture f) {
      if (f == null) throw new IllegalArgumentException("null furniture");

      furniture = f;

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
            ? ((deltaY >= 0) ? 0 : -Math.PI)
            : (Math.atan2(deltaY, deltaX)-Math.PI/2);

         coords.rotateFurniture(furniture, rotation);

      } else {
         coords.moveFurniture(furniture, p);
      }

      isCollided = coords.detectCollisions(furniture);
   }

   /** Stop the furniture dragging, resets the class so it is ready to recieve
    *  start() calls again. Resets the furniture to the last valid position if the
    *  user attempts to place it in an invalid place. */
   public void stop() {
      if (furniture == null) return;

      /*if (isControlDown) {
         double deltaX = p.getX() - furniture.getRotationCenterX();
         double deltaY = p.getY() - furniture.getRotationCenterY();
         double rotation = deltaX == 0
            ? ((deltaY >= 0) ? Math.PI / 2 : -Math.PI / 2)
            : (Math.atan2(deltaY, deltaX));

         coords.rotateFurniture(furniture, rotation);

      } else {
         coords.moveFurniture(furniture, p);
      }*/

      isCollided = coords.detectCollisions(furniture);

      if (isCollided) {
         isCollided = false;
         coords.rotateFurniture(furniture, rotationRevert);
         coords.moveFurniture(furniture, revert);
      }
   }

   /** Returns the furniture item which is being moved. might be null if no
    *  furniture is being moved */
   public Furniture getFurniture() {
      return furniture;
   }

   /** Returns true if the furniture is in an invalid position. (And will be
    *  reset to the last valid position if the user doesn't put it in an ok place) */
   public boolean isCollided() {
      return isCollided;
   }

   /** Don't call this when things are happening in start, middle or stop! */
   public void delete() {
      Furniture remember = furniture;
      furniture = null;
      if (remember != null) coords.delete(remember);
   }

   /** Don't call this when things are happening in start, middle or stop! */
   public void forgetRememberedFurniture() {
      furniture = null;
   }
}

