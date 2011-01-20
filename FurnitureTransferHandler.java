import javax.swing.*;
import java.awt.datatransfer.*;

/** Used to enable furniture drag support on the SQL JList (among other things) */
public class FurnitureTransferHandler extends TransferHandler {
   private ObjectBrowser objectBrowser;

   FurnitureTransferHandler(ObjectBrowser objectBrowser) {
      if (objectBrowser == null) throw new IllegalArgumentException("null argument");
      this.objectBrowser = objectBrowser;
   }

   @Override
   public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      return false;
   }

   @Override
   public boolean canImport(TransferHandler.TransferSupport info) {
      return false;
   }

   @Override
   protected Transferable createTransferable(JComponent c) {
      FurnitureSQLData selectedFurniture = objectBrowser.getSelectedFurnitureOrNull();
      if (selectedFurniture == null) return null;
      else return new TransferData(selectedFurniture);
   }

   @Override
   public int getSourceActions(JComponent c) {
      return TransferHandler.COPY;
   }

   @Override
   public boolean importData(TransferHandler.TransferSupport info) {
      return false;
   }

   @Override
   public boolean importData(JComponent comp, Transferable t) {
      return false;
   }
}
