import java.awt.geom.*;
import java.awt.geom.Line2D.*;
import java.awt.*;

/** Furniture item that is used by Coords */
public class Furniture {
   private final RoundRectangle2D.Float rectangle = new RoundRectangle2D.Float();
   private FurnitureSQLData data;
   private double rotation; // theta - in radians
   private float rotationCenterX, rotationCenterY;

   Furniture(FurnitureSQLData data, Point center) {
      this.data = data;
      rotation = 0;
      setRotationCenter(center);
      recalcRectangle();
   }

   private void setRotationCenter(Point center) {
      rotationCenterX = (float) center.x;
      rotationCenterY = (float) center.y;
   }

   private void recalcRectangle() {
      float x = rotationCenterX - (float) 0.5 * data.width;
      float y = rotationCenterY - (float) 0.5 * data.height;
      // float x, float y, float w, float h, float arcw, float arch
      rectangle.setRoundRect(x, y, data.width, data.height, 0.2*data.width, 0.2*data.height);
   }

   /** Use moveFurniture() in the coordinates class instead! */
   public void set(Point center) {
      if (center == null) return;
      setRotationCenter(center);
      recalcRectangle();
   }

   /** use rotateFurniture() in the coordinates class instead! Rotates clockwise */
   public void setRotation(double degrees) {
      rotation = Math.toRadians(degrees);
   }

   /** Draws the  */
   public void paint(Graphics2D g2) {
      AffineTransform original = g2.getTransform();
      g2.rotate(rotation, rotationCenterX, rotationCenterY);
      g2.fill(rectangle);
      g2.setTransform(original);
   }
}
