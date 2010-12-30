import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

/** Drop supporting class */
public class DropLabel extends JLabel {

   private Color borderColor;
   private DropTarget dropTarget;
   private DropTargetListener dtListener;
   private int acceptableActions; // DnDConstants.ACTION_MOVE, DnDConstants.ACTION_COPY_OR_MOVE

   /** Sets ACTION_COPY as the acceptableAction */
   public DropLabel(String s) {
      this(s, DnDConstants.ACTION_COPY);
   }

   /** Sets custom acceptableAction */
   public DropLabel(String s, int a) {
      if (a != DnDConstants.ACTION_NONE
              && a != DnDConstants.ACTION_COPY
              && a != DnDConstants.ACTION_MOVE
              && a != DnDConstants.ACTION_COPY_OR_MOVE
              && a != DnDConstants.ACTION_LINK) {
         throw new IllegalArgumentException("Invalid DnDConstants.ACTION_...: " + a);
      }
      this.acceptableActions = a;
      
      this.setText(s);
      this.borderColor = Color.green;
      this.dtListener = new DTListener();

      // component, ops, listener, accepting
      this.dropTarget = new DropTarget(this, this.acceptableActions, this.dtListener, true);
   }

   /** Sets a 10pixel border if b=true of colour this.borderColor, otherwise removes it */
   private void showBorder(boolean b) {
      if (b) this.setBorder(BorderFactory.createLineBorder(this.borderColor, 10));
      else this.setBorder(BorderFactory.createEmptyBorder());
      
      this.getParent().validate();
      this.repaint();
   }
















   /** DTListener - a listener that tracks the state of the operation
    *  @see java.awt.dnd.DropTargetListener
    *  @see java.awt.dnd.DropTarget
    */
   class DTListener implements DropTargetListener {

      /** Called by isDragOk. Checks to see if the flavor drag flavor is acceptable
       *  @param e the DropTargetDragEvent object
       *  @return whether the flavor is acceptable
       */
      private boolean isDragFlavorSupported(DropTargetDragEvent e) {
         boolean ok = false;
         
         if (e.isDataFlavorSupported(StringTransferable.plainTextFlavor)) ok = true;
         else if (e.isDataFlavorSupported(StringTransferable.localStringFlavor)) ok = true;
         else if (e.isDataFlavorSupported(DataFlavor.stringFlavor)) ok = true;
         else if (e.isDataFlavorSupported(DataFlavor.plainTextFlavor)) ok = true;
         
         return ok;
      }

      /** Called by drop
       *  Checks the flavors and operations
       *  @param e the DropTargetDropEvent object
       *  @return the chosen DataFlavor or null if none match
       */
      private DataFlavor chooseDropFlavor(DropTargetDropEvent e) {
         if (e.isLocalTransfer() && e.isDataFlavorSupported(StringTransferable.localStringFlavor)) {
            return StringTransferable.localStringFlavor;
         }

         DataFlavor chosen = null;
         
         if (e.isDataFlavorSupported(StringTransferable.plainTextFlavor)) {
            chosen = StringTransferable.plainTextFlavor;
         } else if (e.isDataFlavorSupported(StringTransferable.localStringFlavor)) {
            chosen = StringTransferable.localStringFlavor;
         } else if (e.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            chosen = DataFlavor.stringFlavor;
         } else if (e.isDataFlavorSupported(DataFlavor.plainTextFlavor)) {
            chosen = DataFlavor.plainTextFlavor;
         }

         return chosen;
      }

      /** Called by dragEnter and dragOver
       *  Checks the flavors and operations
       *  @param e the event object
       *  @return whether the flavor and operation is ok
       */
      private boolean isDragOk(DropTargetDragEvent e) {
         if (isDragFlavorSupported(e) == false) {
            System.out.println("isDragOk:no flavors chosen");
            return false;
         }

         // the actions specified when the source
         // created the DragGestureRecognizer
         //      int sa = e.getSourceActions();

         // the docs on DropTargetDragEvent rejectDrag says that
         // the dropAction should be examined
         System.out.print("dt drop action " + e.getDropAction());
         System.out.println(" my acceptable actions " + acceptableActions);

         // we're saying that these actions are necessary
         if ((e.getDropAction() & DropLabel.this.acceptableActions) == 0) return false;
         else return true;
      }

      /** start "drag under" feedback on component
       *  invoke acceptDrag or rejectDrag based on isDragOk
       */
      public void dragEnter(DropTargetDragEvent e) {
System.out.println("dtlistener dragEnter");

         if (!isDragOk(e)) {
System.out.println("enter not ok");
            DropLabel.this.borderColor = Color.red;
            showBorder(true);
            e.rejectDrag();
            return;
         }

         DropLabel.this.borderColor = Color.green;
         showBorder(true);
System.out.println("dt enter: accepting " + e.getDropAction());

         e.acceptDrag(e.getDropAction());
      }

      /**
       * continue "drag under" feedback on component
       * invoke acceptDrag or rejectDrag based on isDragOk
       */
      public void dragOver(DropTargetDragEvent e) {
         if (!isDragOk(e)) {
System.out.println("dtlistener dragOver not ok");
            DropLabel.this.borderColor = Color.red;
            showBorder(true);
            e.rejectDrag();
            return;
         }

System.out.println("dt over: accepting");
         e.acceptDrag(e.getDropAction());
      }

      /** dropActionChanged */
      public void dropActionChanged(DropTargetDragEvent e) {
         if (isDragOk(e) == false) {
System.out.println("dtlistener changed not ok");
            e.rejectDrag();
            return;
         }
System.out.println("dt changed: accepting" + e.getDropAction());
         e.acceptDrag(e.getDropAction());
      }

      /** dragExit */
      public void dragExit(DropTargetEvent e) {
System.out.println("dtlistener dragExit");
         DropLabel.this.borderColor = Color.green;
         showBorder(false);
      }

      /** perform action from getSourceActions. on the transferrable. invoke
       *  acceptDrop or rejectDrop. invoke dropComplete. if its a local (same JVM)
       *  transfer, use StringTransferable.localStringFlavor. find a match for the
       *  flavor. check the operation. get the transferable according to the chosen
       *  flavor. do the transfer */
      public void drop(DropTargetDropEvent e) {
System.out.println("dtlistener drop");

         DataFlavor chosen = chooseDropFlavor(e);

         if (chosen == null) {
System.err.println("No flavor match found");
            e.rejectDrop();
            return;
         }
System.err.println("Chosen data flavor is " + chosen.getMimeType());

         // the actions that the source has specified with DragGestureRecognizer
System.out.println("drop: sourceActions: " + e.getSourceActions());
System.out.println("drop: dropAction: " + e.getDropAction()); // dropAction = the actual operation

         if ((e.getSourceActions() & DropLabel.this.acceptableActions) == 0) {
System.err.println("No action match found");
            e.rejectDrop();
            showBorder(false);
            return;
         }

         Object data = null;
         try {
            /* the source listener receives this action in dragDropEnd.
             * if the action is DnDConstants.ACTION_COPY_OR_MOVE then
             * the source receives MOVE! */
            e.acceptDrop(DropLabel.this.acceptableActions);
            // DnDConstants.ACTION_MOVE / DnDConstants.ACTION_COPY / DnDConstants.ACTION_COPY_OR_MOVE

            data = e.getTransferable().getTransferData(chosen);
            if (data == null) throw new NullPointerException();

         } catch (Throwable t) {
System.err.println("Couldn't get transfer data: " + t.getMessage());
            t.printStackTrace();
            e.dropComplete(false);
            showBorder(false);
            return;
         }
System.out.println("Got data: " + data.getClass().getName());

         if (data instanceof String) {
            String s = (String) data;
            DropLabel.this.setText(s);
            
         } else if (data instanceof InputStream) {
            InputStream input = (InputStream) data;
            InputStreamReader isr = null;
            // BufferedReader br = null;
            
            try {
               // br = new BufferedReader(isr=new InputStreamReader(input,"Unicode"));
               isr = new InputStreamReader(input, "Unicode");
               
            } catch (UnsupportedEncodingException uee) {
               isr = new InputStreamReader(input);
            }

            StringBuffer str = new StringBuffer();
            int in = -1;
            try {
               while ((in = isr.read()) >= 0) {
                  if (in != 0) str.append((char) in);
               }

               /* you get garbage chars this way
               try {
                  String line=null;
                  while ((line = br.readLine()) != null) {
                     str.append(line);
                     str.append('\n');
                     System.out.println( "read: " + line);
                     System.out.println( "read: " + (int)line.charAt(line.length()-1));
                  }
                  br.close();
               } */

               DropLabel.this.setText(str.toString());

            } catch (IOException ioe) {
               /* bug #4094987
                  sun.io.MalformedInputException: Missing byte-order mark
                  e.g. if dragging from MS Word 97
                  still a bug in 1.2 final */

System.err.println("cannot read" + ioe);
               e.dropComplete(false);
               showBorder(false);
               String message = "Bad drop\n" + ioe.getMessage();
               JOptionPane.showMessageDialog(DropLabel.this, message, "Error", JOptionPane.ERROR_MESSAGE);
               return;
            }

         } else {
System.out.println("drop: rejecting");
            e.dropComplete(false);
            showBorder(false);
            return;
         }

         e.dropComplete(true);
         showBorder(false);
      }
   }












   /** Creates an individual Drop Target */
   public static void main(String[] args) {
      JFrame frame = new JFrame();
      frame.setTitle("DropLabel test");
      frame.setSize(300, 300);
      frame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });

      DropLabel l = new DropLabel("Drop here");
      l.setBackground(Color.black);
      l.setForeground(Color.yellow);

      Container pane = frame.getContentPane();
      pane.add(l);

      frame.setVisible(true);
   }
}
