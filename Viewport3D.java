import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.dnd.*;

/**  */
public class Viewport3D extends JPanel {

   private DragSource dragSource;
   private DragGestureListener dgListener;

   private Main main;

   Viewport3D(Main main) {
      super(new GridBagLayout());
      this.setBackground(Color.WHITE);
      this.setBorder(BorderFactory.createLineBorder(Color.GRAY));

      this.main = main;

      dragSource = DragSource.getDefaultDragSource();
      dgListener = new SQLDragListener();

      // component, action, listener
      dragSource.createDefaultDragGestureRecognizer(this, SQLDragListener.dragAction, dgListener);



      //initPane();
   }

   @Override
   public void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;

      g2.drawImage(main.frontEnd.getIcon(main.frontEnd.ICON_LOCATION), new AffineTransform(), null);

   }

   private void initPane() {


      //pane = new JPanel(new GridBagLayout());
      //pane.setBackground(Color.WHITE);
      //pane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

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
      return this;
   }
}
