import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.awt.Point;

/** Drop Listener for 2D end of the drag and drop furniture system */
class TwoDDropListener implements DropTargetListener {
   public static final int acceptableActions = DnDConstants.ACTION_COPY;
   private TwoDPanel twoDPanel;
   private ObjectBrowser objectBrowser;
   
   private FurnitureSQLData rawData; // rawData is not set to null after each drag
   private Furniture inProgress; // inProgress is however...

   TwoDDropListener(TwoDPanel twoDPanel, ObjectBrowser objectBrowser) {
      if (twoDPanel == null || objectBrowser == null) throw new IllegalArgumentException("null parameter");
      this.twoDPanel = twoDPanel;
      this.objectBrowser = objectBrowser;
   }

   private Point scalePoint(Point p, double zoomScale) {
      Point val = new Point();
      val.setLocation(Math.round(p.getLocation().x / zoomScale),
         Math.round(p.getLocation().y / zoomScale));
      return val;
   }

   private FurnitureSQLData getRawDataFromEvent(DropTargetDragEvent e, DataFlavor chosen) {
      try {
         Object data = e.getTransferable().getTransferData(chosen);
         if (data instanceof FurnitureSQLData) {
            return (FurnitureSQLData) data;
         }
      } catch (UnsupportedFlavorException ufe) {
      } catch (IOException ioe) {
      }

      return null;
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
      } else {
         return null;
      }
      // return DataFlavor.stringFlavor if supported, etc...
   }

   /** Called by drop Checks the flavors */
   private DataFlavor chooseDropFlavor(DropTargetDragEvent e) {
      if (/*e.isLocalTransfer() &&*/ e.isDataFlavorSupported(TransferData.furniture)) {
         return TransferData.furniture;
      } else {
         return null;
      }
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
      if ( inProgress.isDoorWindow() && twoDPanel.getCoords().doorWindowInvalidPosition(inProgress) ) return false;

      return true;
   }

   /** Called while a drag operation is ongoing, when the mouse pointer enters the
    *  operable part of the drop site for the DropTarget registered with this listener. */
   public void dragEnter(DropTargetDragEvent e) {
//System.out.println("ENTER");

      DataFlavor chosen = chooseDropFlavor(e);

      if (!isDragOk(e) || chosen == null) {
         e.rejectDrag();

      } else {
         rawData = getRawDataFromEvent(e, chosen);
         if (rawData == null) {
            e.rejectDrag();
            return;
         }

         Point p = scalePoint(e.getLocation(), twoDPanel.getZoomScale());
         Furniture f = new Furniture( rawData, p, objectBrowser);

         inProgress = f;

         if (inProgress.isDoorWindow()) {
            twoDPanel.getCoords().addDoorWindow(inProgress);
         } else {
            twoDPanel.getCoords().addFurniture(inProgress);
         }

         twoDPanel.setFurnitureAsHandlerAndStart(inProgress);

//         e.acceptDrag(e.getDropAction());
      }
   }

   /** Called while a drag operation is ongoing, when the mouse pointer has exited
    *  the operable part of the drop site for the DropTarget registered with this listener. */
   public void dragExit(DropTargetEvent e) {
System.out.println("EXIT");

      twoDPanel.dropFurnitureStopHandlerCall();
      twoDPanel.dropFurnitureHandlerForgetFurniture();

      if( inProgress.isDoorWindow() ) {
         twoDPanel.getCoords().deleteDoorWindow(inProgress);
         twoDPanel.repaint();
      } else {
         twoDPanel.getCoords().delete(inProgress);
      }

      // MAYBE NOT - remember inProgress as the same drag might continue again
      inProgress = null;
   }

   /** Called when a drag operation is ongoing, while the mouse pointer is still over
    * the operable part of the drop site for the DropTarget registered with this listener. */
   public void dragOver(DropTargetDragEvent e) {
      Point p = scalePoint(e.getLocation(), twoDPanel.getZoomScale());
//      System.out.println(p);

      if (inProgress == null) {
         // DRAGENTER COPY
         inProgress = new Furniture( rawData, p, objectBrowser);

         if (inProgress.isDoorWindow()) {
            twoDPanel.getCoords().addDoorWindow(inProgress);
         } else {
            twoDPanel.getCoords().addFurniture(inProgress);
         }

         twoDPanel.setFurnitureAsHandlerAndStart(inProgress);

//         e.acceptDrag(e.getDropAction());
         
      } else {
         // dragEnter is not called for every entry, so re-add the furniture if it
         // was deleted from coords by dragExit. This will do nothing if already added
         if( inProgress.isDoorWindow() ) {
            twoDPanel.getCoords().moveDoorWindow(inProgress, p);
         } else {
            twoDPanel.dropFurnitureMiddleHandlerCall(p);
         }

//         e.acceptDrag(e.getDropAction());
      }
   }

   /** Called when the drag operation has terminated with a drop on the operable
    *  part of the drop site for the DropTarget registered with this listener. */
   public void drop(DropTargetDropEvent e) {
//System.out.println("   drop");

      if (!isDropOk(e)) {
         e.rejectDrop();

         twoDPanel.dropFurnitureStopHandlerCall();
         twoDPanel.dropFurnitureHandlerForgetFurniture();

         if( inProgress.isDoorWindow() ) {
            twoDPanel.getCoords().deleteDoorWindow(inProgress);
            twoDPanel.repaint();
         } else {
            twoDPanel.getCoords().delete(inProgress);
         }

         e.dropComplete(false);

      } else {
         e.acceptDrop(acceptableActions); // if you give ACTION_COPY_OR_MOVE, then source will receive MOVE!
         Point p = scalePoint(e.getLocation(), twoDPanel.getZoomScale());

         if( inProgress.isDoorWindow() ) {
            twoDPanel.getCoords().moveDoorWindow(inProgress, p);
         } else {
            twoDPanel.getCoords().moveFurniture(inProgress, p);            
         }

         twoDPanel.dropFurnitureStopHandlerCall();
         twoDPanel.dropFurnitureHandlerForgetFurniture();

         e.dropComplete(true);
      }

      inProgress = null;
   }

   /** Called if the user has modified the current drop gesture. */
   public void dropActionChanged(DropTargetDragEvent e) {
//System.out.println("    CHANGED");

      if (!isDragOk(e)) {
         if( inProgress.isDoorWindow() ) {
            twoDPanel.getCoords().deleteDoorWindow(inProgress);
         } else {
            twoDPanel.getCoords().delete(inProgress);
         }

         e.rejectDrag();
      } else {
         e.acceptDrag(e.getDropAction());
      }
   }
}
