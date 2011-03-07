import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/** Creates and displays the whole main window and adds the various different
 *  JPanels to it.
 *  
 */
public class FrontEnd implements WindowListener, ChangeListener {
   public static final String ICON_LOCATION = "img/frontend/icon.png";
   public static final String WINDOW_TITLE = "ArchiTECH";

   private final JFrame window = new JFrame(WINDOW_TITLE);
   private final JSplitPane TwoDandThreeD = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
   private final JTabbedPane tabbedPane = new JTabbedPane() {
      @Override
      public void remove(Component tab) {
         super.remove(tab);
         if (tab instanceof TwoDScrollPane) {
            // unregister the listener and tell 3D to forget about the walls so
            // that the memory will be freed
            ((TwoDScrollPane) tab).getCoords().removeCoordsChangeListener(main.viewport3D);
            main.viewport3D.tabRemoved(((TwoDScrollPane) tab).getCoords());
         }
      }

      @Override
      public void remove(int index) {
         Component tab = this.getComponentAt(index);
         this.remove(tab);
      }
   };
   private FrontEndMenu frontEndMenu;
   private DesignButtons designButtons;
   public Main main;
   
   public void refreshtt(){
	   TwoDandThreeD.revalidate();
   }

   public JFrame getwindow(){
	   return window;
   }
   /** holds the displayable window and shows or hides sub windows
    * displays dialogs on request by other classes (i.e. crash dialog) */
   FrontEnd(Main main) {
      this.main = main;
      this.frontEndMenu = new FrontEndMenu(this);
      this.designButtons = new DesignButtons(this, main.viewport3D);

      initTwoDAndThreeD();
      initWindow();

      // allow the 3d to be resizable and center the divider
      main.viewport3D.getCanvas().setMinimumSize(new Dimension(0,0));

      try {
         addTab(new File("testSave.atech"));
         stateChanged(new ChangeEvent(tabbedPane));
      } catch (Exception e) {
         // FAILED TO LOAD, NOTIFY USER
         addTab("New File");
      }

      tabbedPane.addChangeListener(this);
   }

   /** Initialises the twoDandThreeD variable */
   public void initTwoDAndThreeD() {
      TwoDandThreeD.setTopComponent(tabbedPane);
      TwoDandThreeD.setBottomComponent(main.viewport3D.getCanvas());
      TwoDandThreeD.setOneTouchExpandable(true);
      TwoDandThreeD.setDividerSize(11);
      TwoDandThreeD.setBorder(null);
      TwoDandThreeD.setResizeWeight(0.45);
   }

   /** Called whenever the current tab state is changed in tabbedPane */
   public final void stateChanged(ChangeEvent e) {
      TwoDScrollPane currTab = getCurrentTab();
      try {
         main.viewport3D.tabChanged(currTab == null ? null : currTab.getCoords());
      } catch (Exception err) {
         /* Catch and ignore just in case something in viewport3D crashes. */
      }
   }

   /** Creates a new tab and registers viewport3D as a listener for it */
   private void addTab(File file) throws Exception {
      TwoDScrollPane newTab = new TwoDScrollPane(file, null, designButtons, main.viewport3D,main.objectBrowser);
      tabbedPane.addTab(newTab.getCoords().getAssociatedSaveName(), newTab);
      tabbedPane.setSelectedComponent(newTab);

      int i = tabbedPane.indexOfComponent(newTab);
      tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane));
   }

   /** Creates a new tab and registers viewport3D as a listener for it */
   private void addTab(String title) {
      try {
         TwoDScrollPane newTab = new TwoDScrollPane(null, title, designButtons, main.viewport3D,main.objectBrowser);
         tabbedPane.addTab(newTab.getCoords().getAssociatedSaveName(), newTab);
         tabbedPane.setSelectedComponent(newTab);

         int i = tabbedPane.indexOfComponent(newTab);
         tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane));
      } catch (Exception e) {
         System.err.println("Never Happen Case");
      }
   }

   /** calls requestFocusToCurrentTwoDScrollPane on the open tab if there is one */
   public void requestFocusToCurrentTwoDScrollPane() {
      TwoDScrollPane selected = getCurrentTab();
      if (selected != null) selected.requestFocusToPanel();
   }

   /** Gets the current tab or null if there are no tabs */
   private TwoDScrollPane getCurrentTab() {
      Component selected = tabbedPane.getSelectedComponent();
      if (selected != null && selected instanceof TwoDScrollPane) {
         return (TwoDScrollPane) selected;
      } else return null;
   }

   /** Returns the current tab's coords or null if there are no tabs */
   public Coords getCurrentCoords() {
      TwoDScrollPane tab = getCurrentTab();
      return tab == null ? null : tab.getCoords();
   }

   /** Initialises the window variable */
   private void initWindow() {
      window.setJMenuBar(frontEndMenu);
      
      window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      window.addWindowListener(this);

      Image icon = FrontEnd.getImage(this, ICON_LOCATION);
      if (icon != null) window.setIconImage(icon);

      // puts everything in the window at its default (fairly small) size
      addWindowComponents(window.getContentPane());

      window.setResizable(true);
      window.pack();

      // set minimum size to initialised size, before maximisation
      window.setMinimumSize(window.getSize());
      window.setExtendedState(JFrame.MAXIMIZED_BOTH);
   }

   /** Centers and sets the window visible */
   public void display() {
      window.setLocationRelativeTo(null);
      window.setVisible(true);
   }

   /** Adds all the things 2D, 3D, SQL etc. to the given pane */
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
      pane.add(designButtons.getPane(), c);

      c = buildGBC(1, 0, 0.0, 0.0, topCenterAnchor, top_right);
      pane.add(main.objectButtons.getPane(), c);

      c = buildGBC(0, 1, 0.5, 0.5, leftAnchor, top_left_bottom_right);
      c.fill = GridBagConstraints.BOTH;
      pane.add(TwoDandThreeD, c);

      c = buildGBC(1, 1, 0.07, 0.5, topLeftAnchor, top_bottom_right);
      c.fill = GridBagConstraints.BOTH;
      pane.add(main.objectBrowser.getPane(), c);
   }

   /** Asks the user to choose a file to open */
   private File userChoiceOfOpenFile() {
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

   /** Asks the user to choose a file to save to */
   private File userChoiceOfSaveFile() {
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

   /** Creates a blank panel and adds a tab with the new file */
   public void newCoords() {
      addTab("New File");
   }

   /** Asks the user to specify a file and then loads it into a new tab */
   public void openCoords() {
      File toOpen = userChoiceOfOpenFile();
      if (toOpen == null) return;

      try {
         addTab(toOpen);
      } catch (Exception e) {
         // FILE FAILED TO LOAD, NOTIFY USER
      }
   }

   /** Saves the current file (if it has an associated save file) */
   public void currentCoordsSave() {
      TwoDScrollPane tab = getCurrentTab();
      if (tab == null) return;

      try {
         if (tab.getCoords().getAssociatedSaveFile() == null) currentCoordsSaveAs();
         else tab.getCoords().save();
      } catch (IOException e) {
         // SAVE FAILED, NOTIFY USER
      }
   }

   /** Prompts the user to choose a file to save as, then saves it */
   public void currentCoordsSaveAs() {
      TwoDScrollPane tab = getCurrentTab();
      if (tab == null) return;

      File saveFile = userChoiceOfSaveFile();
      if (saveFile == null) return;

      try {
         tab.getCoords().saveAs(saveFile);
         tabbedPane.setTitleAt(tabbedPane.indexOfComponent(tab), tab.getCoords().getAssociatedSaveName());
      } catch (IOException e) {
         // SAVE FAILED, NOTIFY USER
      }
   }

   /** Prompts the user to choose a file to save as, then saves it as a copy */
   public void currentCoordsSaveCopyAs() {
      TwoDScrollPane tab = getCurrentTab();
      if (tab == null) return;

      File saveFile = userChoiceOfSaveFile();
      if (saveFile == null) return;

      try {
         tab.getCoords().saveCopyAs(saveFile);
      } catch (IOException e) {
         // SAVE FAILED, NOTIFY USER
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

   /** Sets the cursor across the whole JPanel */
   public void setWindowCursor(Cursor cursor) {
      window.setCursor(cursor);
   }

   //-WINDOW-LISTENER-----------------------------------------------------------

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

   //-STATIC-METHODS------------------------------------------------------------

   /** Returns an image for use as the icon or null if it failed somehow. It will
    *  look, to begin with, in the same directory as the class you give it. For us
    *  giving it "this" will probably be what we need */
   public static Image getImage(Object classToGetResourceFrom, String location) {
      if (classToGetResourceFrom == null) throw new IllegalArgumentException("null class");

      URL iconResource = classToGetResourceFrom.getClass().getResource(location);
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
}
