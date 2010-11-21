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
public class DesignButtons implements ActionListener {

   private Main main;
   private JPanel pane;
   private JButton selectTool, lineTool, curveTool;
   private Cursor selectCursor, lineCursor, curveCursor;
   private JToggleButton gridTool, snapTool;

   public boolean isLineTool() {
      /* this code will work for now but if the button doesn't disable it will fail */
      if (!lineTool.isEnabled()) return true;
      else return false;
   }

   DesignButtons(Main main) {
      this.main = main;
      initCursors();
      initButtons();
      initPane();
   }

   private void initCursors() {
      selectCursor = new Cursor(Cursor.DEFAULT_CURSOR);
      lineCursor = new Cursor(Cursor.DEFAULT_CURSOR);
      curveCursor = new Cursor(Cursor.DEFAULT_CURSOR);
   }

   private void initButtons() {
      selectTool = new JButton("Sel");
        lineTool = new JButton("Lne");
       curveTool = new JButton("Crv");
        gridTool = new JToggleButton("Grd");
        snapTool = new JToggleButton("Snp");

      selectTool.addActionListener(this);
        lineTool.addActionListener(this);
       curveTool.addActionListener(this);
        gridTool.addActionListener(this);
        snapTool.addActionListener(this);

      selectTool.setEnabled(false);
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
      pane.setBorder(BorderFactory.createTitledBorder("Design Buttons"));

      GridBagConstraints c;

      c = buildGridBagConstraints(0, 0, 0.5, centerAnchor, right);
      pane.add(selectTool, c);

      c = buildGridBagConstraints(1, 0, 0.5, centerAnchor, right);
      pane.add(lineTool, c);

      c = buildGridBagConstraints(2, 0, 0.5, centerAnchor, right);
      pane.add(curveTool, c);

      c = buildGridBagConstraints(3, 0, 0.5, centerAnchor, right);
      pane.add(gridTool, c);

      c = buildGridBagConstraints(4, 0, 0.5, centerAnchor, none);
      pane.add(snapTool, c);

   }

   public JPanel getPane() {
      return pane;
   }

   private void disableButtonEnableOthers(JButton button) {
      button.setEnabled(false);

      if (button == selectTool) {
         lineTool.setEnabled(true);
         curveTool.setEnabled(true);
         
      } else if (button == lineTool) {
         selectTool.setEnabled(true);
         curveTool.setEnabled(true);

      } else if (button == curveTool) {
         selectTool.setEnabled(true);
         lineTool.setEnabled(true);
      }
   }

   public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();

      if (selectTool == source) {
         main.frontEnd.setWindowCursor(selectCursor);
         disableButtonEnableOthers(selectTool);

      } else if (lineTool == source) {
         main.frontEnd.setWindowCursor(lineCursor);
         disableButtonEnableOthers(lineTool);

      } else if (curveTool == source) {
         main.frontEnd.setWindowCursor(curveCursor);
         disableButtonEnableOthers(curveTool);

      } else if (gridTool == source) {
         // toggle grid showing
         
      } else if (snapTool == source) {
         // toggle snap showing

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
