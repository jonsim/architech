
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

/** Furniture item that is used by Coords. */
public class Furniture {

   public final RoundRectangle2D.Float rectangle = new RoundRectangle2D.Float();
   private int furnitureID;
   private float width, height;
   private float rotationCenterX, rotationCenterY;
   private double rotation; // theta - in radians
   private String objPath;
   private boolean isDW; // is this a door/window
   private boolean isD;  // is this a door
   private boolean isW;  // is this a window
   private boolean isL;  //is this a light?
   private boolean isP;

   Furniture(FurnitureSQLData data, Point center, ObjectBrowser ob) {
      if (data == null || data.objPath == null || center == null) {
         throw new IllegalArgumentException("Null parameter or furniture ID");
      }

      this.furnitureID = data.furnitureID;
      this.width = data.width;
      this.height = data.height;
      this.rotation = 0;
      this.objPath = data.objPath;
      System.out.println("objpath="+objPath);
      isL = ob.isLight(data.type);
      isP = ob.isPhysical(data.type);
      int ftype = ob.isDoorWindow( data.type );
      if ( ftype > -1)
      {
          if( this.height < 10 )
            this.height = 10;

          isDW = true;
          if (ftype == 8)
          {
        	  isD = true;
              isW = false;
              this.width += 10;
          }
          else if (ftype == 9)
          {
              isW = true;
              isD = false;
              this.width += 30;
          }
      }
      else
      {
          isDW = false;
      }

      setRotationCenter(center);
      recalcRectangle();
   }

   Furniture(String toLoadFrom) throws IllegalArgumentException {
      if (toLoadFrom == null)
         throw new IllegalArgumentException("Null parameter");
      String[] split = toLoadFrom.split(",");
      if (split.length < 12)
         throw new IllegalArgumentException("Too few fields (expected 12)");

      try {
         furnitureID = Integer.parseInt(split[0]);
         width = java.lang.Float.parseFloat(split[1]);
         height = java.lang.Float.parseFloat(split[2]);
         rotationCenterX = java.lang.Float.parseFloat(split[3]);
         rotationCenterY = java.lang.Float.parseFloat(split[4]);
         rotation = java.lang.Double.parseDouble(split[5]);

      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Malformed value in saved furniture object");
      }

      // objPath might contain commas, so load split from the end backwards.
      objPath = "";
      for (int i = 6; i < split.length - 5; i++)
        objPath += split[i];
      
      isDW = java.lang.Boolean.parseBoolean(split[split.length - 5]);
      isD  = java.lang.Boolean.parseBoolean(split[split.length - 4]);
      isW  = java.lang.Boolean.parseBoolean(split[split.length - 3]);
      isL  = java.lang.Boolean.parseBoolean(split[split.length - 2]);
      isP  = java.lang.Boolean.parseBoolean(split[split.length - 1]);
      if (isDW && !(isD || isW)) throw new IllegalArgumentException("Malformed furniture type doorwindow");
      if (isD && isW) throw new IllegalArgumentException("Malformed furniture is both a door and a window");
      if (isDW && isL) throw new IllegalArgumentException("Malformed furniture is both a doorwindow and a light");
      if (isDW && isP) throw new IllegalArgumentException("Malformed furniture is both a doorwindow and a physical object");

      recalcRectangle();
   }

   public int getID() {
      return furnitureID;
   }

   public String getObjPath() {
      return objPath;
   }

   public String getSaveString() {
      return furnitureID + "," + width + "," + height + "," + rotationCenterX
         + "," + rotationCenterY + "," + rotation + "," + objPath + "," + isDW
         + "," + isD + "," + isW + "," + isL + "," + isP;
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

   /** Checks if the current object is a door/window */
   public boolean isDoorWindow() {
      return isDW;
   }

   /** Checks if the current object is a door */
   public boolean isDoor() {
      return isD;
   }

   /** Checks if the current object is a window */
   public boolean isWindow() {
      return isW;
   }
   
   /** Checks if the current object is a light */
   public boolean isLight() {
      return isL;
   }
   
   /** Checks if the current object is a physical object (i.e. you can
    *  collide with it in the 3D view. */
   public boolean isPhysical () {
	   return isP;
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
      Color oldColor = g2.getColor();
      AffineTransform original = g2.getTransform();
      g2.rotate(rotation, rotationCenterX, rotationCenterY);
      g2.setColor(Color.WHITE);
      g2.fill(rectangle);
      g2.setColor(oldColor);
      g2.draw(rectangle);
      g2.setTransform(original);
   }
}