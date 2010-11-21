import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author James
 */
public class ObjectBrowser {
   private Main main;
   private JPanel pane;

   ObjectBrowser(Main main) {
      this.main = main;

      initPane();
   }

   private void initPane() {
      pane = new JPanel(new GridBagLayout());
      pane.setBackground(Color.WHITE);
      pane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
   }

   public JPanel getPane() {
      return pane;
   }
}
