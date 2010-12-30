
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import java.util.List;

/** TransferData */
public class TransferData implements Transferable, ClipboardOwner {

   // if we support string the furnitureData will be droppable outside the window
   public static final DataFlavor furniture = new DataFlavor(FurnitureSQLData.class, "Furniture Lookup Data");
   private static final DataFlavor[] flavors = { furniture };

   private FurnitureSQLData furnitureSQLData;

   /** Constructor. simply initialises instance variable */
   public TransferData(FurnitureSQLData furnitureSQLData) {
      this.furnitureSQLData = furnitureSQLData;
   }

   @Override
   public String toString() {
      return "Furniture Lookup Data";
   }



   /** Returns an array of DataFlavor objects indicating the flavors the data can be provided in. */
   public DataFlavor[] getTransferDataFlavors() {
      return flavors;
   }

   /** Returns whether or not the specified data flavor is supported for this object. */
   public boolean isDataFlavorSupported(DataFlavor flavor) {
      for (int i=0; i < flavors.length; i++) if (flavors[i].equals(flavor)) return true;
      return false;
   }

   /** Returns an object which represents the data to be transferred. */
   public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
      if (flavor.equals(furniture)) return furnitureSQLData;
      else throw new UnsupportedFlavorException(flavor);
   }



   public void lostOwnership(Clipboard clipboard, Transferable contents) {
      System.out.println("TransferData lost ownership of:  Clipboard="
         + clipboard.getName() + "  Contents=" + contents);
   }
}
