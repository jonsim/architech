import java.awt.geom.*;
import java.awt.geom.Line2D.*;
import java.awt.*;

/**
 *
 */
public class Furniture {

   private FurnitureSQLData data;
   private final RoundRectangle2D.Float rectangle = new RoundRectangle2D.Float();
   private double rotation; // theta - in radians
   private float rotationCenterX;
   private float rotationCenterY;

   Furniture(FurnitureSQLData data, Point center) {
      this.data = data;

      rotation = 0;
      rotationCenterX = (float) center.x;
      rotationCenterY = (float) center.y;

      // float x, float y, float w, float h, float arcw, float arch
      float x = rotationCenterX - (float) 0.5 * data.width;
      float y = rotationCenterY - (float) 0.5 * data.height;
      rectangle.setRoundRect(x, y, data.width, data.height, 0.15*data.width, 0.15*data.height);
   }

   public void paint(Graphics2D g2) {
      AffineTransform original = g2.getTransform();
      g2.rotate(rotation, rotationCenterX, rotationCenterY);
      g2.fill(rectangle);
      g2.setTransform(original);
   }
}
