import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.plaf.metal.MetalButtonUI;

/** Holds the buttons for tools and features related to the 2D pane */
public class DesignButtons implements ActionListener {
   public static final String IMG_DIR = "img/designbuttons/";
   public static final String TWIMG_DIR = "img/twbuttons/";

   public final Viewport3D viewport3D;
   private final FrontEnd frontEnd;
   private ObjectBrowser objectBrowser;
   private JPanel pane,fillcom;
   private JButton selectTool, lineTool, curveTool,currentTool, dayToggle, tweaker, fillTool,rfloor;
   private JButton deselectTool,dropD,dropT;
   private JButton TwUp,TwDown,TwLeft,TwRight,TwFor,TwBack,TwCol,TwTex,TwPlus,TwMinus,TwRotl,TwRotr;
   private JButton TwPx,TwMx,TwPy,TwMy,TwPz,TwMz;
   private JSlider zoomTool;
   private Cursor selectCursor, lineCursor,curveCursor;
   private JToggleButton gridTool;
   //private final Color back = new Color(74,74,74);
   private TWPalette twPalette;
   private boolean palonshow = false;
   private TWTex twTex;
   private boolean texonshow = false;
   private boolean tweakmode = false;

   /** Initialises the private variables as usual */
   DesignButtons(FrontEnd frontEnd, Viewport3D viewport3D) {
  	if (frontEnd == null || viewport3D == null) {
     	throw new IllegalArgumentException("null parameter");
  	}
  	this.frontEnd = frontEnd;
  	this.viewport3D = viewport3D;
  	initCursors();
  	initButtons();
  	pane = new JPanel(new GridBagLayout());
  	pane.setOpaque(false);
  	initPane();
  	objectBrowser=null;
  	twPalette = new TWPalette(TwLeft);
  	twTex = new TWTex(TwLeft);
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
  	curveCursor = new Cursor(Cursor.MOVE_CURSOR);
   }

   /** Initialises the private button variables */
   private void initButtons() {
      Insets margins = new Insets(0,0,0,0);
  	selectTool = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "hand.png")));
  	selectTool.addActionListener(this);
  	selectTool.setMargin(margins);
  	selectTool.setUI(new MetalButtonUI());
  	selectTool.setToolTipText("Use the select tool to select and move vertices/edges");
 	 
  	deselectTool = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "deselect.png")));
  	deselectTool.addActionListener(this);
  	deselectTool.setMargin(margins);
  	deselectTool.setUI(new MetalButtonUI());
  	deselectTool.setToolTipText("Deselect all selected walls and corners");

  	lineTool = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "line.png")));
  	lineTool.addActionListener(this);
  	lineTool.setMargin(margins);
  	lineTool.setUI(new MetalButtonUI());
  	lineTool.setToolTipText("Use the line tool to place vertices and drag to draw walls");

  	curveTool = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "cline.png")));
  	curveTool.addActionListener(this);
  	curveTool.setMargin(margins);
  	curveTool.setUI(new MetalButtonUI());
  	curveTool.setToolTipText("Use the curve tool to draw curved walls");

  	gridTool = new JToggleButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "grid.png")));
  	gridTool.addActionListener(this);
  	gridTool.setSelected(true);
  	gridTool.setMargin(margins);
  	gridTool.setUI(new MetalButtonUI());;
  	gridTool.setToolTipText("Turns the grid on/off");
 	 
  	dayToggle = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "daynight.png")));
  	dayToggle.addActionListener(this);
  	dayToggle.setMargin(margins);
  	dayToggle.setUI(new MetalButtonUI());
  	dayToggle.setToolTipText("Switch between night and day");
 	 
 	 
  	fillTool = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "fill.png")));
  	fillTool.addActionListener(this);
  	fillTool.setMargin(margins);
  	fillTool.setUI(new MetalButtonUI());
  	fillTool.setToolTipText("Fill the currently selected room");
 	 
  	dropD = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "dropd.png")));
  	dropD.setPreferredSize(new Dimension(44,7));
  	dropD.addActionListener(this);
  	dropD.setMargin(margins);
  	dropD.setUI(new MetalButtonUI());
  	dropD.setToolTipText("Select a fill colour");
  	fillcom = new JPanel(new GridBagLayout());
  	GridBagConstraints c = FrontEnd.buildGBC(0, 0, 0.5, 0.5, GridBagConstraints.NORTH, new Insets(0,0,0,0));
  	fillcom.add(fillTool,c);
  	c = FrontEnd.buildGBC(0, 1, 0.5, 0.5, GridBagConstraints.NORTH, new Insets(0,0,0,0));
  	fillcom.add(dropD,c);
 	 
  	rfloor = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "rfloor.png")));
  	rfloor.addActionListener(this);
  	rfloor.setMargin(margins);
  	rfloor.setUI(new MetalButtonUI());
  	rfloor.setToolTipText("Update the 3D view with the current 2D Floor filling");
 	 
  	tweaker = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "tweak.png")));
  	tweaker.addActionListener(this);
  	tweaker.setMargin(margins);
  	tweaker.setUI(new MetalButtonUI());
  	tweaker.setToolTipText("Open the tweaker in order to edit furniture");

  	currentTool = lineTool;
  	zoomTool = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);
  	initZoomTool(); 	 
  	reCalcButtonStates();
   }
   
   public void changetotw(ObjectBrowser objectBrowser){
   	this.objectBrowser = objectBrowser;
   	pane.removeAll();
   	pane.revalidate();
   	initTwButtons();
   	initTwPane();
   	tweakmode = true;
   }
   
   public void changetonormal(){
   	pane.removeAll();
   	pane.revalidate();
   	pane.repaint();
   	initButtons();
   	initPane();
   	pane.revalidate();
   	pane.repaint();
   	tweakmode = false;
   }
   
   private void initTwButtons() {
    Insets margins = new Insets(-1,-1,-1,-1);
  	TwLeft = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "left.png")));
  	TwLeft.addActionListener(this);
  	TwLeft.setActionCommand("!r");
  	TwLeft.setMargin(margins);
  	TwLeft.setUI(new MetalButtonUI());
  	TwLeft.setToolTipText("Move the current object left"); //to be added
  	TwRight = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "right.png")));
  	TwRight.addActionListener(this);
  	TwRight.setActionCommand("!l");
  	TwRight.setMargin(margins);
  	TwRight.setUI(new MetalButtonUI());
  	TwRight.setToolTipText("Move the current object right"); //to be added
  	TwFor = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "forward.png")));
  	TwFor.addActionListener(this);
  	TwFor.setActionCommand("!f");
  	TwFor.setMargin(margins);
  	TwFor.setUI(new MetalButtonUI());
  	TwFor.setToolTipText("Move the current object forward"); //to be added
  	TwBack = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "back.png")));
  	TwBack.addActionListener(this);
  	TwBack.setActionCommand("!b");
  	TwBack.setMargin(margins);
  	TwBack.setUI(new MetalButtonUI());
  	TwBack.setToolTipText("Move the current object back"); //to be added
  	TwUp = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "up.png")));
  	TwUp.addActionListener(this);
  	TwUp.setActionCommand("!u");
  	TwUp.setMargin(margins);
  	TwUp.setUI(new MetalButtonUI());
  	TwUp.setToolTipText("Move the current object up"); //to be added
  	TwDown = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "down.png")));
  	TwDown.addActionListener(this);
  	TwDown.setActionCommand("!d");
  	TwDown.setMargin(margins);
  	TwDown.setUI(new MetalButtonUI());
  	TwDown.setToolTipText("Move the current object down"); //to be added
  	TwRotl = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "rotl.png")));
  	TwRotl.addActionListener(this);
  	TwRotl.setActionCommand("!<");
  	TwRotl.setMargin(margins);
  	TwRotl.setUI(new MetalButtonUI());
  	TwRotl.setToolTipText("Rotate the current object left"); //to be added
  	TwRotr = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "rotr.png")));
  	TwRotr.addActionListener(this);
  	TwRotr.setActionCommand("!>");
  	TwRotr.setMargin(margins);
  	TwRotr.setUI(new MetalButtonUI());
  	TwRotr.setToolTipText("Rotate the current object right"); //to be added
  	TwPlus = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "plus.png")));
  	TwPlus.addActionListener(this);
  	TwPlus.setActionCommand("!+");
  	TwPlus.setMargin(margins);
  	TwPlus.setUI(new MetalButtonUI());
  	TwPlus.setToolTipText("Increase the size of the current object uniformly"); //to be added
  	TwMinus = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "minus.png")));
  	TwMinus.addActionListener(this);
  	TwMinus.setActionCommand("!-");
  	TwMinus.setMargin(margins);
  	TwMinus.setUI(new MetalButtonUI());
  	TwMinus.setToolTipText("Decrease the size of the current object uniformly"); //to be added
  	TwPx = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "px.png")));
  	TwPx.addActionListener(this);
  	TwPx.setActionCommand("!x");
  	TwPx.setMargin(margins);
  	TwPx.setUI(new MetalButtonUI());
  	TwPx.setToolTipText("Increase the size of the current object in the X axis"); //to be added
  	TwMx = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "mx.png")));
  	TwMx.addActionListener(this);
  	TwMx.setActionCommand("!1");
  	TwMx.setMargin(margins);
  	TwMx.setUI(new MetalButtonUI());
  	TwMx.setToolTipText("Decrease the size of the current object in the X axis"); //to be added
  	TwPy = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "py.png")));
  	TwPy.addActionListener(this);
  	TwPy.setActionCommand("!y");
  	TwPy.setMargin(margins);
  	TwPy.setUI(new MetalButtonUI());
  	TwPy.setToolTipText("Increase the size of the current object in the Y axis"); //to be added
  	TwMy = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "my.png")));
  	TwMy.addActionListener(this);
  	TwMy.setActionCommand("!2");
  	TwMy.setMargin(margins);
  	TwMy.setUI(new MetalButtonUI());
  	TwMy.setToolTipText("Decrease the size of the current object in the Y axis"); //to be added
  	TwPz = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "pz.png")));
  	TwPz.addActionListener(this);
  	TwPz.setActionCommand("!z");
  	TwPz.setMargin(margins);
  	TwPz.setUI(new MetalButtonUI());
  	TwPz.setToolTipText("Increase the size of the current object in the Z axis"); //to be added
  	TwMz = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "mz.png")));
  	TwMz.addActionListener(this);
  	TwMz.setActionCommand("!3");
  	TwMz.setMargin(margins);
  	TwMz.setUI(new MetalButtonUI());
  	TwMz.setToolTipText("Decrease the size of the current object in the Z axis"); //to be added
  	TwCol = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "colour.png")));
  	TwCol.addActionListener(this);
  	TwCol.setActionCommand("col");
  	TwCol.setMargin(margins);
  	TwCol.setUI(new MetalButtonUI());
  	TwCol.setToolTipText("Colour the current object with the chosen colour. Drop down below to select a colour."); //to be added
  	TwTex = new JButton(new ImageIcon(FrontEnd.getImage(this, TWIMG_DIR + "texture.png")));
  	TwTex.addActionListener(this);
  	TwTex.setActionCommand("tex");
  	TwTex.setMargin(margins);
  	TwTex.setUI(new MetalButtonUI());
  	TwTex.setToolTipText("Texture the current object with the chosen texture. Drop down below to browse for a texture."); //to be added
  	dropD = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "dropd.png")));
  	dropD.setPreferredSize(new Dimension(30,7));
  	dropD.addActionListener(this);
  	dropD.setMargin(margins);
  	dropD.setUI(new MetalButtonUI());
  	dropD.setToolTipText("Select a colour");
  	dropT = new JButton(new ImageIcon(FrontEnd.getImage(this, IMG_DIR + "dropd.png")));
  	dropT.setPreferredSize(new Dimension(30,7));
  	dropT.addActionListener(this);
  	dropT.setUI(new MetalButtonUI());
  	dropT.setToolTipText("Select a fill texture");
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
  	//zoomTool.setUI(new BasicMThumbSliderUI(zoomTool));

  	Font font = new Font("Serif", Font.ITALIC, 15);
  	zoomTool.setFont(font);
   }

   /** Initialises the private pane variable, adds the buttons to it */
   private void initPane() {
  	//int leftAnchor = GridBagConstraints.LINE_START;
  	//int rightAnchor = GridBagConstraints.LINE_END;
  	int centerAnchor = GridBagConstraints.CENTER;
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

  	GridBagConstraints c;

  	c = FrontEnd.buildGBC(0, 1, 0.5, 0.5, topCenterAnchor, right);
  	pane.add(selectTool, c);
 	 
  	c = FrontEnd.buildGBC(1, 1, 0.5, 0.5, topCenterAnchor, right);
  	pane.add(deselectTool, c);

  	c = FrontEnd.buildGBC(2, 1, 0.5, 0.5, topCenterAnchor, right);
  	pane.add(lineTool, c);

  	c = FrontEnd.buildGBC(3, 1, 0.5, 0.5, topCenterAnchor, right);
  	pane.add(curveTool, c);

  	c = FrontEnd.buildGBC(4, 1, 0.5, 0.5, topCenterAnchor, right);
  	pane.add(gridTool, c);
 	 
  	fillcom.setOpaque(false);
  	c = FrontEnd.buildGBC(5, 1, 0.5, 0.5, topCenterAnchor, right);
  	pane.add(fillcom, c);
 	 
  	c = FrontEnd.buildGBC(6, 1, 0.5, 0.5, topCenterAnchor, right);
  	pane.add(rfloor, c);
 	 
  	c = FrontEnd.buildGBC(7, 0, 0.5, 0.5, bottomCenterAnchor, new Insets(0,0,0,30));
  	pane.add(new JLabel("<html><font color='white'>Grid Zoom"), c);
  	c = FrontEnd.buildGBC(7, 1, 0.5, 0.5, topCenterAnchor, new Insets(0,0,0,30));
  	pane.add(zoomTool, c);
 	 
  	c = FrontEnd.buildGBC(8, 1, 0.5, 0.5, topCenterAnchor, right);
  	pane.add(dayToggle, c);
 	 
  	c = FrontEnd.buildGBC(9, 1, 0.5, 0.5, topCenterAnchor, right);
  	pane.add(tweaker, c);
   }  
   
   private void initTwPane() {
      	//int leftAnchor = GridBagConstraints.LINE_START;
      	//int rightAnchor = GridBagConstraints.LINE_END;
      	int centerAnchor = GridBagConstraints.CENTER;
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
      	//pane.setOpaque(false);
      	GridBagConstraints c;

      	c = FrontEnd.buildGBC(0, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwLeft, c);
      	c = FrontEnd.buildGBC(1, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwRight, c);
      	c = FrontEnd.buildGBC(2, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwFor, c);
      	c = FrontEnd.buildGBC(3, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwBack, c);
      	c = FrontEnd.buildGBC(4, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwUp, c);
      	c = FrontEnd.buildGBC(5, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwDown, c);
      	c = FrontEnd.buildGBC(6, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwRotl, c);
      	c = FrontEnd.buildGBC(7, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwRotr, c);
      	c = FrontEnd.buildGBC(8, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwPlus, c);
      	c = FrontEnd.buildGBC(9, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwMinus, c);
      	c = FrontEnd.buildGBC(10, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwPx, c);
      	c = FrontEnd.buildGBC(11, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwMx, c);
      	c = FrontEnd.buildGBC(12, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwPy, c);
      	c = FrontEnd.buildGBC(13, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwMy, c);
      	c = FrontEnd.buildGBC(14, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwPz, c);
      	c = FrontEnd.buildGBC(15, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwMz, c);     	 
      	c = FrontEnd.buildGBC(16, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwCol, c);
      	c = FrontEnd.buildGBC(17, 1, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(TwTex, c);
      	c = FrontEnd.buildGBC(16, 2, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(dropD, c);
      	c = FrontEnd.buildGBC(17, 2, 0.5, 0.5, topCenterAnchor, right);
      	pane.add(dropT, c);

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
  	String comm = e.getActionCommand();
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
     	viewport3D.toggleDay();
     	for(int i=0;i<10;i++)
   		  viewport3D.focus();
    	 
  	} else if (deselectTool == source) {
   	   frontEnd.getCurrentTab().getpanel().deselectall();
    	 
  	} else if (dropD==source) {
   	   Point tooltip = dropD.getLocationOnScreen();
   	   if(!tweakmode){
          	frontEnd.getCurrentTab().getpanel().togglepal(tooltip.x, tooltip.y+10);
   	   }
   	   else{
   		   if(palonshow==false){
              	twPalette.show(null,tooltip.x, tooltip.y+10);
              	pane.repaint();
              	palonshow = true;
   		   }else {
              	twPalette.hide();
              	pane.repaint();
              	palonshow = false;
          	}   		   
   	   }
  	} else if (dropT==source) {
   	   Point tooltip = dropT.getLocationOnScreen();
   		   if(texonshow==false){
              	twTex.show(null,tooltip.x, tooltip.y+10);
              	pane.repaint();
              	texonshow = true;
   		   }else {
              	twTex.hide();
              	pane.repaint();
              	texonshow = false;
          	}
  	} else if (tweaker == source) {
   	   if(System.getProperty("os.name").equals("Linux")){
   		   JOptionPane.showMessageDialog(null, "The tweaker is not currently avaliable for your OS","OS Incompatible", 1);
   	   }else{
          	viewport3D.shutdown3D();
   		   frontEnd.changetw();
   	   }   	   
  	} else if(fillTool == source){
   	   frontEnd.getCurrentTab().getpanel().fillfloor();
  	} else if(rfloor == source){
   	   frontEnd.getCurrentTab().getpanel().getFloorScreenshot(); 
  	} else if(comm.equals("col")){
   	   objectBrowser.getprev().paintitem(objectBrowser.getselected(),0,twTex.ppath(),twTex.pname(),0f,twPalette.getr(),twPalette.getg(),twPalette.getb());
 	 
  	}else if(comm.equals("tex")){
   	   if(twTex.ppath()==null){
   		   JOptionPane.showMessageDialog(null, "No Picture Selected!","Texture Error", 1);
   	   }else{
   		   objectBrowser.getprev().paintitem(objectBrowser.getselected(),1,twTex.ppath(),twTex.pname(),0f,twPalette.getr(),twPalette.getg(),twPalette.getb());
   	   }
  	} else {if(comm.substring(0,1).equals("!")){
   			  if(objectBrowser.getselected()!=-1){
   			  objectBrowser.getprev().moveitem(objectBrowser.getselected(),comm.charAt(1));
   			  } else{
   				  JOptionPane.showMessageDialog(null, "No item selected.","Error", 1);
   			  }
   		  }
  	else{
    	// Main.showFatalExceptionTraceWindow(
             	//new Exception("BUG: Action ocurred with unexpected source (" + e.getSource().toString() + ")"));
  	}}
  	frontEnd.requestFocusToCurrentTwoDScrollPane();
   }
   
   public void twButtons(boolean toggle){
   		TwLeft.setEnabled(toggle);
   		TwRight.setEnabled(toggle);
   		TwFor.setEnabled(toggle);
   		TwBack.setEnabled(toggle);
   		TwUp.setEnabled(toggle);
   		TwDown.setEnabled(toggle);
   		TwRotl.setEnabled(toggle);
   		TwRotr.setEnabled(toggle);
   		TwPlus.setEnabled(toggle);
   		TwMinus.setEnabled(toggle);
   		TwPx.setEnabled(toggle);
   		TwMx.setEnabled(toggle);
   		TwPy.setEnabled(toggle);
   		TwMy.setEnabled(toggle);
   		TwPz.setEnabled(toggle);
   		TwMz.setEnabled(toggle);
   		TwCol.setEnabled(toggle);
   		TwTex.setEnabled(toggle);
   }
}