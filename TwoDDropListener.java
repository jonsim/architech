import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/** Drop Listener for 2D end of the drag and drop furniture system */
class TwoDDropListener implements DropTargetListener {
   public static final int acceptableActions = DnDConstants.ACTION_COPY;
   private TwoDPanel twoDPanel;
   private Furniture inProgress;

   TwoDDropListener(TwoDPanel twoDPanel) {
      if (twoDPanel == null) throw new IllegalArgumentException("null parameter");
      this.twoDPanel = twoDPanel;
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

   /** Called by drop Checks the flavors */
   private DataFlavor chooseDropFlavor(DropTargetDragEvent e) {
      if (/*e.isLocalTransfer() &&*/ e.isDataFlavorSupported(TransferData.furniture)) {
         return TransferData.furniture;
      } else return null;
      // return DataFlavor.stringFlavor if supported, etc...
   }

   /** Called by dragEnter and dragOver. Checks the flavors and acceptable actions */
   private boolean isDragOk(DropTargetDragEvent e) {
      if (!isDragFlavorSupported(e)) return false;

      //BUG: THIS MIGHT NOT BE FUCKING RIGHT
      if ((e.getDropAction() & acceptableActions) == 0) return false;
      if ((e.getSourceActions() & acceptableActions) == 0) return false;

      return true;
   }

   /** Checks if the final drop action should be allowed */
   private boolean isDropOk(DropTargetDropEvent e) {
      // if (no action match found)
      if ((e.getDropAction() & acceptableActions) == 0) return false;
      if ((e.getSourceActions() & acceptableActions) == 0) return false;

      if (chooseDropFlavor(e) == null) return false;

      return true;
   }

   /** Called while a drag operation is ongoing, when the mouse pointer enters the
    *  operable part of the drop site for the DropTarget registered with this listener. */
   public void dragEnter(DropTargetDragEvent e) {
      DataFlavor chosen;

      if (!isDragOk(e) || (chosen = chooseDropFlavor(e)) == null) {
         e.rejectDrag();
      } else {
         try {
            Object data = e.getTransferable().getTransferData(chosen);
            if (data instanceof FurnitureSQLData) {
               inProgress = new Furniture((FurnitureSQLData) data, e.getLocation(), twoDPanel.getZoomScale());
               twoDPanel.getCoords().addFurniture(inProgress);
               e.acceptDrag(e.getDropAction());
            }
         } catch (UnsupportedFlavorException ufe) {
            /* if the requested data flavor is not supported. */
         } catch (IOException ioe) {
            /* if the data is no longer available in the requested flavor. */
         }

         e.rejectDrag();
      }
   }

   /** Called while a drag operation is ongoing, when the mouse pointer has exited
    *  the operable part of the drop site for the DropTarget registered with this listener. */
   public void dragExit(DropTargetEvent e) {
      // user is no longer dragging over the window, get rid of any redrawn things
      twoDPanel.getCoords().delete(inProgress);
      inProgress = null;
   }

   /** Called when a drag operation is ongoing, while the mouse pointer is still over
    * the operable part of the drop site for the DropTarget registered with this listener. */
   public void dragOver(DropTargetDragEvent e) {
      // assumes that dropActionChanged(DropTargetDragEvent e) is called if something
      // changed about the drag, so it must still be ok since dragEnter()...
      twoDPanel.getCoords().moveFurniture(inProgress, e.getLocation());
      e.acceptDrag(e.getDropAction());
   }

   /** Called when the drag operation has terminated with a drop on the operable
    *  part of the drop site for the DropTarget registered with this listener. */
   public void drop(DropTargetDropEvent e) {
      if (!isDropOk(e)) {
         e.rejectDrop();
         twoDPanel.getCoords().delete(inProgress);
      } else {
         // if you give ACTION_COPY_OR_MOVE, then source will receive MOVE!
         e.acceptDrop(acceptableActions);
      }

      twoDPanel.getCoords().moveFurniture(inProgress, e.getLocation());
      e.dropComplete(true);
   }

   /** Called if the user has modified the current drop gesture. */
   public void dropActionChanged(DropTargetDragEvent e) {
      if (!isDragOk(e)) {
         twoDPanel.getCoords().delete(inProgress);
         e.rejectDrag();
      } else {
         e.acceptDrag(e.getDropAction());
      }
   }
}
