
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

/** Holds the buttons for tools and features related to the 2D pane */
public class DesignButtons implements ActionListener {
   public static final String IMG_DIR = "img/designbuttons/";

   private FrontEnd frontEnd;
   private JPanel pane;
   private JButton selectTool, lineTool, curveTool, currentTool,tweaker;
   private JSlider zoomTool;
   private Cursor selectCursor, lineCursor, curveCursor;
   private JToggleButton gridTool, dayToggle;

   /** Initialises the private variables as usual */
   DesignButtons(FrontEnd frontEnd) {
      this.frontEnd = frontEnd;
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
      selectCursor = new Cursor(Cursor.DEFAULT_CURSOR);
      lineCursor = new Cursor(Cursor.DEFAULT_CURSOR);
      curveCursor = new Cursor(Cursor.DEFAULT_CURSOR);
   }

   /** Initialises the private button variables */
   private void initButtons() {
      selectTool = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "hand.png")));
      selectTool.addActionListener(this);

      lineTool = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "line.png")));
      lineTool.addActionListener(this);

      curveTool = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "cline.png")));
      curveTool.addActionListener(this);

      gridTool = new JToggleButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "grid.png")));
      gridTool.addActionListener(this);
      gridTool.setSelected(true);
      
      dayToggle = new JToggleButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "daynight.png")));
      dayToggle.addActionListener(this);
      dayToggle.setSelected(true);
      
      tweaker = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "pas.png")));
      tweaker.addActionListener(this);

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

      pane = new JPanel(new GridBagLayout());
      pane.setBorder(BorderFactory.createTitledBorder("Design Buttons"));

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

      c = FrontEnd.buildGBC(6, 1, 0.5, 0.5, topCenterAnchor, none);
      pane.add(zoomTool, c);

      c = FrontEnd.buildGBC(6, 0, 0.5, 0.5, bottomCenterAnchor, none);
      pane.add(new JLabel("Zoom"), c);
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
         frontEnd.setWindowCursor(curveCursor);
         currentTool = curveTool;
         reCalcButtonStates();

      } else if (gridTool == source) {
         // toggle grid showing
    	  

      } else if (dayToggle == source) {
         Main.viewport3D.toggleDay();
         
      } else if (tweaker == source) {
         Main.viewport3D.shutdown3D();
         Tweaker hello = new Tweaker(frontEnd.getCurrentTab().gettd(), frontEnd.main);
      } else {
         Main.showFatalExceptionTraceWindow(
                 new Exception("BUG: Action ocurred with unexpected source (" + e.getSource().toString() + ")"));
      }

      frontEnd.requestFocusToCurrentTwoDScrollPane();
   }
}
