public class FurnitureSQLData {
   public String furnitureID;
   public float width = 20;
   public float height = 10;

   /** Furniture is represented as a rectangle with width and height */
   FurnitureSQLData(String furnitureID, float width, float height) {
      this.furnitureID = furnitureID;
      this.width = width;
      this.height = height;
   }

   @Override
   public String toString() {
      return "FurnitureID=" + furnitureID + ", Width=" + width + ", Height=" + height;
   }
}
