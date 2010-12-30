import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.*;

/** Drag supporting class */
public class DragLabel extends JLabel {

   private DragSource dragSource;
   private DragGestureListener dgListener;
   private DragSourceListener dsListener;
   private int dragAction;

   /** Creates a new DragLabel with dragAction COPY */
   public DragLabel(String s) {
      this(s, DnDConstants.ACTION_COPY);
   }

   /** Creates a new DragLabel and sets a custom dragAction */
   public DragLabel(String s, int a) {
      if (a != DnDConstants.ACTION_NONE
              && a != DnDConstants.ACTION_COPY
              && a != DnDConstants.ACTION_MOVE
              && a != DnDConstants.ACTION_COPY_OR_MOVE
              && a != DnDConstants.ACTION_LINK) {
         throw new IllegalArgumentException("Invalid DnDConstants.ACTION_...: " + a);
      }
      this.dragAction = a;
      
      this.setText(s);
      this.setOpaque(true);
      this.dragSource = DragSource.getDefaultDragSource();
      this.dgListener = new DGListener();
      this.dsListener = new DSListener();

      // component, action, listener
      this.dragSource.createDefaultDragGestureRecognizer(this, this.dragAction, this.dgListener);
   }













   /**
    * DGListener
    * a listener that will start the drag.
    * has access to top level's dsListener and dragSource
    * @see java.awt.dnd.DragGestureListener
    * @see java.awt.dnd.DragSource
    * @see java.awt.datatransfer.StringSelection
    */
   class DGListener implements DragGestureListener {

      /**
       * Start the drag if the operation is ok.
       * uses java.awt.datatransfer.StringSelection to transfer
       * the label's data
       * @param e the event object
       */
      public void dragGestureRecognized(DragGestureEvent e) {

         // if the action is ok we go ahead
         // otherwise we punt
System.out.println(e.getDragAction());
         if ((e.getDragAction() & DragLabel.this.dragAction) == 0) return;
System.out.println("kicking off drag");

         // get the label's text and put it inside a Transferable
         // Transferable transferable = new StringSelection( DragLabel.this.getText() );
         Transferable transferable = new StringTransferable(DragLabel.this.getText());

         // now kick off the drag
         try {
            // initial cursor, transferrable, dsource listener
            e.startDrag(DragSource.DefaultCopyNoDrop, transferable, DragLabel.this.dsListener);

            // or if dragSource is a variable
            // dragSource.startDrag(e, DragSource.DefaultCopyDrop, transferable, dsListener);


            // or if you'd like to use a drag image if supported

            /*
            if(DragSource.isDragImageSupported() )
            // cursor, image, point, transferrable, dsource listener
            e.startDrag(DragSource.DefaultCopyDrop, image, point, transferable, dsListener);
             */

         } catch (InvalidDnDOperationException idoe) {
            System.err.println(idoe);
         }
      }
   }













   /**
    * DSListener
    * a listener that will track the state of the DnD operation
    *
    * @see java.awt.dnd.DragSourceListener
    * @see java.awt.dnd.DragSource
    * @see java.awt.datatransfer.StringSelection
    */
   class DSListener implements DragSourceListener {

      /** This method is invoked to signify that the Drag and Drop operation is complete. */
      public void dragDropEnd(DragSourceDropEvent e) {
         if (e.getDropSuccess() == false) {
System.out.println("not successful");
            return;
         }

         // the dropAction should be what the drop target specified in acceptDrop
System.out.println("dragdropend action " + e.getDropAction());

         // this is the action selected by the drop target
         if (e.getDropAction() == DnDConstants.ACTION_MOVE) {
            DragLabel.this.setText("");
         }
      }

      /** Called as the cursor's hotspot enters a platform-dependent drop site. */
      public void dragEnter(DragSourceDragEvent e) {
System.out.println("draglabel enter " + e);
         DragSourceContext context = e.getDragSourceContext();

         //intersection of the users selected action, and the source and target actions
         int myaction = e.getDropAction();
         if ((myaction & DragLabel.this.dragAction) != 0) {
            context.setCursor(DragSource.DefaultCopyDrop);
         } else {
            context.setCursor(DragSource.DefaultCopyNoDrop);
         }
      }

      /** Called as the cursor's hotspot moves over a platform-dependent drop site. */
      public void dragOver(DragSourceDragEvent e) {
         DragSourceContext context = e.getDragSourceContext();
         int sa = context.getSourceActions();
         int ua = e.getUserAction();
         int da = e.getDropAction();
         int ta = e.getTargetActions();
System.out.println("dl dragOver source actions" + sa);
System.out.println("user action" + ua);
System.out.println("drop actions" + da);
System.out.println("target actions" + ta);
      }

      /** Called as the cursor's hotspot exits a platform-dependent drop site. */
      public void dragExit(DragSourceEvent e) {
System.out.println("draglabel exit " + e);
         DragSourceContext context = e.getDragSourceContext();
      }

      /** Called when the user has modified the drop gesture. (for example, press
       *  shift during drag to change to a link action */
      public void dropActionChanged(DragSourceDragEvent e) {
         DragSourceContext context = e.getDragSourceContext();
         context.setCursor(DragSource.DefaultCopyNoDrop);
      }
   }

   
   
   
   
   
   
   
   
   
   /** Creates an individual DragLabel */
   public static void main(String[] args) {
      JFrame frame = new JFrame();
      frame.setTitle("DragLabel test");
      frame.setSize(300, 300);
      frame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });

      DragLabel l = new DragLabel("Here is some text");
      l.setBackground(Color.black);
      l.setForeground(Color.yellow);

      Container pane = frame.getContentPane();
      pane.add(l);

      frame.setVisible(true);
   }
}
