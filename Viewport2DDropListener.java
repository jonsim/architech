import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

/**  */
public class Viewport2DDropListener implements DropTargetListener {

   public static final int acceptableActions = DnDConstants.ACTION_COPY;
   private Main main;

   Viewport2DDropListener(Main main) {
      this.main = main;
   }

   /** Called by isDragOk. Checks to see if the drag flavor is acceptable */
   private boolean isDragFlavorSupported(DropTargetDragEvent e) {
      return e.isDataFlavorSupported(TransferData.furniture);
      // This drop target only supports one data flavor
   }

   /** Called by drop Checks the flavors */
   private DataFlavor chooseDropFlavor(DropTargetDropEvent e) {
      if (e.isLocalTransfer() && e.isDataFlavorSupported(TransferData.furniture)) {
         return TransferData.furniture;
      } else return null;

      // return DataFlavor.stringFlavor if supported, etc...
   }

   /** Called by dragEnter and dragOver. Checks the flavors and acceptable actions */
   private boolean isDragOk(DropTargetDragEvent e) {
      if (!isDragFlavorSupported(e)) return false;

      //BUG: THIS MIGHT NOT BE FUCKING RIGHT
      if ((e.getDropAction() & acceptableActions) == 0) return false;

      return true;
   }




   /** Called while a drag operation is ongoing, when the mouse pointer enters the
    *  operable part of the drop site for the DropTarget registered with this listener. */
   public void dragEnter(DropTargetDragEvent e) {
      if (!isDragOk(e)) {
         e.rejectDrag();
         // main.viewport2D.repaint() - clear lingering gfx
      } else {
         e.acceptDrag(e.getDropAction());
         // main.viewport2D.repaint() - display visual feedback/furniture
      }
   }

   /** Called while a drag operation is ongoing, when the mouse pointer has exited
    *  the operable part of the drop site for the DropTarget registered with this listener. */
   public void dragExit(DropTargetEvent e) {
      // user is no longer dragging over the window, get rid of any redrawn things
      main.viewport2D.repaint();
   }

   /** Called when a drag operation is ongoing, while the mouse pointer is still over
    * the operable part of the drop site for the DropTarget registered with this listener. */
   public void dragOver(DropTargetDragEvent e) {
      if (!isDragOk(e)) {
         e.rejectDrag();
         // main.viewport2D.repaint() - clear lingering gfx
      } else {
         e.acceptDrag(e.getDropAction());
         // main.viewport2D.repaint() - display visual feedback/furniture
      }
   }

   /** Called when the drag operation has terminated with a drop on the operable
    *  part of the drop site for the DropTarget registered with this listener. */
   public void drop(DropTargetDropEvent e) {

      if ((e.getSourceActions() & acceptableActions) == 0) {
         e.rejectDrop(); // (no action match found)
         // main.viewport2D.repaint() - clear lingering gfx
         return;
      }

      DataFlavor chosen = chooseDropFlavor(e);
      
      if (chosen == null) {
         e.rejectDrop();
         // main.viewport2D.repaint() - clear lingering gfx
         return;
      }

      // if you give ACTION_COPY_OR_MOVE, then source will receive MOVE!
      e.acceptDrop(acceptableActions);

      Object data;
      try {
         data = e.getTransferable().getTransferData(chosen);

      } catch (Exception err) {
         System.err.println("Exception whilst dropping drag-n-drop object");
         err.printStackTrace(System.err);
         
         e.dropComplete(false);
         // main.viewport2D.repaint() - clear lingering gfx
         return;
      }

      if (!(data instanceof FurnitureSQLData)) {
         e.dropComplete(false);
         // main.viewport2D.repaint() - clear lingering gfx
         return;
      }

      FurnitureSQLData dropped = (FurnitureSQLData) data;
      Point droppedAt = e.getLocation();

      main.coordStore.addFurniture(new Furniture(dropped, droppedAt));
      main.viewport2D.repaint();

      e.dropComplete(true);
      // main.viewport2D.repaint() - clear lingering gfx
   }

   /** Called if the user has modified the current drop gesture. */
   public void dropActionChanged(DropTargetDragEvent e) {
      if (!isDragOk(e)) {
         e.rejectDrag();
         // main.viewport2D.repaint() - clear lingering gfx
      } else {
         e.acceptDrag(e.getDropAction());
         // main.viewport2D.repaint() - display visual feedback/furniture
      }
   }
}
