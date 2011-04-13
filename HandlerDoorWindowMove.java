import java.awt.*;
/**
 *
 * @author James
 */
public class HandlerDoorWindowMove {
   private final Coords coords;

   private Furniture furniture;
   private final Point revert = new Point();
   private boolean isCollided = false;

   HandlerDoorWindowMove(Coords coords) {
      if (coords == null) throw new IllegalArgumentException("null coords");
      this.coords = coords;
   }

   public void start(Point p) {
//      if (furniture != null) {
//         // already started
//         System.err.println("START CALLED WHILST RUNNING");
//      }

      furniture = coords.doorWindowAt(p);

      if (furniture != null) {
         revert.setLocation(furniture.getRotationCenter());
         isCollided = false;
      }
   }

   /** Special method for TwoDDropListener, please don't use! */
   public void start(Furniture f) {
      if (f == null) throw new IllegalArgumentException("null furniture");

      furniture = f;

      if (furniture != null) {
         revert.setLocation(furniture.getRotationCenter());
         isCollided = false;
      }
   }

   public void middle(Point p, boolean isControlDown) {
      if (furniture == null) return;

      coords.moveDoorWindow(furniture, p);

      isCollided = coords.doorWindowValidPosition(furniture);
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

      isCollided = coords.doorWindowValidPosition(furniture);

      if (isCollided) {
         isCollided = false;
         coords.moveDoorWindow(furniture, revert);
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
      if (remember != null) coords.deleteDoorWindow(remember);
   }

   /** Don't call this when things are happening in start, middle or stop! */
   public void forgetRememberedDoorWindow() {
      furniture = null;
   }
}

