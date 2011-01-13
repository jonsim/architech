import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.*;

/** DSListener. a listener that will track the state of the DnD operation */
public class SQLDragListener implements DragGestureListener, DragSourceListener {

   public static final int dragAction = DnDConstants.ACTION_COPY;
   private FurnitureSQLData dataToBeTransferred;

   /** These three lines control the SQL end of drag and drop furniture */
   private void makeSQLObjectDraggable(Component toMakeDraggable, int furnitureID, float rectangleWidth, float rectangleHeight) {
      FurnitureSQLData dataToBeTransferred = new FurnitureSQLData(furnitureID, rectangleWidth, rectangleHeight);
      SQLDragListener dragListener = new SQLDragListener(dataToBeTransferred);
      DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(toMakeDraggable, SQLDragListener.dragAction, dragListener);
   }

   SQLDragListener(FurnitureSQLData dataToBeTransferred) {
      if (dataToBeTransferred == null) {
         throw new IllegalArgumentException("dataToBeTransferred is null");
      }
      
      this.dataToBeTransferred = dataToBeTransferred;
   }

   /** Start the drag if the operation is ok. uses java.awt.datatransfer.StringSelection
    *  to transfer the label's data */
   public void dragGestureRecognized(DragGestureEvent e) {
      if ((e.getDragAction() & dragAction) == 0) return;

      // get the label's text and put it inside a Transferable
      // Transferable transferable = new StringSelection( DragLabel.this.getText() );
      Transferable transferData = new TransferData(dataToBeTransferred);

      try {
         // initial cursor, transferrable, dsource listener
         DragSourceListener dsListener = this;
         e.startDrag(DragSource.DefaultCopyNoDrop, transferData, dsListener);

         // or if dragSource is a variable
         // dragSource.startDrag(e, DragSource.DefaultCopyDrop, transferable, dsListener);

         // or if you'd like to use a drag image if supported

         /*
         if(DragSource.isDragImageSupported() )
         // cursor, image, point, transferrable, dsource listener
         e.startDrag(DragSource.DefaultCopyDrop, image, point, transferable, dsListener);
          */

      } catch (InvalidDnDOperationException err) {
         System.err.println(err);
      }
   }








   /** This method is invoked to signify that the Drag and Drop operation is complete. */
   public void dragDropEnd(DragSourceDropEvent e) {
      if (e.getDropSuccess() == false) {
         System.err.println("Drag operation not successful");
         return;
      }

      // the dropAction should be what the drop target specified in acceptDrop
      if (e.getDropAction() == DnDConstants.ACTION_MOVE) {
         // nothing to delete/cut from the source end of the drag
      }
   }

   /** Called as the cursor's hotspot enters a platform-dependent drop site. */
   public void dragEnter(DragSourceDragEvent e) {
      DragSourceContext context = e.getDragSourceContext();

      //intersection of the users selected action, and the source and target actions
      if ((e.getDropAction() & dragAction) != 0) {
         context.setCursor(DragSource.DefaultCopyDrop);
      } else {
         context.setCursor(DragSource.DefaultCopyNoDrop);
      }
   }

   /** Called as the cursor's hotspot moves over a platform-dependent drop site. */
   public void dragOver(DragSourceDragEvent e) {
//      DragSourceContext context = e.getDragSourceContext();
//      int sa = context.getSourceActions();
//      int ua = e.getUserAction();
//      int da = e.getDropAction();
//      int ta = e.getTargetActions();
   }

   /** Called as the cursor's hotspot exits a platform-dependent drop site. */
   public void dragExit(DragSourceEvent e) {
      DragSourceContext context = e.getDragSourceContext();
      context.setCursor(DragSource.DefaultCopyNoDrop);
   }

   /** Called when the user has modified the drop gesture. (for example, press
    *  shift during drag to change to a link action */
   public void dropActionChanged(DragSourceDragEvent e) {
      DragSourceContext context = e.getDragSourceContext();

      //intersection of the users selected action, and the source and target actions
      if ((e.getDropAction() & dragAction) != 0) {
         context.setCursor(DragSource.DefaultCopyDrop);
      } else {
         context.setCursor(DragSource.DefaultCopyNoDrop);
      }
   }
}
