import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.awt.dnd.*;

/** Test */
public class Test extends JPanel {

   /** Creates a DragLabel with white background */
   private DragLabel createDragLabel(String string, int action) {
      DragLabel dragLabel = new DragLabel(string, action);
      dragLabel.setBackground(Color.white);
      dragLabel.setOpaque(true);
      return dragLabel;
   }

   /** Creates a Drop Label with background colour yellow */
   private DropLabel createDropLabel(String string, int action) {
      DropLabel dropLabel = new DropLabel(string, action);
      dropLabel.setBackground(Color.yellow);
      dropLabel.setOpaque(true);
      return dropLabel;
   }

   /** Test */
   public Test() {
      this.setLayout(new GridLayout(6, 1, 5, 5));

      add(createDragLabel("C Drag from here", DnDConstants.ACTION_COPY));
      add(createDragLabel("M Drag from here too", DnDConstants.ACTION_MOVE));
      add(createDragLabel("CM Drag from here too", DnDConstants.ACTION_COPY_OR_MOVE));

      add(createDropLabel("C Drop here", DnDConstants.ACTION_COPY));
      add(createDropLabel("M Drop here also", DnDConstants.ACTION_MOVE));
      add(createDropLabel("CM Drop here also", DnDConstants.ACTION_COPY_OR_MOVE));
   }

   /** Creates the full on demo */
   public static void main(String[] args) {
      JFrame frame = new JFrame();
      frame.setTitle("Drag and Drop test");
      frame.setSize(300, 300);
      frame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });

      Container pane = frame.getContentPane();
      pane.add(new Test());

      frame.setVisible(true);
   }
}
