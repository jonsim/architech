public class FurnitureSQLData {
   public int furnitureID = -1;
   public int initialScale = -1;

   FurnitureSQLData(int furnitureID, int initialScale) {
      this.furnitureID = furnitureID;
      this.initialScale = initialScale;
   }

   @Override
   public String toString() {
      return "FurnitureID=" + furnitureID + ", InitialScale=" + initialScale;
   }
}
