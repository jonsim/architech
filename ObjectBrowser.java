
import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.ListSelectionModel;
import java.awt.dnd.*;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.JScrollBar;
import java.awt.Dimension;

/**
 *
 * @author Michael, Brent
 */
public class ObjectBrowser implements MouseListener, ActionListener {
   public static final String IMG_DIR = "img/database/";
   private final Color divcol = new Color(74,74,74);
   private ListCellRenderer original;
   private boolean tweakmode;
   private int idcount=0;
   private TWPane preview;
   private Main main;
   private JPanel pane;
   private JSplitPane split;
   private JPanel splitTop;
   private JPanel splitBottom;
   private JLabel listTitle;
   public JList library;
   private int selectedindex=-1;
   private int selectedpos=-1;
   private JComboBox typelist;
   private JTextField desct,namet;
   public DefaultListModel fields = new DefaultListModel();
   private ListSelectionListener listSelectionListener;
   // This will get the database as long as it is in the current directory
   private String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "/ObjectDatabase.sqlite";
   private Font f = new Font("sansserif", Font.PLAIN, 18);
   private Connection connection = null;
   private PreparedStatement statement = null;
   private ResultSet rs = null;
   private JLabel picLabel;
   private int NextID = 37;
   private String dashedSeparator = "------------------------";
   private String backButtonText = "* Go Back *";
   private String wallpaperTitle = "* Wallpapers *";
   private String catTitle = "CATEGORIES";
   private String typeTitle = "TYPES";
   private String itemTitle = "ITEMS";
   private String wallTitle = "WALLPAPERS";
   private int currentCategory = -1;
   private int currentType = -1;
   private FurnitureObject draggedObject;
   private String objectName;
   private String typeName;
   private String categoryName;
   private int itemID = -1;
   private int typeID = -1;
   private int itemType = -1;
   private int isTweaked = 0;
   private int currentLibrary = -1;
   private int categoryIndex = -1;
   private int prevCurrentLib = -1;
   private JTextArea description;
   private JScrollPane libraryScroller;
   private JScrollPane descriptionScroller;
   private JPanel picPan;
   private String decName;
   public boolean wall = false;
   private JLabel piclabel;
   private JPanel picture;
   private String picpath,picname;

	ObjectBrowser(Main main) {
		this.main = main;
		initPane();
	}
	
	public Main getm(){
		return main;
	}

	private void initPane() {
		tweakmode = false;
		pane = new JPanel(new GridBagLayout());
		//pane.setBackground(Color.WHITE);
		//pane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	    splitTop = new JPanel(new GridBagLayout());
	    splitTop.setBackground(Color.WHITE);
		splitTop.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	    splitTop.setPreferredSize(new Dimension(160, 180));
	    splitBottom = new JPanel(new GridBagLayout());
	    splitBottom.setBackground(Color.WHITE);
		splitBottom.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	    splitBottom.setPreferredSize(new Dimension(160, 180));
	    split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, splitTop, splitBottom);
		split.setBackground(Color.WHITE);
		split.setDividerSize(1);
		Container div = (BasicSplitPaneDivider) split.getComponent(2);
		div.setBackground(Color.WHITE);
		split.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();// screen dimensions
	    scrDim.setSize(scrDim.getWidth()-200,scrDim.getHeight()-(scrDim.getHeight()/8));
	    //scrDim.setSize(scrDim.getWidth()-200,scrDim.getHeight()-100);
	    //System.out.println("Screen Size: "+scrDim);
	    //split.setDividerLocation(330);
	    split.setDividerLocation((int)scrDim.getHeight()/2);
		// Load up the list of categories
		ConnectSQL();
	    listTitle = new JLabel();
	    listTitle.setFont(new Font("sansserif", Font.BOLD, 20));
	    listTitle.setText(catTitle);
		Insets top_left_bottom_right = new Insets(5,1,1,1);
		GridBagConstraints gbc;
	    gbc = FrontEnd.buildGBC(0, 0, 0.5, 0.5, GridBagConstraints.NORTH, new Insets(10,10,10,10));
	    splitTop.add(listTitle, gbc);
		AddLibrary();
		currentCategory = -1;
		addDescriptionPane();
		addImagePane();
		picInitialise();
		SQLStatement("select COUNT(*) from TYPE", "count");
	}
	
	private void addDescriptionPane() {
		description = new JTextArea();
		description.setFont(f);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setEditable(false);
		descriptionScroller = new JScrollPane(description);
		descriptionScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		GridBagConstraints c;
		Insets top_left_bottom_right = new Insets(10,10,10,10);
		c = FrontEnd.buildGBC(0, 0, 0.5, 0.5, GridBagConstraints.CENTER, top_left_bottom_right);
		c.weighty = 15;
		c.fill = GridBagConstraints.BOTH;
		splitBottom.add(descriptionScroller, c);
		splitBottom.revalidate();
	}

	private void addImagePane() {
		picPan = new JPanel();
		picPan.setBackground(Color.WHITE);
		Insets top_left_bottom_right = new Insets(10,10,10,10);
		GridBagConstraints gbc;
		gbc = FrontEnd.buildGBC(0, 1, 0.5, 0.5, GridBagConstraints.CENTER, top_left_bottom_right);
		gbc.weighty = 40;
		splitBottom.add(picPan, gbc);
		Border border = BorderFactory.createLineBorder(Color.WHITE);
		picPan.setBorder(border);
		splitBottom.revalidate();
	}
	
	private void AddLibrary() {
		library = new JList(fields);
		library.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		library.setFont(f);
		library.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		libraryScroller = new JScrollPane(library);
		libraryScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		libraryScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		libraryScroller.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		Insets top_left_bottom_right = new Insets(5,10,10,10);
		GridBagConstraints c;
		c = FrontEnd.buildGBC(0, 1, 0.5, 0.5, GridBagConstraints.NORTHWEST, top_left_bottom_right);
		c.weighty = 45;
		c.fill = GridBagConstraints.BOTH;
		splitTop.add(libraryScroller, c);
		listSelectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent listSelectionEvent) {
				int index = library.getSelectedIndex();
				boolean adjust = listSelectionEvent.getValueIsAdjusting();
				if(!adjust) {
					if(index >= 0 && index < fields.size()) {
						typeName = fields.get(index).toString();
						Object obj = fields.get(index);
						typeID = getTypeID(typeName);
            if (wall == false) {
						   setDescription(typeName);
						   showImage(obj,typeID);	
            }	
            else{
               showWallpaper(obj);
            }
					}
				}
			}
		};
		library.addMouseListener(this);
		library.setDragEnabled(true);
		SQLStatement("select * from CATEGORIES ORDER BY Category", "Category");
		currentLibrary = 0;
		library.setDragEnabled(true);
  		library.setTransferHandler(new FurnitureTransferHandler(this));
  		DropTarget mustDisable = library.getDropTarget();
   		if (mustDisable != null) mustDisable.setActive(false);
	}
	
	private void ConnectSQL() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection(url);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void DisconnectSQL() {
		try {
			rs.close();
			statement.close();
			connection.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// 'Type' changes what column of the table is returned or what action is performed
	private void SQLStatement(String request, String Type) {
		try {
			statement = connection.prepareStatement(request);
			if(Type.equals("update")) {
				statement.executeUpdate();
			} else {
				rs = statement.executeQuery();
				if(Type.equals("count")) NextID = rs.getInt(1);
				else while(rs.next()) fields.addElement(rs.getString(Type));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setDescription(String objectName) {
		if(currentLibrary == 2) {
			try {
				int ID = getID(objectName);
				statement = connection.prepareStatement("select * from ITEM where ID=" + ID);
				rs = statement.executeQuery();
				if(rs.next()) {
					description.setText(rs.getString("Description"));
					description.setCaretPosition(0);
				} else description.setText("");
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else description.setText("");
	}

   public boolean isDoorWindow( int typeID ) {
      try {
         statement = connection.prepareStatement("select Category1, Category2, Category3 from TYPE where ID = '" + typeID + "'");
         rs = statement.executeQuery();
         if (rs.next()) {
            if(rs.getInt("Category1") == 8 || rs.getInt("Category1") == 9 || rs.getInt("Category2") == 8 || rs.getInt("Category2") == 9 || rs.getInt("Category3") == 8 || rs.getInt("Category3") == 9)
               return true;
         }
      } catch(Exception e) {
			e.printStackTrace();
		}

      return false;
   }
	
	private void getDimensions(int itemTypeID, int isTweaked) {
		draggedObject = null;
		try {
       if (isTweaked == 0) {
			    statement = connection.prepareStatement("select * from TYPE where ID='" + itemTypeID + "'");
			    rs = statement.executeQuery();
			    if(rs.next()) {
				     draggedObject = new FurnitureObject(objectName, rs.getFloat("Length"), rs.getFloat("Width"), rs.getFloat("Height"));
			    }
       }
       if (isTweaked == 1){
          statement = connection.prepareStatement("select * from ITEM where Name='" + objectName + "'");
			    rs = statement.executeQuery();
			    if(rs.next()) {
				     draggedObject = new FurnitureObject(objectName, rs.getFloat("Length"), rs.getFloat("Width"), rs.getFloat("Height"));
			    }
       }
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private int getID(String itemName) {
		itemID = -1;
		try {
			statement = connection.prepareStatement("select * from ITEM where Name='" + itemName + "'");
			rs = statement.executeQuery();
			if(rs.next()) {
				itemID = rs.getInt("ID");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return itemID;
	}

	private int getItemType(String itemName)
	{
		itemType = -1;
		try {
			statement = connection.prepareStatement("select * from ITEM where Name='" + itemName + "'");
			rs = statement.executeQuery();
			if(rs.next()) {
				itemType = rs.getInt("Type");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return itemType;
	}

	private int getTweaked(String itemName)
	{
		isTweaked = 0;
		try {
			statement = connection.prepareStatement("select * from ITEM where Name='" + itemName + "'");
			rs = statement.executeQuery();
			if(rs.next()) {
				isTweaked = rs.getInt("Tweaked");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return isTweaked;
	}

	private int getTypeID(String typeName)
	{
		try {
			statement = connection.prepareStatement("select * from TYPE where Type='" + typeName + "'");
			rs = statement.executeQuery();
			if(rs.next()) {
				typeID = rs.getInt("ID");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return typeID;
	}

	private void selectType() {
		int index = library.getSelectedIndex();
		if (index != -1)
		{
			typeName = fields.get(index).toString();
			index++;
		}
		if(index > 0 && currentType == -1 && !typeName.equals(dashedSeparator)) {
      listTitle.setText(itemTitle);
			library.addListSelectionListener(listSelectionListener);
			int typeIndex = getTypeID(typeName);
			currentType = typeIndex;
			fields.clear();
			SQLStatement("select * from ITEM where Type="+ typeIndex +" ORDER BY Name", "Name");
			currentLibrary = 2;
			fields.addElement(dashedSeparator);
			fields.addElement(backButtonText);
			library.setSelectedIndex(-1);
			library.ensureIndexIsVisible(0);
			pane.revalidate();
		}
	}
	
	private void selectCategory() {
		int index = library.getSelectedIndex();
		index++;
		if(index > 0 && currentCategory == -1) {
      listTitle.setText(typeTitle);
			library.addListSelectionListener(listSelectionListener);
      categoryName = fields.get(index-1).toString();
      try{
         statement = connection.prepareStatement("select * from CATEGORIES where Category='" + categoryName + "'");
			   rs = statement.executeQuery();
         if(rs.next()) {
				    currentCategory = rs.getInt("ID");
			   } 
      } catch(Exception e) {
			      e.printStackTrace();
		  }
			fields.clear();
			SQLStatement("select * from TYPE where Category1="+currentCategory+
				" or Category2="+currentCategory+" or Category3="+currentCategory+" ORDER BY Type", "Type");
			currentLibrary = 1;
			categoryIndex = currentCategory;
			fields.addElement(dashedSeparator);
			fields.addElement(backButtonText);
			library.setSelectedIndex(-1);
			splitTop.revalidate();
		}
	}

  private void selectWallpaper() {
     int index = 1;
     //System.out.println("index: "+index);
		 if(index > 0 ) {
			  library.addListSelectionListener(listSelectionListener);
			  library.ensureIndexIsVisible(0);
              wall = true;
			  splitTop.revalidate();
		 }
  }

  private void showWallpaper(Object object) {
    if(library.getSelectedIndex() >= 0) {
    		//System.out.println("selected");
			Insets top_left_bottom_right = new Insets(10,10,10,10);
			GridBagConstraints gbc;
			gbc = FrontEnd.buildGBC(0, 2, 0.5, 0.5, GridBagConstraints.CENTER, top_left_bottom_right);
			gbc.weighty = 40;
			JLabel blankLabel = new JLabel(new ImageIcon( FrontEnd.getImage(this, IMG_DIR+"blank.png") ));
			picPan.add(blankLabel);
			picPan.remove(picLabel);
			splitTop.revalidate();
			try {
				BufferedImage myPicture;
				if(object == dashedSeparator || object == backButtonText || object == wallpaperTitle) {
					picLabel = new JLabel(new ImageIcon( FrontEnd.getImage(this, IMG_DIR+"blank.png") ));
          description.setText("");
					description.setCaretPosition(0);
				} else {
					String request = "select * from ITEM where Type='38' AND Name='"+object+"'";
					statement = connection.prepareStatement(request);
					rs = statement.executeQuery();
					String image = rs.getString("Image");
					if (image.equals("none")==true)
					{
						picLabel = new JLabel(new ImageIcon( FrontEnd.getImage(this, IMG_DIR+"NoImage.png") ));
						Border border = BorderFactory.createLineBorder(Color.GRAY);
						picPan.setBorder(border);
						description.setText("");
					  description.setCaretPosition(0);
					}
					else
					{
						main.frontEnd.gethvs().texcurrent("img/wallpapers/"+image);
						picLabel = new JLabel(new ImageIcon( FrontEnd.getImage(this, IMG_DIR+image)));
						Border border = BorderFactory.createLineBorder(Color.GRAY);
						picPan.setBorder(border);
						description.setText(rs.getString("Description"));
					   description.setCaretPosition(0);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			picPan.add(picLabel, gbc);
			picPan.remove(blankLabel);
			splitBottom.revalidate();
		}
  }
	
	private void toCategories() {
		if(currentCategory > 0 && library.getSelectedIndex() == fields.size()-1 ) {
			listTitle.setText(catTitle);
			library.removeListSelectionListener(listSelectionListener);
			currentCategory = -1;
			fields.clear();
			SQLStatement("select * from CATEGORIES ORDER BY Category", "Category");
			currentLibrary = 0;
			library.setSelectedIndex(-1);
			splitTop.revalidate();
		}
	}

	public void toReset() {
	  library.removeListSelectionListener(listSelectionListener);
      listTitle.setText(catTitle);
      description.setText("");
      picPan.remove(picLabel);
			currentCategory = -1;
      currentType = -1;
			fields.clear();
			SQLStatement("select * from CATEGORIES ORDER BY Category", "Category");
			currentLibrary = 0;
			library.setSelectedIndex(-1);
			splitTop.revalidate();
	}


	private void toType() {
		if(currentType > 0 && library.getSelectedIndex() == fields.size()-1 ) {
      listTitle.setText(typeTitle);
			library.removeListSelectionListener(listSelectionListener);
			typeID = -1;
			currentType = -1;
			fields.clear();
			SQLStatement("select * from TYPE where Category1="+currentCategory+
				" or Category2="+currentCategory+" or Category3="+currentCategory+" ORDER BY Type", "Type");
			currentLibrary = 1;
			fields.addElement(dashedSeparator);
			fields.addElement(backButtonText);
			library.setSelectedIndex(-1);
			splitTop.revalidate();
		}
	}

  public void toDecoration() {
      library.removeListSelectionListener(listSelectionListener);
      listTitle.setText(wallTitle);
      description.setText("");
      picPan.remove(picLabel);
			fields.clear();
			fields.addElement(dashedSeparator);
			SQLStatement("select * from ITEM where Type='38'"+" ORDER BY Name", "Name");
			prevCurrentLib = currentLibrary;
			currentLibrary = 3;
		    currentType = 38;
			library.setSelectedIndex(-1);
			selectWallpaper();
			splitTop.revalidate();
  }

	private String getModel(Object object) {
		String model = null;
		if(library.getSelectedIndex() >= 0 && currentType > 0) {
			//pane.remove(picLabel);
			try {
				if(object != dashedSeparator && object != backButtonText) {
					String request = "select * from ITEM where Name='"+object+"'";
					//System.out.println("OBJNAME: "+object);
					statement = connection.prepareStatement(request);
					rs = statement.executeQuery();
					//System.out.println("Type Name ===== "+typeName);
					if(rs.next()) {
						model = rs.getString("Model");	
						//System.out.println("MODEL: "+model);				
					} 
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		//System.out.println("ObjPath - - - - "+model);
		return model;
	}
	
	private void showImage(Object object, Object typeName) {
		if(library.getSelectedIndex() >= 0 && currentType > 0) {
			Insets top_left_bottom_right = new Insets(10,10,10,10);
			GridBagConstraints gbc;
			gbc = FrontEnd.buildGBC(0, 2, 0.5, 0.5, GridBagConstraints.CENTER, top_left_bottom_right);
			gbc.weighty = 40;
			Image prev = FrontEnd.getImage(this, IMG_DIR+"blank.png");
			prev = prev.getScaledInstance( 128, 128,  java.awt.Image.SCALE_SMOOTH ) ;  
			JLabel blankLabel = new JLabel(new ImageIcon( prev ));
			picPan.add(blankLabel);
			picPan.remove(picLabel);
			splitBottom.revalidate();
			try {
				BufferedImage myPicture;
				if(object == dashedSeparator || object == backButtonText) {
					prev = FrontEnd.getImage(this, IMG_DIR+"blank.png");
					prev = prev.getScaledInstance( 128, 128,  java.awt.Image.SCALE_SMOOTH ) ; 
					picLabel = new JLabel(new ImageIcon( prev ));
				} else {
					String request = "select * from ITEM where Type='" + typeName + "' AND Name='"+object+"'";
					statement = connection.prepareStatement(request);
					rs = statement.executeQuery();
					//System.out.println("Type Name ===== "+typeName);
					if(rs.next()) {
						String image = rs.getString("Image");
						if (image.equals("none")==true)
						{
							prev = FrontEnd.getImage(this, IMG_DIR+"NoImage.png");
							prev = prev.getScaledInstance( 128, 128,  java.awt.Image.SCALE_SMOOTH ) ; 
							picLabel = new JLabel(new ImageIcon( prev));
							Border border = BorderFactory.createLineBorder(Color.GRAY);
		          picPan.setBorder(border);
						}
						else
					    {
							prev = FrontEnd.getImage(this, IMG_DIR+image);
							prev = prev.getScaledInstance( 128, 128,  java.awt.Image.SCALE_SMOOTH ) ; 
							picLabel = new JLabel(new ImageIcon( prev));
							Border border = BorderFactory.createLineBorder(Color.GRAY);
		          picPan.setBorder(border);
						}
					} else {
						prev = FrontEnd.getImage(this, IMG_DIR+"NoImage.png");
						prev = prev.getScaledInstance( 128, 128,  java.awt.Image.SCALE_SMOOTH ) ; 
						picLabel = new JLabel(new ImageIcon( prev ));
						Border border = BorderFactory.createLineBorder(Color.GRAY);
		        picPan.setBorder(border);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			picPan.add(picLabel, gbc);
			picPan.remove(blankLabel);
			splitBottom.revalidate();
		}
	}

	private void picInitialise(){
		try {
			picLabel = new JLabel(new ImageIcon( FrontEnd.getImage(this, IMG_DIR+"blank.png") ));
			Insets top_left_bottom_right = new Insets(10,10,10,10);
			GridBagConstraints gbc;
			gbc = FrontEnd.buildGBC(0, 1, 0.5, 0.5, GridBagConstraints.CENTER, top_left_bottom_right);
			gbc.weighty = 40;
			splitBottom.add(picLabel, gbc);
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	public void addObject(String object, int type,String desc,String image, String model,float width,float length,float height) {
		// Strings put into a TEXT field in sql need to be surrounded by apostrophes: eg. "'"+object+"'"
		SQLStatement("select COUNT(*) from ITEM", "count");
		String request = "select * from ITEM";
		int nextcode=0;
		try{
		statement = connection.prepareStatement(request);
		rs = statement.executeQuery();
		String ident = null;
		while (rs.next()) {
	        ident = rs.getString("ID");
	    }
		nextcode = Integer.parseInt(ident);
		} 
		catch(Exception e) {e.printStackTrace(); }
		NextID = nextcode+1;
		//System.out.println(NextID);
		SQLStatement("insert into ITEM values ("+NextID+",'"+object+"','"+desc+"',"+type+","+1+",'"+image+"','"+model+"'," + 1 +","+width+","+length+","+height+")", "update");
	}
	
	public void deleteObject() {
		if(currentType > 0 && library.getSelectedIndex() != -1) {
			String object = library.getSelectedValue().toString();
			if(!object.equals(dashedSeparator) && !object.equals(backButtonText)) {
				JFrame window = new JFrame("Delete");
				int choice = JOptionPane.showConfirmDialog(window,
				"Confirm Delete of Item \"" + object + "\"", "Delete", JOptionPane.YES_NO_OPTION);
				if(choice == JOptionPane.YES_OPTION) {
					try {
						statement = connection.prepareStatement("DELETE FROM ITEM WHERE Name='" + object + "'");
						int delete = statement.executeUpdate();
						int typeIndex = getTypeID(typeName);
						fields.clear();
						SQLStatement("select * from ITEM where Type="+ typeIndex, "Name");
						currentLibrary = 2;
						fields.addElement(dashedSeparator);
						fields.addElement(backButtonText);
						library.setSelectedIndex(-1);
						Insets top_left_bottom_right = new Insets(10,10,10,10);
						GridBagConstraints gbc;
						gbc = FrontEnd.buildGBC(0, 2, 0.5, 0.5, GridBagConstraints.CENTER, top_left_bottom_right);
						gbc.weighty = 40;
						JLabel blankLabel = new JLabel(new ImageIcon( FrontEnd.getImage(this, IMG_DIR+"blank.png") ));
						pane.add(blankLabel, gbc);
						pane.remove(picLabel);
						picLabel = new JLabel(new ImageIcon( FrontEnd.getImage(this, IMG_DIR+"blank.png")));
						splitBottom.add(picLabel, gbc);
						splitBottom.remove(blankLabel);
						splitBottom.revalidate();
					} catch(Exception e) {
						e.printStackTrace();
					}
					window.dispose();
				} else window.dispose();
			}
		}
	}

	public FurnitureSQLData getSelectedFurnitureOrNull()
	{
		String itemName = library.getSelectedValue().toString();
		if (itemName.equals(dashedSeparator) || itemName.equals(backButtonText))
		{
			return null;
		}
		else 
		{
			getDimensions(getItemType(objectName), getTweaked(objectName));
			int ID = getID(itemName);
			String objPath = getModel(objectName);
			float width = draggedObject.X * 50;
			float length = draggedObject.Y * 50;
         int type = getItemType(itemName);
			FurnitureSQLData footprint = new FurnitureSQLData(ID, (float)width, (float)length, type, objPath);
			return footprint;
		}
		//return null;
	}

	
	public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == 10) {
			int index = library.getSelectedIndex();
			if(currentLibrary == 0)
			{
				selectCategory();
			}
			else if(currentLibrary == 1)
			{
				toCategories();
				selectType();
				//toType();
			}
			if(currentLibrary == 2)
			{
				toType();
		 		//toCategories();
			}
		} else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
			deleteObject();
		}
    }
    public void keyReleased(KeyEvent e) {}

	public void mousePressed(MouseEvent e) {
		if(tweakmode){
			selectedpos = library.locationToIndex(e.getPoint());
			objectName = fields.get(selectedpos).toString();
			objectName = objectName.substring(objectName.indexOf(':')+1,objectName.length()-1);
			selectedindex = Integer.parseInt(objectName);
			main.frontEnd.getDButtons().twButtons(true);
		}else{
		if(currentCategory > 0) {
			selectedpos = library.locationToIndex(e.getPoint());
			objectName = fields.get(selectedpos).toString();
			//System.out.println("Object Name = " + objectName);			
			getDimensions(getItemType(objectName), getTweaked(objectName));
			String test = getModel(objectName);
			//System.out.println("ObjPath - - - - - - - - - - "+test);
			if(draggedObject != null) {
				//System.out.println("Object Name = " + draggedObject.Name);
				//System.out.println("X = " + draggedObject.X + " m");
				//System.out.println("Y = " + draggedObject.Y + " m");
				//System.out.println("Z = " + draggedObject.Z + " m");
			} else if(!objectName.equals(dashedSeparator) && !objectName.equals(backButtonText)) {
				//System.out.println("Object '" + objectName + "' Not Found In Database 'TYPE'");
			}
		}}
	}

	public void mouseReleased(MouseEvent e) {
		draggedObject = null;
	}

	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount()%2 == 0 && !tweakmode) {
			if (currentLibrary == 0)
			{
				selectCategory();
			}
			if (currentLibrary == 1)
			{
				toCategories();
				selectType();
			}
			if (currentLibrary == 2)
			{
				toType();
			}
		}
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public JPanel getPane() {
		return pane;
	}

	public JSplitPane getSplit() {
		return split;
	}
	
	private void bottomSetUp(){
		splitBottom.removeAll();		
		ImageIcon plus = new ImageIcon(main.frontEnd.getImage(this,"img/designbuttons/add.png"));
        JButton pbut = new JButton("<html><h3>Add Object",plus);
        pbut.setActionCommand("add");
        pbut.addActionListener(this);
        pbut.setPreferredSize(new Dimension(200,50));
		ImageIcon minus = new ImageIcon(main.frontEnd.getImage(this,"img/designbuttons/min.png"));
        JButton mbut = new JButton("<html><h3>Remove Object",minus);
        mbut.setActionCommand("remove");
        mbut.addActionListener(this);
        mbut.setPreferredSize(new Dimension(200,50));
        //button.setMinimumSize( new Dimension(150, 50) );
        //button.setMaximumSize( new Dimension(150, 50) );
        GridBagConstraints c = FrontEnd.buildGBC(0, 0, 0.5, 0.5, GridBagConstraints.NORTH, new Insets(20,0,0,0));
		splitBottom.add(pbut,c);
        c = FrontEnd.buildGBC(0, 1, 0.5, 0.5, GridBagConstraints.NORTH, new Insets(0,0,0,0));
		splitBottom.add(mbut,c);		
		JPanel holster = new JPanel(new FlowLayout(FlowLayout.CENTER));
		holster.setOpaque(false);
		JLabel lab = new JLabel("<html><font size=4> Object Name:");
		namet = new JTextField(20);
		holster.add(lab);
		holster.add(namet);
		c = FrontEnd.buildGBC(0, 2, 0.5, 0.5, GridBagConstraints.NORTH, new Insets(0,0,0,0));
		splitBottom.add(holster,c);
		holster = new JPanel(new FlowLayout(FlowLayout.CENTER));	
		holster.setOpaque(false);
		lab = new JLabel("<html><font size=4> Description:");
		desct = new JTextField(20);
		holster.add(lab);
		holster.add(desct);
		c = FrontEnd.buildGBC(0, 3, 0.5, 0.5, GridBagConstraints.NORTH, new Insets(0,0,0,0));
		splitBottom.add(holster,c);
		holster = new JPanel(new FlowLayout(FlowLayout.CENTER));	
		holster.setOpaque(false);
		String[] types = {"Chair", "Armchair", "Sofa (2 person)", "Stool", "Bench", "Dining Table", "Desk", "Coffee Table", "Bedside Table", "Desk Lamp", "Table Lamp", "Floor Lamp", "Wall Light", "Ceiling Light", "Cupboard", "Drawers", "Wardrobe", "Bookcase", "Wall-mounted Cupboard", "Kitchen Units", "Single Bed", "Bath (w/Shower)", "Shower", "Bathroom Sink", "Toilet", "Oven", "Fridge", "Freezer", "Kitchen Sink", "DishWasher", "Rug", "Double Bed", "Sofa (3 person)", "Large Plant", "Pot Plant"};
	    Arrays.sort(types);
	    lab = new JLabel("<html><font size=4> Item Type:");
	    typelist = new JComboBox(types);
	    typelist.insertItemAt("None",0);
	    typelist.setSelectedIndex(0);
	    typelist.addActionListener(this);
	    holster.add(lab);
	    holster.add(typelist);
	    c = FrontEnd.buildGBC(0, 4, 0.5, 0.5, GridBagConstraints.NORTH, new Insets(0,0,0,0));
		splitBottom.add(holster,c);
		holster = new JPanel(new FlowLayout(FlowLayout.CENTER));	
		holster.setOpaque(false);
		ImageIcon save = new ImageIcon(main.frontEnd.getImage(this, "img/designbuttons/save.png"));
		JButton button = new JButton("<html><h3>Save",save);
		button.setActionCommand("save");
		button.addActionListener(this);
		holster.add(button);		   
		ImageIcon can = new ImageIcon(main.frontEnd.getImage(this, "img/designbuttons/can.png"));
		button = new JButton("<html><h3>Cancel", can);
		button.setActionCommand("can");
		button.addActionListener(this);
		holster.add(button);
	    c = FrontEnd.buildGBC(0, 5, 0.5, 0.5, GridBagConstraints.NORTH, new Insets(0,0,0,0));
		splitBottom.add(holster,c);
		splitBottom.revalidate();
		splitBottom.repaint();
	}
	
	public void changetonormal(){
		toReset();
		splitBottom.removeAll();
		currentCategory = -1;
		addDescriptionPane();
		addImagePane();
		picInitialise();
	    splitBottom.revalidate();
	    splitBottom.repaint();
		split.revalidate();
		split.repaint();
		tweakmode = false;
	}
	
	public void changetotw(TWPane pane){
		tweakmode = true;
		preview = pane;
		listTitle.setText("<html><center><font size=5>Current Objects:<br><font size=3>(add objects to get started)");
		original = library.getCellRenderer();
		fields.clear();
		main.frontEnd.getDButtons().twButtons(false);
		splitTop.revalidate();
		bottomSetUp();
	}	
	
	public int getselected(){
		return selectedindex;
	}
	
	public TWPane getprev(){
		return preview;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		 String comm = e.getActionCommand();
		 if (comm.equals("add")){		
			 URL folder = getClass().getResource("req");
	        	String fpath = folder.getPath();
			    final JFileChooser fc = new JFileChooser(fpath);
			    FileFilter objf = new ExtensionGroup("OBJ Files", new String[] {".obj"});
			    fc.setAcceptAllFileFilterUsed(false);
			    fc.addChoosableFileFilter(objf);
			    fc.setFileFilter(objf);
			    int returnVal = fc.showOpenDialog(split);
			    if (returnVal==0){
				    File file = fc.getSelectedFile();
				    String path = file.getPath();
				    String fname = file.getName();
				    path = path.substring(0,path.lastIndexOf(File.separator));
				    preview.additem(path,fname,idcount);
				    fields.addElement(fname +"  [ID:"+idcount+"]");
				    idcount++;
			    }
			    else{JOptionPane.showMessageDialog(null, "The file you selected didn't open properly. Try another.","File Error", 1);}
		 }
		 if (comm.equals("remove")){
			 if(selectedindex==-1){
				 JOptionPane.showMessageDialog(null, "No item selected","Error - Can't delete", 1);
			 }else{
				 preview.removeitem(selectedindex);
				 fields.remove(selectedpos);
				 main.frontEnd.getDButtons().twButtons(false);
				 selectedindex=-1;
			 }
		 }
		 if (comm.equals("save")){
			 preview.saveitem(namet.getText(), desct.getText(), (String)typelist.getSelectedItem(),this);
		 }
		 if (comm.equals("can")){
			 main.frontEnd.revert(preview);
		 }
	 
	}
}