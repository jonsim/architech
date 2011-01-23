
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Holds the buttons for tools and features related to the 2D pane */
public class DesignButtons implements ActionListener {
   private FrontEnd frontEnd;
   private JPanel pane;
   private JButton selectTool, lineTool, curveTool, currentTool;
   private Cursor selectCursor, lineCursor, curveCursor;
   private JToggleButton gridTool, snapTool;

   /** Initialises the private variables as usual */
   DesignButtons(FrontEnd frontEnd) {
      this.frontEnd = frontEnd;
      initCursors();
      initButtons();
      initPane();
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
      selectTool = new JButton("Sel");
      lineTool = new JButton("Lne");
      curveTool = new JButton("Crv");
      gridTool = new JToggleButton("Grd");
      snapTool = new JToggleButton("Snp");

      selectTool.addActionListener(this);
      lineTool.addActionListener(this);
      curveTool.addActionListener(this);
      gridTool.addActionListener(this);
      snapTool.addActionListener(this);

      gridTool.setSelected(true);
      snapTool.setSelected(true);

      currentTool = lineTool;
      reCalcButtonStates();
   }

   /** Initialises the private pane variable, adds the buttons to it */
   private void initPane() {
      int leftAnchor = GridBagConstraints.LINE_START;
      int rightAnchor = GridBagConstraints.LINE_END;
      int centerAnchor = GridBagConstraints.CENTER;
      int topLeftAnchor = GridBagConstraints.NORTHWEST;
      int topRightAnchor = GridBagConstraints.NORTHEAST;

      Insets top_left_right = new Insets(10, 10, 0, 10);
      Insets top_left_bottom_right = new Insets(10, 10, 10, 10);
      Insets top_right = new Insets(10, 0, 0, 10);
      Insets top_bottom_right = new Insets(10, 0, 10, 10);
      Insets right = new Insets(0, 0, 0, 10);
      Insets bottom = new Insets(0, 0, 5, 0);
      Insets none = new Insets(0, 0, 0, 0);

      pane = new JPanel(new GridBagLayout());
      pane.setBorder(BorderFactory.createTitledBorder("Design Buttons"));

      GridBagConstraints c;

      c = FrontEnd.buildGBC(0, 0, 0.5, 0.5, centerAnchor, right);
      pane.add(selectTool, c);

      c = FrontEnd.buildGBC(1, 0, 0.5, 0.5, centerAnchor, right);
      pane.add(lineTool, c);

      c = FrontEnd.buildGBC(2, 0, 0.5, 0.5, centerAnchor, right);
      pane.add(curveTool, c);

      c = FrontEnd.buildGBC(3, 0, 0.5, 0.5, centerAnchor, right);
      pane.add(gridTool, c);

      c = FrontEnd.buildGBC(4, 0, 0.5, 0.5, centerAnchor, none);
      pane.add(snapTool, c);

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

      } else if (snapTool == source) {
         // toggle snap
         
      } else {
         Main.showFatalExceptionTraceWindow(
                 new Exception("BUG: Action ocurred with unexpected source (" + e.getSource().toString() + ")"));
      }

      frontEnd.requestFocusToCurrentTwoDScrollPane();
   }
}
