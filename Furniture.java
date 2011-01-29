import java.awt.geom.*;
import java.awt.geom.Line2D.*;
import java.awt.*;

/** Furniture item that is used by Coords */
public class Furniture {
   private final RoundRectangle2D.Float rectangle = new RoundRectangle2D.Float();
   private String furnitureID;
   private float width, height;
   private float rotationCenterX, rotationCenterY;
   private double rotation; // theta - in radians

   Furniture(FurnitureSQLData data, Point center, double zoomScale) {
      if (data == null || data.furnitureID == null || center == null) {
         throw new IllegalArgumentException("Null parameter or furniture ID");
      }

      this.furnitureID = data.furnitureID;
      this.width = data.width;
      this.height = data.height;
      this.rotation = 0;
      
      setRotationCenter(center);
      recalcRectangle(zoomScale);
   }
   
   Furniture(String toLoadFrom) throws IllegalArgumentException {
      if (toLoadFrom == null) throw new IllegalArgumentException("Null parameter");

      String[] split = toLoadFrom.split(",");
      if (split.length < 6) throw new IllegalArgumentException("Not enough fields");

      // furnitureID might contain "," so just split from the end as we know the expected number
      try {
         rotation = java.lang.Double.parseDouble(split[split.length-1]);
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Malformed rotation value");
      }

      try {
         rotationCenterY = java.lang.Float.parseFloat(split[split.length-2]);
         rotationCenterX = java.lang.Float.parseFloat(split[split.length-3]);
         height = java.lang.Float.parseFloat(split[split.length-4]);
         width = java.lang.Float.parseFloat(split[split.length-5]);
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Malformed float value");
      }

      furnitureID = "";
      for (int i = 0; i <= split.length - 6; i++) {
         furnitureID += split[i];
      }

      recalcRectangle(1.0);
   }
   
   public String getID(){
      return furnitureID;
   }

   public String getSaveString() {
      return furnitureID + "," + width + "," + height + "," + rotationCenterX + "," + rotationCenterY + "," + rotation;
   }

   /** Use the method in the Coords class instead! Sets the Furniture's location */
   private void setRotationCenter(Point center) {
      rotationCenterX = (float) center.x;
      rotationCenterY = (float) center.y;
   }

   /** Returns the location of this furniture object */
   public Point getRotationCenter() {
      return new Point((int) Math.round(rotationCenterX), (int) Math.round(rotationCenterY));
   }

   public void recalcRectangle(double zoomScale) {
      float x = rotationCenterX - (float) 0.5 * width;
      float y = rotationCenterY - (float) 0.5 * height;
      // float x, float y, float w, float h, float arcw, float arch
      rectangle.setRoundRect(zoomScale*x, zoomScale*y, zoomScale*width, zoomScale*height, 0.2*width, 0.2*height);
   }

   /** Use moveFurniture() in the coordinates class instead! */
   public void set(Point center, double zoomScale) {
      if (center == null) return;
      setRotationCenter(center);
      recalcRectangle(zoomScale);
   }

   /** use rotateFurniture() in the coordinates class instead! Rotates clockwise */
   public void setRotation(double degrees) {
      rotation = Math.toRadians(degrees);
   }

   /** Draws the  */
   public void paint(Graphics2D g2) {
      AffineTransform original = g2.getTransform();
      g2.rotate(rotation, rotationCenterX, rotationCenterY);
      g2.draw(rectangle);
      g2.setTransform(original);
   }
}
