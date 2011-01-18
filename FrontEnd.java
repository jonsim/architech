import java.net.URL;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/** Creates and displays the whole main window and adds the various different
 *  JPanels to it.
 *  
 */
public class FrontEnd implements WindowListener {
   public static final String  ICON_LOCATION = "icon.png";
   public static final String  WINDOW_TITLE = "ArchiTECH";

   private final JFrame window = new JFrame(WINDOW_TITLE);
   private final JSplitPane TwoDandThreeD = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
   private final JTabbedPane tabbedPane = new JTabbedPane();
   private FrontEndMenu frontEndMenu;
   private Main main;

   /** holds the displayable window and shows or hides sub windows
    * displays dialogs on request by other classes (i.e. crash dialog) */
   FrontEnd(Main main) {
      this.main = main;
      
      frontEndMenu = new FrontEndMenu(this);

      TwoDandThreeD.setTopComponent(tabbedPane);
      TwoDandThreeD.setBottomComponent(main.viewport3D.getCanvas());
      TwoDandThreeD.setOneTouchExpandable(true);
      TwoDandThreeD.setDividerSize(11);
      TwoDandThreeD.setBorder(null);
      TwoDandThreeD.setResizeWeight(0.45);

      windowInit();

      TwoDScrollPane newTab = new TwoDScrollPane(main.designButtons);
      tabbedPane.addTab(newTab.getCoords().getAssociatedSaveName(), newTab);
      newTab = new TwoDScrollPane(main.designButtons);
      tabbedPane.addTab(newTab.getCoords().getAssociatedSaveName(), newTab);
      newTab = new TwoDScrollPane(main.designButtons);
      tabbedPane.addTab(newTab.getCoords().getAssociatedSaveName(), newTab);
   }

   /** calls requestFocusToCurrentTwoDScrollPane on the open tab if there is one */
   public void requestFocusToCurrentTwoDScrollPane() {
      TwoDScrollPane selected = getCurrentTab();
      if (selected != null) selected.requestFocusToCurrentTwoDScrollPane();
   }

   /** Gets the current tab or null if there are no tabs */
   public TwoDScrollPane getCurrentTab() {
      Component selected = tabbedPane.getSelectedComponent();
      if (selected != null && selected instanceof TwoDScrollPane) {
         return (TwoDScrollPane) selected;
      } else return null;
   }

   private void windowInit() {
      window.setJMenuBar(frontEndMenu);

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

      // allow the 3d to be resizable and center the divider
      main.viewport3D.getCanvas().setMinimumSize(new Dimension(0,0));
   }

   public void display() {
      window.setLocationRelativeTo(null);
      window.setVisible(true);
   }

   /** Returns an image for use as the icon or null if it failed somehow */
   public Image getIcon(String location) {
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

      tabbedPane.setPreferredSize(new Dimension(400,180));
      main.viewport3D.getCanvas().setPreferredSize(new Dimension(400,180));
      main.objectBrowser.getPane().setPreferredSize(new Dimension(160,180));

      c = buildGBC(0, 0, 0.5, 0.0, topCenterAnchor, top_left_right);
      pane.add(main.designButtons.getPane(), c);

      c = buildGBC(1, 0, 0.0, 0.0, topCenterAnchor, top_right);
      pane.add(main.objectButtons.getPane(), c);

      c = buildGBC(0, 1, 0.5, 0.5, leftAnchor, top_left_bottom_right);
      c.fill = GridBagConstraints.BOTH;
      pane.add(TwoDandThreeD, c);

      c = buildGBC(1, 1, 0.07, 0.5, topLeftAnchor, top_bottom_right);
      c.fill = GridBagConstraints.BOTH;
      pane.add(main.objectBrowser.getPane(), c);
   }

   public File userChoiceOfOpenFile() {
      JFileChooser fc = new JFileChooser();
      fc.setDialogType(JFileChooser.OPEN_DIALOG);
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

      if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
         File file = fc.getSelectedFile();
         if (!file.exists()) {
            JOptionPane.showMessageDialog(window, "The Selected File Doesn't Ex"
               + "ist.\n\nPlease Try Again", "Doesn't Exist", JOptionPane.INFORMATION_MESSAGE);
            return userChoiceOfOpenFile();
         } else return file;

      } else {
         return null;
      }
   }

   public void newCoords() {

   }

   public void openCoords() {
      /*File toOpen = userChoiceOfOpenFile();
      try {
         if (toOpen != null) main.coordStore = FileManager.load(toOpen);
         main.viewport2D.repaint();
      } catch (Exception e) {
         // FAILED TO LOAD, NOTIFY USER
      }*/
   }

   public void currentCoordsSave() {

   }

   public void currentCoordsSaveAs() {

   }

   public void currentCoordsSaveCopyAs() {
      File saveFile = userChoiceOfSaveFile();
      if (saveFile != null) {
         try {
            TwoDScrollPane selected = getCurrentTab();
            if (selected != null) selected.getCoords().saveCopyAs(saveFile);
         
         } catch (IOException io) {
            // SAVE FAILED - NOTIFY USER
         }
      }
   }
   
   /** Returns true if the tab should be closed */
   private boolean quitTab(TwoDScrollPane tab) {
      if (!tab.getCoords().saveRequired()) return true;

      File saveFile = tab.getCoords().getAssociatedSaveFile();

      int choice = JOptionPane.showConfirmDialog(window,
         "Save file \"" + (saveFile == null ? tab.getCoords().getAssociatedSaveName() : tab.getCoords().getAssociatedSaveFileAsString())
         + "\" \u003F", "Save", JOptionPane.YES_NO_CANCEL_OPTION);

      if (choice == JOptionPane.YES_OPTION) {
         if (saveFile == null) { // user hasn't saved this coords yet
            if ((saveFile = userChoiceOfSaveFile()) == null) return false; // user doesn't want to exit
            try {
               tab.getCoords().saveAs(saveFile);
            } catch (IOException e) {
               return false; // UNABLE TO SAVE, ASK THE USER TO TRY AGAIN
            }
            return true;

         } else {
            try {
               tab.getCoords().save();
            } catch (IOException e) {
               return false; // UNABLE TO SAVE, ASK THE USER TO TRY AGAIN
            }
            return true;
         }

      } else if (choice == JOptionPane.NO_OPTION) return true; // don't save

      else return false; // user doesn't actually want to exit (pressed cancel)
   }

   /** If a save is needed, asks the user to confirm. returns true if the program
    *  should exit */
   private boolean quit() {
      while (tabbedPane.getTabCount() > 0) {
         TwoDScrollPane tab = getCurrentTab();
         if (quitTab(tab)) tabbedPane.remove(tab);
         else return false; // the user cancelled the quit operation on a tab
      }

      return true;
   }

   public File userChoiceOfSaveFile() {
      JFileChooser fc = new JFileChooser();
      fc.setDialogType(JFileChooser.SAVE_DIALOG);
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      
      if (fc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
         File file = fc.getSelectedFile();
         if (file.exists()) {
            int choice = JOptionPane.showConfirmDialog(window,
            "File Already Exists, Overwrite\u003F", "Overwrite\u003F", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.NO_OPTION) return userChoiceOfSaveFile();
         }
         return file;

      } else {
         return null;
      }
   }

   /** Sets the cursor across the whole JPanel */
   public void setWindowCursor(Cursor cursor) {
      window.setCursor(cursor);
   }

//! WINDOWLISTENER
   /** Invoked when the user attempts to close the window from the window's system menu. */
   public void windowClosing(WindowEvent e) {
      if (quit()) {
         window.dispose();
         main.viewport3D.shutdown3D();
         //System.exit(0); // if JME thread is still running, (for now) this stops it
      }
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



/** Creates the menu that appears at the top of the main window */
class FrontEndMenu extends JMenuBar implements ActionListener {

   private FrontEnd frontEnd;
   private JMenuItem create, open, save, saveAs, saveCopyAs, helpContents, undo, fullScreen, tweaker;

   FrontEndMenu(FrontEnd frontEnd) {
      this.frontEnd = frontEnd;

      addFileMenu();
      create.addActionListener(this);
      open.addActionListener(this);
      save.addActionListener(this);
      saveAs.addActionListener(this);
      saveCopyAs.addActionListener(this);

      addEditMenu();
      undo.addActionListener(this);

      addViewMenu();
      fullScreen.addActionListener(this);

      addCustomisationMenu();
      tweaker.addActionListener(this);

      addHelpMenu();
      helpContents.addActionListener(this);
   }

   private void addFileMenu() {
      JMenu menu = new JMenu("File");
      menu.setMnemonic(KeyEvent.VK_F);
      menu.getAccessibleContext().setAccessibleDescription("File option menu");

      create = new JMenuItem("New", KeyEvent.VK_N);
      create.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
      create.getAccessibleContext().setAccessibleDescription("Create a blank file");
      menu.add(create);

      open = new JMenuItem("Open", KeyEvent.VK_O);
      open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
      open.getAccessibleContext().setAccessibleDescription("Open a file");
      menu.add(open);

      save = new JMenuItem("Save", KeyEvent.VK_S);
      save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
      save.getAccessibleContext().setAccessibleDescription("Save any changes");
      menu.add(save);

      saveAs = new JMenuItem("Save As", KeyEvent.VK_E);
      saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
      saveAs.getAccessibleContext().setAccessibleDescription("Save as a new file");
      menu.add(saveAs);

      saveCopyAs = new JMenuItem("Save Copy As", KeyEvent.VK_C);
      saveCopyAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
      saveCopyAs.getAccessibleContext().setAccessibleDescription("Save a copy of this file");
      menu.add(saveCopyAs);

      this.add(menu);
   }

   private void addEditMenu() {
      JMenu menu;

      menu = new JMenu("Edit");
      menu.setMnemonic(KeyEvent.VK_E);
      menu.getAccessibleContext().setAccessibleDescription("Edit option menu");

      undo = new JMenuItem("Undo", KeyEvent.VK_Z);
      undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
      undo.getAccessibleContext().setAccessibleDescription("Undo the last action");
      menu.add(undo);

      this.add(menu);
   }

   private void addViewMenu() {
      JMenu menu;

      menu = new JMenu("View");
      menu.setMnemonic(KeyEvent.VK_V);
      menu.getAccessibleContext().setAccessibleDescription("View option menu");

      fullScreen = new JMenuItem("Full Screen", KeyEvent.VK_F11);
      fullScreen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
      fullScreen.getAccessibleContext().setAccessibleDescription("Display window in full screen");
      menu.add(fullScreen);

      this.add(menu);
   }

   private void addCustomisationMenu() {
      JMenu menu;

      menu = new JMenu("Customisation");
      menu.setMnemonic(KeyEvent.VK_C);
      menu.getAccessibleContext().setAccessibleDescription("Customisation option menu");

      tweaker = new JMenuItem("Model Tweaker", KeyEvent.VK_T);
      tweaker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
      tweaker.getAccessibleContext().setAccessibleDescription("Show the model tweaker");
      menu.add(tweaker);

      this.add(menu);
   }

   private void addHelpMenu() {
      JMenu menu;

      menu = new JMenu("Help");
      menu.setMnemonic(KeyEvent.VK_H);
      menu.getAccessibleContext().setAccessibleDescription("Help option menu");

      helpContents = new JMenuItem("Help Contents", KeyEvent.VK_H);
      helpContents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
      helpContents.getAccessibleContext().setAccessibleDescription("Show Help Contents");
      menu.add(helpContents);

      this.add(menu);
   }

   /** Invoked when an action occurs. */
   public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();

      if (source == create) {
         frontEnd.newCoords();
      } else if (source == open) {
         frontEnd.openCoords();
      } else if (source == save) {
         frontEnd.currentCoordsSave();
      } else if (source == saveAs) {
         frontEnd.currentCoordsSaveAs();
      } else if (source == saveCopyAs) {
         frontEnd.currentCoordsSaveCopyAs();
      } else if (source == helpContents) {
         System.out.println("Help Contents...");

      } else if (source == undo) {
         System.out.println("Undo...");

      } else if (source == fullScreen) {
         System.out.println("Full Screen...");

      } else if (source == tweaker) {
         System.out.println("Model Tweaker...");

      } else {
         Main.showFatalExceptionTraceWindow(
                 new Exception("BUG: Action ocurred with unexpected source (" + e.getSource().toString() + ")"));
      }
   }
}
