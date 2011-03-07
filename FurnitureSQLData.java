public class FurnitureSQLData {
   public int furnitureID;
   public String objPath;
   public float width = 20;
   public float height = 10;
   public int type;

   /** Furniture is represented as a rectangle with width and height */
   FurnitureSQLData(int furnitureID, float width, float height, int type, String objPath) {
      this.objPath = objPath;
      this.furnitureID = furnitureID;
      this.width = width;
      this.height = height;
      this.type = type;
   }

   @Override
   public String toString() {
      return "FurnitureID=" + furnitureID + ", Width=" + width + ", Height=" + height + ", Type=" + type + ", ObjPath="+ objPath;
   }
}
