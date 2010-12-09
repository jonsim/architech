import java.net.URL;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** Creates and displays the whole main window and adds the various different
 *  JPanels to it.
 *  
 */
public class FrontEnd implements WindowListener {
   public static final String  ICON_LOCATION = "icon.png";
   public static final String  WINDOW_TITLE = "ArchiTECH";

   private final JFrame window = new JFrame(WINDOW_TITLE);

   private Main main;

   /** holds the displayable window and shows or hides sub windows
    * displays dialogs on request by other classes (i.e. crash dialog) */
   FrontEnd(Main main) {
      this.main = main;

      windowInit();
   }

   private void windowInit() {
      window.setJMenuBar(main.frontEndMenu);

      window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      window.addWindowListener(this);

      Image icon = getIcon(ICON_LOCATION);
      if (icon != null) window.setIconImage(icon);

      // puts everything in the window at its default (fairly small) size
      addWindowComponents(window.getContentPane());

      window.setResizable(true);
      window.pack();

      // set minimum size to initialised size, before maximisation
      window.setMinimumSize(window.getSize());
      window.setExtendedState(JFrame.MAXIMIZED_BOTH);
   }

   public void display() {
      window.setLocationRelativeTo(null);
      window.setVisible(true);
      main.viewport2D.repaint();
   }

   /** Returns an image for use as the icon or null if it failed somehow */
   private Image getIcon(String location) {
      URL iconResource = this.getClass().getResource(location);
      if (iconResource == null) return null;

      Image icon = (new ImageIcon(iconResource)).getImage();
      return icon;
   }

   /** Creates and returns a GridBagConstraints with the values given */
   public static GridBagConstraints buildGBC(int x, int y,
         double weightx, double weighty, int anchor, Insets i) {
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = x;
      c.gridy = y;
      c.weightx = weightx;
      c.weighty = weighty;
      c.anchor = anchor;
      c.insets = i;
      return c;
   }

   private void addWindowComponents(Container pane) {
      int leftAnchor = GridBagConstraints.LINE_START;
      int rightAnchor = GridBagConstraints.LINE_END;
      int centerAnchor = GridBagConstraints.CENTER;
      int topCenterAnchor = GridBagConstraints.NORTH;
      int topLeftAnchor = GridBagConstraints.NORTHWEST;
      int topRightAnchor = GridBagConstraints.NORTHEAST;

      Insets top_left_right = new Insets(10,10,0,10);
      Insets top_left_bottom_right = new Insets(10,10,10,10);
      Insets top_right = new Insets(10,0,0,10);
      Insets top_bottom_right = new Insets(10,0,10,10);

      pane.setLayout(new GridBagLayout());
      GridBagConstraints c;

      main.viewport2D.getScrollPane().setPreferredSize(new Dimension(400,180));
      main.viewport3D.getPane().setPreferredSize(new Dimension(400,180));
      main.objectBrowser.getPane().setPreferredSize(new Dimension(160,180));

      c = buildGBC(0, 0, 0.5, 0.0, topCenterAnchor, top_left_right);
      pane.add(main.designButtons.getPane(), c);

      c = buildGBC(1, 0, 0.0, 0.0, topCenterAnchor, top_right);
      pane.add(main.objectButtons.getPane(), c);

      c = buildGBC(0, 1, 1.0, 1.0, leftAnchor, top_left_right);
      c.fill = GridBagConstraints.BOTH;
      pane.add(main.viewport2D.getScrollPane(), c); // viewport2D

      c = buildGBC(1, 1, 0.18, 0.0, topLeftAnchor, top_bottom_right);
      c.gridheight = 2;
      c.fill = GridBagConstraints.BOTH;
      pane.add(main.objectBrowser.getPane(), c);

      c = buildGBC(0, 2, 1.0, 1.0, leftAnchor, top_left_bottom_right);
      c.fill = GridBagConstraints.BOTH;
      pane.add(main.viewport3D.getPane(), c);
      
   }

   /** If a save is needed, asks the user to confirm */
   private boolean quit() {
      if (!main.coordStore.saveRequired()) return true;

      int choice = JOptionPane.showConfirmDialog(window,
         "Save file \"C:\\Something\\Something.obj\" \u003F", "Save",
         JOptionPane.YES_NO_CANCEL_OPTION);

      if (choice == JOptionPane.YES_OPTION) {
         main.coordStore.save();
         return true;

      } else if (choice == JOptionPane.NO_OPTION) {
         return true; // don't save

      } else {
         return false; // user doesn't actually want to exit

      }
   }

   /** Sets the cursor across the whole JPanel */
   public void setWindowCursor(Cursor cursor) {
      window.setCursor(cursor);
   }

//! WINDOWLISTENER
   /** Invoked when the user attempts to close the window from the window's system menu. */
   public void windowClosing(WindowEvent e) {
      if (quit()) window.dispose();
   }

   /** Invoked when the Window is set to be the active Window. */
   public void windowActivated(WindowEvent e) {}
   /** Invoked when a Window is no longer the active Window. */
   public void windowDeactivated(WindowEvent e) {}
   /** Invoked when a window is changed from a minimised to a normal state. */
   public void windowDeiconified(WindowEvent e) {}
   /** Invoked when a window is changed from a normal to a minimised state. */
   public void windowIconified(WindowEvent e) {}
   /** Invoked when a window has been closed as the result of calling dispose on the window. */
   public void windowClosed(WindowEvent e) {}
   /** Invoked the first time a window is made visible. */
   public void windowOpened(WindowEvent e) {}
}
