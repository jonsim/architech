
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.MenuItemUI;

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

/** Holds the buttons for tools and features related to the 2D pane */
public class DesignButtons implements ActionListener {
   public static final String IMG_DIR = "img/designbuttons/";

   public final Viewport3D viewport3D;
   private final FrontEnd frontEnd;
   private JPanel pane;
   private JButton selectTool, lineTool, curveTool,  currentTool, dayToggle, tweaker;
   private JSlider zoomTool;
   private Cursor selectCursor, lineCursor;
   private JToggleButton gridTool;
   private final Color back = new Color(74,74,74);

   /** Initialises the private variables as usual */
   DesignButtons(FrontEnd frontEnd, Viewport3D viewport3D) {
      if (frontEnd == null || viewport3D == null) {
         throw new IllegalArgumentException("null parameter");
      }
      this.frontEnd = frontEnd;
      this.viewport3D = viewport3D;
      initCursors();
      initButtons();
      initPane();
   }

   /** Returns the zoom slider object */
   public JSlider getSlider() {
      return zoomTool;
   }

   /** Returns true if the line tool is selected */
   public boolean isLineTool() {
      return lineTool == currentTool;
   }

   /** Returns true if the grid toggle is currently on */
   public boolean isGridOn() {
      return gridTool.isSelected();
   }

   /** Returns true if the select tool is selected */
   public boolean isSelectTool() {
      return selectTool == currentTool;
   }

   /** Returns true if the curve tool is selected */
   public boolean isCurveTool() {
      return curveTool == currentTool;
   }

   /** Initialises the private cursor variables */
   private void initCursors() {
      selectCursor = new Cursor(Cursor.HAND_CURSOR);
      lineCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
   }

   /** Initialises the private button variables */
   private void initButtons() {
	  Insets margins = new Insets(0,0,0,0);
      selectTool = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "hand.png")));
      selectTool.addActionListener(this);
      selectTool.setMargin(margins);
      selectTool.setToolTipText("Use the select tool to select and move vertices/edges");

      lineTool = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "line.png")));
      lineTool.addActionListener(this);
      lineTool.setMargin(margins);
      lineTool.setToolTipText("Use the line tool to place vertices and drag to draw walls");

      curveTool = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "cline.png")));
      curveTool.addActionListener(this);
      curveTool.setMargin(margins);
      curveTool.setToolTipText("Use the curve tool to draw curved walls");

      gridTool = new JToggleButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "grid.png")));
      gridTool.addActionListener(this);
      gridTool.setSelected(true);
      gridTool.setMargin(margins);
      gridTool.setToolTipText("Turns the grid on/off");
      
      dayToggle = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "daynight.png")));
      dayToggle.addActionListener(this);
      dayToggle.setMargin(margins);
      dayToggle.setToolTipText("Switch between night and day");
      
      tweaker = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "tweak.png")));
      tweaker.addActionListener(this);
      tweaker.setMargin(margins);
      tweaker.setToolTipText("Open the tweaker in order to edit furniture");

      currentTool = lineTool;
      zoomTool = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);
      initZoomTool();
      reCalcButtonStates();
   }

   /** Initialises the zoomTool slider */
   private void initZoomTool() {
      zoomTool.setMajorTickSpacing(5);
      zoomTool.setMinorTickSpacing(1);
      zoomTool.setPaintTicks(true);
      zoomTool.setSnapToTicks(true);

      Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
      labelTable.put( 0, new JLabel("0%") );
      labelTable.put( 10, new JLabel("100%") );
      labelTable.put( 20, new JLabel("200%") );
      zoomTool.setLabelTable( labelTable );
      zoomTool.setPaintLabels(true);

      Font font = new Font("Serif", Font.ITALIC, 15);
      zoomTool.setFont(font);
   }

   /** Initialises the private pane variable, adds the buttons to it */
   private void initPane() {
      //int leftAnchor = GridBagConstraints.LINE_START;
      //int rightAnchor = GridBagConstraints.LINE_END;
      //int centerAnchor = GridBagConstraints.CENTER;
      int topCenterAnchor = GridBagConstraints.NORTH;
      //int topLeftAnchor = GridBagConstraints.NORTHWEST;
      //int topRightAnchor = GridBagConstraints.NORTHEAST;
      int bottomCenterAnchor = GridBagConstraints.SOUTH;

      //Insets top_left_right = new Insets(10, 10, 0, 10);
      //Insets top_left_bottom_right = new Insets(10, 10, 10, 10);
      //Insets top_right = new Insets(10, 0, 0, 10);
      //Insets top_bottom_right = new Insets(10, 0, 10, 10);
      Insets right = new Insets(0, 0, 0, 10);
      //Insets bottom = new Insets(0, 0, 5, 0);
      Insets none = new Insets(0, 0, 0, 0);

      //pane = new JPanel(new GridBagLayout());
      pane = new JPanel(new GridBagLayout());
      pane.setOpaque(false);
      /*JToolBar dbuttons = new JToolBar("Design buttons");
      dbuttons.setFloatable(false);
      dbuttons.addSeparator();
      dbuttons.add(selectTool);
      dbuttons.addSeparator();
      dbuttons.add(lineTool);
      dbuttons.addSeparator();
      dbuttons.add(curveTool);
      dbuttons.addSeparator();
      dbuttons.add(gridTool);  
      dbuttons.addSeparator();
      dbuttons.add(zoomTool);
      dbuttons.addSeparator();
      dbuttons.add(dayToggle);
      dbuttons.addSeparator();
      dbuttons.add(tweaker); 
      dbuttons.addSeparator();*/
      pane.add(selectTool);
      //pane.add(dbuttons);
      
      //pane.setBorder(BorderFactory.createTitledBorder("Design Buttons"));

      GridBagConstraints c;

      c = FrontEnd.buildGBC(0, 1, 0.5, 0.5, topCenterAnchor, right);
      pane.add(selectTool, c);

      c = FrontEnd.buildGBC(1, 1, 0.5, 0.5, topCenterAnchor, right);
      pane.add(lineTool, c);

      c = FrontEnd.buildGBC(2, 1, 0.5, 0.5, topCenterAnchor, right);
      pane.add(curveTool, c);

      c = FrontEnd.buildGBC(3, 1, 0.5, 0.5, topCenterAnchor, right);
      pane.add(gridTool, c);
      
      c = FrontEnd.buildGBC(4, 1, 0.5, 0.5, topCenterAnchor, right);
      pane.add(dayToggle, c);
      
      c = FrontEnd.buildGBC(5, 1, 0.5, 0.5, topCenterAnchor, right);
      pane.add(tweaker, c);

      c = FrontEnd.buildGBC(6, 1, 0.5, 0.5, topCenterAnchor, right);
      pane.add(zoomTool, c);

      c = FrontEnd.buildGBC(6, 0, 0.5, 0.5, bottomCenterAnchor, right);
      pane.add(new JLabel("<html><font color='white'>Zoom"), c);
   }

   /** Returns the pane containing the buttons / GUI stuff */
   public JPanel getPane() {
      return pane;
   }

   /** If currentTool has changed then this will grey out the tool's button.
    *  Does not change anything to do with the toggle buttons */
   private void reCalcButtonStates() {
      selectTool.setEnabled(true);
      lineTool.setEnabled(true);
      curveTool.setEnabled(true);
      currentTool.setEnabled(false);
   }

   /** Whenever a button in this pane is pressed this method is called */
   public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();

      if (selectTool == source) {
         frontEnd.setWindowCursor(selectCursor);
         currentTool = selectTool;
         reCalcButtonStates();

      } else if (lineTool == source) {
         frontEnd.setWindowCursor(lineCursor);
         currentTool = lineTool;
         reCalcButtonStates();

      } else if (curveTool == source) {
         frontEnd.setWindowCursor(lineCursor);
         currentTool = curveTool;
         reCalcButtonStates();

      } else if (gridTool == source) {
         // toggle grid showing

      } else if (dayToggle == source) {
         viewport3D.toggleDay();

      } else if (tweaker == source) {
          viewport3D.shutdown3D();
    	  frontEnd.changetw();
         //frontEnd.getwindow().setVisible(false);
         //Tweaker hello = new Tweaker(frontEnd.main);
         //hello.setVisible(true);
      } else {
         Main.showFatalExceptionTraceWindow(
                 new Exception("BUG: Action ocurred with unexpected source (" + e.getSource().toString() + ")"));
      }

      frontEnd.requestFocusToCurrentTwoDScrollPane();
   }
}
