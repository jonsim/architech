import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 
 */
public class Viewport3D {
   private JPanel pane;

   private Main main;

   Viewport3D(Main main) {
      this.main = main;

      initPane();
   }

   private void initPane() {
      pane = new JPanel(new GridBagLayout());
      pane.setBackground(Color.WHITE);
      pane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

      /*
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
      */
   }

   public JPanel getPane() {
      return pane;
   }
}
