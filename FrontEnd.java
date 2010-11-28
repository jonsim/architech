import java.net.URL;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class FrontEnd implements WindowListener {
   public static final String  ICON_LOCATION = "icon.png";
   public static final String  WINDOW_TITLE = "ArchiTECH";

   private final JMenuBar menuBar = new JMenuBar();
   private final JFrame window = new JFrame(WINDOW_TITLE);

   private Main          main;
   private Viewport2D    viewport2D;
   private Viewport3D    viewport3D;
   private ObjectBrowser objectBrowser;

   /** holds the displayable window and shows or hides sub windows
    * displays dialogs on request by other classes (i.e. crash dialog) */
   FrontEnd(Main main) {
      this.main = main;

      viewport2D = new Viewport2D(main);
      viewport2D.addMouseListener(viewport2D);
      viewport2D.addMouseMotionListener(viewport2D);
      viewport3D = new Viewport3D(main);
      objectBrowser = new ObjectBrowser(main);

      menuInit();
      windowInit();
   }

   private void menuInit() {
      JMenu menu;

      menu = new JMenu("File");
      menu.setMnemonic(KeyEvent.VK_F);
      menu.getAccessibleContext().setAccessibleDescription("Main option menu");
      menuBar.add(menu);

      menu = new JMenu("Edit");
      menu.setMnemonic(KeyEvent.VK_E);
      menu.getAccessibleContext().setAccessibleDescription("Main option menu");
      menuBar.add(menu);

      menu = new JMenu("View");
      menu.setMnemonic(KeyEvent.VK_V);
      menu.getAccessibleContext().setAccessibleDescription("Main option menu");
      menuBar.add(menu);

      menu = new JMenu("Customisation");
      menu.setMnemonic(KeyEvent.VK_C);
      menu.getAccessibleContext().setAccessibleDescription("Main option menu");
      menuBar.add(menu);

      menu = new JMenu("Window");
      menu.setMnemonic(KeyEvent.VK_W);
      menu.getAccessibleContext().setAccessibleDescription("Main option menu");
      menuBar.add(menu);

      menu = new JMenu("Help");
      menu.setMnemonic(KeyEvent.VK_H);
      menu.getAccessibleContext().setAccessibleDescription("Main option menu");
      menuBar.add(menu);

   }

   private void windowInit() {
      window.setJMenuBar(menuBar);

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
      viewport2D.repaint();
   }

   /** Returns an image for use as the icon or null if it failed somehow */
   private Image getIcon(String location) {
      URL iconResource = this.getClass().getResource(location);
      if (iconResource == null) return null;

      Image icon = (new ImageIcon(iconResource)).getImage();
      return icon;
   }

   private GridBagConstraints buildGBC(int x, int y,
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

      viewport2D.getScrollPane().setPreferredSize(new Dimension(400,180));
      viewport3D.getPane().setPreferredSize(new Dimension(400,180));
      objectBrowser.getPane().setPreferredSize(new Dimension(160,180));

      c = buildGBC(0, 0, 0.5, 0.0, topCenterAnchor, top_left_right);
      pane.add(main.designButtons.getPane(), c);

      c = buildGBC(1, 0, 0.0, 0.0, topCenterAnchor, top_right);
      pane.add(main.objectButtons.getPane(), c);

      c = buildGBC(0, 1, 1.0, 1.0, leftAnchor, top_left_right);
      c.fill = GridBagConstraints.BOTH;
      pane.add(viewport2D.getScrollPane(), c); // viewport2D

      c = buildGBC(1, 1, 0.18, 0.0, topLeftAnchor, top_bottom_right);
      c.gridheight = 2;
      c.fill = GridBagConstraints.BOTH;
      pane.add(objectBrowser.getPane(), c);

      c = buildGBC(0, 2, 1.0, 1.0, leftAnchor, top_left_bottom_right);
      c.fill = GridBagConstraints.BOTH;
      pane.add(viewport3D.getPane(), c);
      
   }

   private boolean saveRequired() {
      return true;
   }

   private void save() {
      // save

      // if unable to save, ask if the user wants to save somewhere else
   }

   /** If a save is needed, asks the user to confirm */
   private boolean quit() {
      if (!saveRequired()) return true;

      int choice = JOptionPane.showConfirmDialog(window,
         "Save file \"C:\\Something\\Something.obj\" \u003F", "Save",
         JOptionPane.YES_NO_CANCEL_OPTION);

      if (choice == JOptionPane.YES_OPTION) {
         save();
         return true;

      } else if (choice == JOptionPane.NO_OPTION) {
         return true; // don't save

      } else {
         return false; // user doesn't actually want to exit

      }
   }

   public void setWindowCursor(Cursor cursor) {
      window.setCursor(cursor);
   }

//! WINDOWLISTENER
   /** Invoked when the Window is set to be the active Window. */
   public void windowActivated(WindowEvent e) {
   }

   /** Invoked when a window has been closed as the result of calling dispose on the window. */
   public void windowClosed(WindowEvent e) {
   }

   /** Invoked when the user attempts to close the window from the window's system menu. */
   public void windowClosing(WindowEvent e) {
      if (quit()) window.dispose();
   }

   /** Invoked when a Window is no longer the active Window. */
   public void windowDeactivated(WindowEvent e) {
   }

   /** Invoked when a window is changed from a minimised to a normal state. */
   public void windowDeiconified(WindowEvent e) {
   }

   /** Invoked when a window is changed from a normal to a minimised state. */
   public void windowIconified(WindowEvent e) {
   }

   /** Invoked the first time a window is made visible. */
   public void windowOpened(WindowEvent e) {
   }
}
