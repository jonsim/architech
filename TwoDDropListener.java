import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.awt.Point;

/** Drop Listener for 2D end of the drag and drop furniture system */
class TwoDDropListener implements DropTargetListener {
   public static final int acceptableActions = DnDConstants.ACTION_COPY;
   private TwoDPanel twoDPanel;
   private Furniture inProgress;
   private ObjectBrowser objectBrowser;

   TwoDDropListener(TwoDPanel twoDPanel, ObjectBrowser objectBrowser) {
      if (twoDPanel == null || objectBrowser == null) throw new IllegalArgumentException("null parameter");
      this.twoDPanel = twoDPanel;
      this.objectBrowser = objectBrowser;
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
      if ( !inProgress.isDoorWindow() && twoDPanel.getCoords().detectCollisions(inProgress) == true ) return false;

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
               Point p = scalePoint(e.getLocation(), twoDPanel.getZoomScale());
               inProgress = new Furniture( (FurnitureSQLData) data, p, objectBrowser);

               if( inProgress.isDoorWindow() )
                  twoDPanel.getCoords().addDoorWindow(inProgress);
               else
                  twoDPanel.getCoords().addFurniture(inProgress);

               e.acceptDrag(e.getDropAction());
               
            } else e.rejectDrag();

         } catch (UnsupportedFlavorException ufe) {
            e.rejectDrag();
         } catch (IOException ioe) {
            e.rejectDrag();
         }
      }
   }

   /** Called while a drag operation is ongoing, when the mouse pointer has exited
    *  the operable part of the drop site for the DropTarget registered with this listener. */
   public void dragExit(DropTargetEvent e) {
      if( inProgress.isDoorWindow() )
         twoDPanel.getCoords().deleteDoorWindow(inProgress);
      else
         twoDPanel.getCoords().delete(inProgress);
      // remember inProgress as the same drag might continue again
   }

   /** Called when a drag operation is ongoing, while the mouse pointer is still over
    * the operable part of the drop site for the DropTarget registered with this listener. */
   public void dragOver(DropTargetDragEvent e) {
      // dragEnter is not called for every entry, so re-add the furniture if it
      // was deleted from coords by dragExit. This will do nothing if already added
      if( inProgress.isDoorWindow() )
         twoDPanel.getCoords().addDoorWindow(inProgress);
      else
         twoDPanel.getCoords().addFurniture(inProgress);

//   twoDPanel.selectFurniture = inProgress;
//   twoDPanel.isCollision = twoDPanel.getCoords().detectCollisions(inProgress);
//   twoDPanel.repaint();
      Point p = scalePoint(e.getLocation(), twoDPanel.getZoomScale());

      if( inProgress.isDoorWindow() )
         twoDPanel.getCoords().moveDoorWindow(inProgress, p);
      else
         twoDPanel.getCoords().moveFurniture(inProgress, p);

      e.acceptDrag(e.getDropAction());
   }

   /** Called when the drag operation has terminated with a drop on the operable
    *  part of the drop site for the DropTarget registered with this listener. */
   public void drop(DropTargetDropEvent e) {
      if (!isDropOk(e)) {
         e.rejectDrop();

         if( inProgress.isDoorWindow() )
            twoDPanel.getCoords().deleteDoorWindow(inProgress);
         else
            twoDPanel.getCoords().delete(inProgress);

         e.dropComplete(false);
      } else {
         e.acceptDrop(acceptableActions); // if you give ACTION_COPY_OR_MOVE, then source will receive MOVE!
         Point p = scalePoint(e.getLocation(), twoDPanel.getZoomScale());

         if( inProgress.isDoorWindow() )
            twoDPanel.getCoords().moveDoorWindow(inProgress, p);
         else
            twoDPanel.getCoords().moveFurniture(inProgress, p);

         e.dropComplete(true);
      }

      inProgress = null;
   }

   /** Called if the user has modified the current drop gesture. */
   public void dropActionChanged(DropTargetDragEvent e) {
      if (!isDragOk(e)) {

         if( inProgress.isDoorWindow() )
            twoDPanel.getCoords().deleteDoorWindow(inProgress);
         else
            twoDPanel.getCoords().delete(inProgress);

         e.rejectDrag();
      } else {
         e.acceptDrag(e.getDropAction());
      }
   }

   private Point scalePoint(Point p, double zoomScale) {
      Point val = new Point();
      val.setLocation(Math.round(p.getLocation().x / zoomScale),
         Math.round(p.getLocation().y / zoomScale));
      return val;
   }
}
