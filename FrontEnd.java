import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

/** Creates and displays the whole main window and adds the various different
 *  JPanels to it.
 *  
 */
public class FrontEnd implements WindowListener, ChangeListener {
   public static final String ICON_LOCATION = "img/frontend/icon.png";
   public static final String WINDOW_TITLE = "ArchiTECH";
   private final Color back = new Color(74,74,74);
   private final Color high = new Color(9,77,154);
   private final Color divcol = new Color(74,74,74);
   private final JFrame window = new JFrame(WINDOW_TITLE);
   private JSplitPane horizsplit;
   private JPanel topbuttons;
   JPanel left;
   TWPane preview;
   private JSplitPane TwoDandThreeD = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
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
     
   public HandlerVertexSelect gethvs(){
	   return getCurrentTab().gethvs();
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
      preview = new TWPane(new Dimension(TwoDandThreeD.getSize().width-10,TwoDandThreeD.getSize().height));

      try {
         addTab(new File("gammaroom.atech"));
         stateChanged(new ChangeEvent(tabbedPane));
      } catch (Exception e) {
         // FAILED TO LOAD, NOTIFY USER
         addTab("New File");
      }

      tabbedPane.addChangeListener(this);
   }
   
   public DesignButtons getDButtons(){
	   return designButtons;
   }
   
   public void revert(){
	   designButtons.changetonormal();
	   main.objectBrowser.changetonormal();
	   reinitTwoDAndThreeD();
   }  
   
   public void changetw(){
	   this.setWindowCursor(new Cursor(Cursor.WAIT_CURSOR));
	   TwoDandThreeD.setBottomComponent(new JPanel());
	   main.viewport3D.shutdown3D();
	   try{Thread.sleep(2000);}catch(InterruptedException e){}
	   preview.make3d(new Dimension(TwoDandThreeD.getSize().width-10,TwoDandThreeD.getSize().height-10));
       TwoDandThreeD.setTopComponent(preview.getCanvas());
	   TwoDandThreeD.setDividerLocation(TwoDandThreeD.getMaximumDividerLocation());
       TwoDandThreeD.revalidate();
	   designButtons.changetotw(main.objectBrowser);
	   main.objectBrowser.changetotw(preview);
	   this.setWindowCursor(Cursor.getDefaultCursor());
   }

   /** Initialises the twoDandThreeD variable */
   private void initTwoDAndThreeD() {
      TwoDandThreeD.setTopComponent(tabbedPane);
      TwoDandThreeD.setDividerSize(11);
      TwoDandThreeD.setBorder(null);
	  TwoDandThreeD.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, high));
      TwoDandThreeD.setResizeWeight(0.45);
      Image img = getImage(this,"img/frontend/eye.png");
      Point centre = new Point(0,0);
      Cursor eye = Toolkit.getDefaultToolkit().createCustomCursor(img, centre, "Add");  
      main.viewport3D.getCanvas().setCursor(eye);
      TwoDandThreeD.setBottomComponent(main.viewport3D.getCanvas());
	  Container div = (BasicSplitPaneDivider) TwoDandThreeD.getComponent(0);
	  div.setBackground(divcol);
   }

   /** Re-initialises the twoDandThreeD variable when we switch back to the main app from the tweaker */
   public void reinitTwoDAndThreeD() {
	      TwoDandThreeD.setTopComponent(tabbedPane);
	      Image img = getImage(this,"img/frontend/eye.png");
	      Point centre = new Point(0,0);
	      Cursor eye = Toolkit.getDefaultToolkit().createCustomCursor(img, centre, "Add");  
	      main.viewport3D.getCanvas().setCursor(eye);
		  preview.shutdown3D();
		   try{Thread.sleep(2000);}catch(InterruptedException e){}
		  main.viewport3D.remake3D();
	      TwoDandThreeD.setBottomComponent(main.viewport3D.getCanvas());
		  left.remove(topbuttons);
		  left.revalidate();
		  left.add(designButtons.getPane());
		  designButtons.getSlider().setMinimumSize( new Dimension(200, 50) );
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
   public TwoDScrollPane getCurrentTab() {
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
      int topLeftAnchor = GridBagConstraints.NORTHWEST;
      /*int rightAnchor = GridBagConstraints.LINE_END;
      int centerAnchor = GridBagConstraints.CENTER;
      int topCenterAnchor = GridBagConstraints.NORTH;
      int topRightAnchor = GridBagConstraints.NORTHEAST;

      Insets none = new Insets(0,0,0,0);
      Insets top_left_right = new Insets(10,10,0,10);
      Insets top_left_bottom_right = new Insets(10,10,10,10);
      Insets top_right = new Insets(10,0,0,10);
      Insets top_bottom_right = new Insets(10,0,10,10);*/

      pane.setLayout(new BorderLayout());
      GridBagConstraints c;      

      tabbedPane.setPreferredSize(new Dimension(400,180));
      main.viewport3D.getCanvas().setPreferredSize(new Dimension(400,180));
      main.objectBrowser.getSplit().setPreferredSize(new Dimension(160,180));
      
      left = new JPanel(new GridBagLayout()){
    	  public void paintComponent (Graphics g)
    		{
    			super.paintComponent(g);
    			Graphics2D g2d = (Graphics2D) g;
    			//Image prev = getImage(this,"img/frontend/faded.png")
    			//prev = prev.getScaledInstance( 128, 128,  java.awt.Image.SCALE_SMOOTH ) ; 
    			g2d.drawImage(getImage(this,"img/frontend/faded.png"),0,0,null);
    		}
      };
      JPanel right = new JPanel(new GridBagLayout());
      
      //build left
      c = buildGBC(0, 0, 0.5, 0.0, GridBagConstraints.NORTH, new Insets(0,0,10,0));
      topbuttons = new JPanel();
      topbuttons.setOpaque(false);
      topbuttons.add(designButtons.getPane());
      left.add(topbuttons, c);      
      c = buildGBC(0, 1, 0.5, 0.5, leftAnchor, new Insets(0,5,5,5));
      c.fill = GridBagConstraints.BOTH;
      left.add(TwoDandThreeD, c);
      left.setBackground(back);     
      
      //build right
      c = buildGBC(1, 1, 0.07, 0.5, topLeftAnchor, new Insets(5,5,5,5));
      c.fill = GridBagConstraints.BOTH;
      right.add(main.objectBrowser.getSplit(), c);
      right.setBackground(back);
      right.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, high));
      
      //add to horiz split pane
	  horizsplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, left, right);
	  Container div = (BasicSplitPaneDivider) horizsplit.getComponent(2);
	  div.setBackground(divcol);
	  Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
	  horizsplit.setDividerLocation(scr.width/2+scr.width/4);	 
	  horizsplit.setDividerSize(11);
      pane.add(horizsplit,BorderLayout.CENTER);
   }

   /** Asks the user to choose a file to open */
   private File userChoiceOfOpenFile() {
      JFileChooser fc = new JFileChooser(getClass().getResource("").getPath());
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
         System.exit(0); // if JME thread is still running, this makes sure
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