
import java.awt.geom.*;
import java.awt.geom.Line2D.*;
import java.awt.*;

/** Furniture item that is used by Coords. NOTE: objPath doesn't save to file! */
public class Furniture {

   public final RoundRectangle2D.Float rectangle = new RoundRectangle2D.Float();
   private int furnitureID;
   private String objPath;
   private float width, height;
   private float rotationCenterX, rotationCenterY;
   private double rotation; // theta - in radians
   private boolean isDW;// is this a door/window

   Furniture(FurnitureSQLData data, Point center, ObjectBrowser ob) {
      if (data == null || data.objPath == null || center == null) {
         throw new IllegalArgumentException("Null parameter or furniture ID");
      }

      this.furnitureID = data.furnitureID;
      this.width = data.width;
      this.height = data.height;
      this.rotation = 0;
      this.objPath = data.objPath;

      if( ob.isDoorWindow( data.type ) )
         isDW = true;
      else
         isDW = false;

      setRotationCenter(center);
      recalcRectangle();
   }

   Furniture(String toLoadFrom) throws IllegalArgumentException {
      if (toLoadFrom == null) {
         throw new IllegalArgumentException("Null parameter");
      }

      String[] split = toLoadFrom.split(",");
      if (split.length < 6) {
         throw new IllegalArgumentException("Not enough fields");
      }

      // Just in case the file is in an older format
      int lenOffset;
      if(split.length == 6) {
         isDW = false;
         lenOffset = 0;
      } else {
         if( split[split.length - 1].equals( "true" ) )
            isDW = true;
         else
            isDW = false;

         lenOffset = 1;
      }

      // furnitureID might contain "," so just split from the end as we know the expected number
      try {
         rotation = java.lang.Double.parseDouble(split[split.length - 1 - lenOffset]);
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Malformed rotation value");
      }

      try {
         rotationCenterY = java.lang.Float.parseFloat(split[split.length - 2 - lenOffset]);
         rotationCenterX = java.lang.Float.parseFloat(split[split.length - 3 - lenOffset]);
         height = java.lang.Float.parseFloat(split[split.length - 4 - lenOffset]);
         width = java.lang.Float.parseFloat(split[split.length - 5 - lenOffset]);
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Malformed float value");
      }

      try {
         furnitureID = Integer.parseInt(split[split.length - 6 - lenOffset]);
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Malformed furnitureID");
      }

      recalcRectangle();
   }

   public int getID() {
      return furnitureID;
   }

   public String getObjPath() {
      return objPath;
   }

   public String getSaveString() {
      String doorWindow;

      if( isDW )
         doorWindow = "true";
      else
         doorWindow = "false";

      return furnitureID + "," + width + "," + height + "," + rotationCenterX + "," + rotationCenterY + "," + rotation + "," + doorWindow;
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

   public final void recalcRectangle() {
      float x = rotationCenterX - (float) 0.5 * width;
      float y = rotationCenterY - (float) 0.5 * height;
      // float x, float y, float w, float h, float arcw, float arch
      rectangle.setRoundRect(x, y, width, height, 0.2 * width, 0.2 * height);
   }

   public boolean isDoorWindow() {
      return isDW;
   }

   /** Use moveFurniture() in the coordinates class instead! */
   public void set(Point center) {
      if (center == null) {
         return;
      }
      setRotationCenter(center);
      recalcRectangle();
   }

   /** use rotateFurniture() in the coordinates class instead! Rotates clockwise */
   public void setRotation(double radians) {
      rotation = radians;
   }

   public double getRotation() {
      return rotation;
   }

   public float getWidth() {
      return width;
   }

   public float getHeight() {
      return height;
   }

   public float getRotationCenterX() {
      return rotationCenterX;
   }

   public float getRotationCenterY() {
      return rotationCenterY;
   }

   public boolean contains(Point p) {
      Point temp = new Point();
      temp.setLocation(p.getX(), p.getY());
      return rectangle.contains(temp);
   }

   public Point getTopLeft() {
      return rotatePoint(-width / 2, -height / 2);
   }

   public Point getTopRight() {
      return rotatePoint(width / 2, -height / 2);
   }

   public Point getBottomLeft() {
      return rotatePoint(-width / 2, height / 2);
   }

   public Point getBottomRight() {
      return rotatePoint(width / 2, height / 2);
   }

   private Point rotatePoint(float pointX, float pointY) {
      double hypotenuse = Math.sqrt(Math.pow(pointX, 2) + Math.pow(pointY, 2));
      double initialRotation;
      if (pointX == 0) {
         initialRotation = (pointY >= 0) ? Math.PI / 2 : -Math.PI / 2;
      } else {
         initialRotation = Math.atan2(pointY, pointX);
      }
      double finalRotation = initialRotation + rotation;
      double endX = hypotenuse * Math.cos(finalRotation);
      double endY = hypotenuse * Math.sin(finalRotation);
      endX += rotationCenterX;
      endY += rotationCenterY;
      Point p = new Point();
      p.setLocation(endX, endY);
      return p;
   }

   /** Draws the  */
   public void paint(Graphics2D g2) {
      AffineTransform original = g2.getTransform();
      g2.rotate(rotation, rotationCenterX, rotationCenterY);
      g2.draw(rectangle);
      g2.setTransform(original);
   }
}
