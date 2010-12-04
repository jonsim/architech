import java.net.URL;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;


/**
 *
 * @author James
 */
public class ObjectButtons implements ActionListener {

   private Main main;
   private JPanel pane;
   private JButton addObject, removeObject;

   ObjectButtons(Main main) {
      this.main = main;

      initButtons();
      initPane();
   }

   private void initButtons() {
         addObject = new JButton("Add");
      removeObject = new JButton("Del");

         addObject.addActionListener(this);
      removeObject.addActionListener(this);
   }

   private void initPane() {
      int leftAnchor = GridBagConstraints.LINE_START;
      int rightAnchor = GridBagConstraints.LINE_END;
      int centerAnchor = GridBagConstraints.CENTER;
      int topLeftAnchor = GridBagConstraints.NORTHWEST;
      int topRightAnchor = GridBagConstraints.NORTHEAST;

      Insets top_left_right = new Insets(10,10,0,10);
      Insets top_left_bottom_right = new Insets(10,10,10,10);
      Insets top_right = new Insets(10,0,0,10);
      Insets top_bottom_right = new Insets(10,0,10,10);
      Insets right = new Insets(0,0,0,10);
      Insets bottom = new Insets(0,0,5,0);
      Insets none = new Insets(0,0,0,0);

      pane = new JPanel(new GridBagLayout());
      pane.setBorder(BorderFactory.createTitledBorder("Object Buttons"));

      GridBagConstraints c;

      c = buildGridBagConstraints(0, 0, 0.5, centerAnchor, right);
      pane.add(addObject, c);

      c = buildGridBagConstraints(1, 0, 0.5, centerAnchor, right);
      pane.add(removeObject, c);

   }

   public JPanel getPane() {
      return pane;
   }

   public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();

      if (addObject == source) {
		 main.frontEnd.getObjectBrowser().selectCategory();
      } else if (removeObject == source) {
         main.frontEnd.getObjectBrowser().toCategories();
      } else Main.showFatalExceptionTraceWindow(
                new Exception("BUG: Action ocurred with unexpected source (" + e.getSource().toString() + ")"));
   }

   private GridBagConstraints buildGridBagConstraints(int x, int y,
         double weightx, int anchor, Insets i) {
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = x;
      c.gridy = y;
      c.weightx = weightx;
      c.anchor = anchor;
      c.insets = i;
      return c;
   }

}