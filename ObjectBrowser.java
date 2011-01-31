
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ListSelectionModel;
import java.awt.dnd.*;

/**
 *
 * @author Michael, Brent
 */
public class ObjectBrowser implements KeyListener, MouseListener {

   private Main main;

   private JPanel pane;
   public JList library;
   public DefaultListModel fields = new DefaultListModel();
   private ListSelectionListener listSelectionListener;
   // This will get the database as long as it is in the current directory
   private String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "\\ObjectDatabase.sqlite";
   private Font f = new Font("sansserif", Font.PLAIN, 14);
   private Connection connection = null;
   private PreparedStatement statement = null;
   private ResultSet rs = null;
   private JLabel picLabel;
   private int NextID = 1;
   private String dashedSeparator = "------------------------";
   private String backButtonText = "* Go Back *";
   private int currentCategory = -1;
   private int currentType = -1;
   private FurnitureObject draggedObject;
   private String objectName;
   private String typeName;
   private int itemID = -1;
   private int typeID = -1;
   private int itemType = -1;
   private int currentLibrary = -1;
   private int categoryIndex = -1;

	ObjectBrowser(Main main) {
		this.main = main;

		initPane();
	}

	private void initPane() {
		pane = new JPanel(new GridBagLayout());
		pane.setBackground(Color.WHITE);
		pane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		// Load up the list of categories
		ConnectSQL();
		AddLibrary();
		currentCategory = -1;
		SQLStatement("select COUNT(*) from TYPE", "count");
	}
	
	private void AddLibrary() {
		library = new JList(fields);
		library.setFont(f);
		library.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		Insets top_left_bottom_right = new Insets(10,10,10,10);
		GridBagConstraints c;
		c = FrontEnd.buildGBC(0, 0, 0.5, 0.5, GridBagConstraints.NORTHWEST, top_left_bottom_right);
		pane.add(library, c);
		listSelectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent listSelectionEvent) {
				int index = library.getSelectedIndex();
				boolean adjust = listSelectionEvent.getValueIsAdjusting();
				if(!adjust) {
					if(index >= 0 && index < fields.size())//{} //showImage(fields.get(index));
						//if (currentLibrary == 2)
						//{
							showImage(fields.get(index), typeName);
						//}
				}
			}
		};
		library.addMouseListener(this);
		library.addKeyListener(this);
		library.setDragEnabled(true);
		SQLStatement("select * from CATEGORIES", "Category");
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
	
	private void getDimensions(int itemTypeID) {
		draggedObject = null;
		try {
			statement = connection.prepareStatement("select * from TYPE where ID='" + itemTypeID + "'");
			rs = statement.executeQuery();
			if(rs.next()) {
				draggedObject = new FurnitureObject(objectName, rs.getFloat("Length"), rs.getFloat("Width"), rs.getFloat("Height"));
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
				//return itemID;
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
				//return itemID;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return itemType;
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
		if(index > 0 && currentType == -1) {
			library.addListSelectionListener(listSelectionListener);
			int typeIndex = getTypeID(typeName);
			currentType = index;
			fields.clear();
			SQLStatement("select * from ITEM where Type="+ typeIndex, "Name");
			currentLibrary = 2;
			fields.addElement(dashedSeparator);
			fields.addElement(backButtonText);
			pane.revalidate();
		}
	}
	
	private void selectCategory() {
		int index = library.getSelectedIndex();
		index++;
		if(index > 0 && currentCategory == -1) {
			library.addListSelectionListener(listSelectionListener);
			currentCategory = index;
			fields.clear();
			SQLStatement("select * from TYPE where Category1="+index+
				" or Category2="+index+" or Category3="+index, "Type");
			currentLibrary = 1;
			categoryIndex = index;
			fields.addElement(dashedSeparator);
			fields.addElement(backButtonText);
			picInitialise();/////////////////////////////////////////
			pane.revalidate();
		}
	}
	
	private void toCategories() {
		if(currentCategory > 0 && library.getSelectedIndex() == fields.size()-1 ) {
			library.removeListSelectionListener(listSelectionListener);
			currentCategory = -1;
			fields.clear();
			pane.remove(picLabel);////////////////////////////////////
			SQLStatement("select * from CATEGORIES", "Category");
			currentLibrary = 0;
			pane.remove(picLabel);
			pane.revalidate();
		}
	}

	private void toType() {
		if(currentType > 0 && library.getSelectedIndex() == fields.size()-1 ) {
			library.removeListSelectionListener(listSelectionListener);
			typeID = -1;
			currentType = -1;
			fields.clear();
			pane.remove(picLabel);////////////////////////////////////
			SQLStatement("select * from TYPE where Category1="+categoryIndex+
				" or Category2="+categoryIndex+" or Category3="+categoryIndex, "Type");
			currentLibrary = 1;
			fields.addElement(dashedSeparator);
			fields.addElement(backButtonText);
			pane.remove(picLabel);
			pane.revalidate();
		}
	}
	
	private void showImage(Object object, Object typeName) {
		if(library.getSelectedIndex() >= 0 && currentType > 0) {
			pane.remove(picLabel);
			try {
				BufferedImage myPicture;
				if(object == dashedSeparator || object == backButtonText) {
					myPicture = ImageIO.read(new File("blank.png"));
					picLabel = new JLabel(new ImageIcon( myPicture ));
				} else {
					String request = "select * from ITEM where Type='" + typeName + "' AND Name='"+object+"'";
					statement = connection.prepareStatement(request);
					rs = statement.executeQuery();
					System.out.println("Type Name ===== "+typeName);
					if(rs.next()) {
						String image = rs.getString("Image");
						if (image.equals("none")==true)
						{
							myPicture = ImageIO.read(new File("NoImage.png"));
							picLabel = new JLabel(new ImageIcon( myPicture ));
						}
						else
					    {
							Image i = main.frontEnd.getIcon("images/"+image);
							picLabel = new JLabel(new ImageIcon( i ));
						}
					} else {
						myPicture = ImageIO.read(new File("NoImage.png"));
						picLabel = new JLabel(new ImageIcon( myPicture ));
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			Insets top_left_bottom_right = new Insets(10,10,10,10);
			GridBagConstraints gbc;
			gbc = FrontEnd.buildGBC(0, 0, 0.5, 0.5, GridBagConstraints.SOUTH, top_left_bottom_right);
			pane.add(picLabel, gbc);
			pane.revalidate();
		}
	}

	private void picInitialise(){
		try {
			BufferedImage myPicture = ImageIO.read(new File("blank.png"));
			picLabel = new JLabel(new ImageIcon( myPicture ));
			Insets top_left_bottom_right = new Insets(10,10,10,10);
			GridBagConstraints gbc;
			gbc = FrontEnd.buildGBC(0, 0, 0.5, 0.5, GridBagConstraints.SOUTH, top_left_bottom_right);
			pane.add(picLabel, gbc);
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	// Make cat2 and/or cat3 0 if you want fewer than 3 categories 
	public void addObject(String object, int cat1, int cat2, int cat3) {
		NextID++;
		// Strings put into a TEXT field in sql need to be surrounded by apostrophes: eg. "'"+object+"'"
		SQLStatement("insert into TYPE values ("+NextID+",'"+object+"',"+cat1+","+cat2+","+cat3+")", "update");
		if(currentCategory == cat1 || currentCategory == cat2 || currentCategory == cat3) {
			fields.clear();
			SQLStatement("select * from TYPE where Category1="+currentCategory+
				" or Category2="+currentCategory+" or Category3="+currentCategory, "Type");
			pane.revalidate();
		}
	}
	
	public void deleteObject(String Object) {
		if(currentCategory > 0) {
			// I'll sort this out later
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
			getDimensions(getItemType(objectName));
			String ID = Integer.toString(getID(itemName));
			float width = draggedObject.X * 50;
			float length = draggedObject.Y * 50;
			FurnitureSQLData footprint = new FurnitureSQLData(ID, (float)width, (float)length);
			return footprint;
		}
		//return null;
	}

	
	public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == 10) {
				int index = library.getSelectedIndex();
				if (currentLibrary == 0)
				{
					selectCategory();
				}
				else if (currentLibrary == 1)
				{
					selectType();
		 		 	toCategories();
					//toType();
				}
				if (currentLibrary == 2)
				{
					toType();
		  		//toCategories();
				}
			}
    }

    public void keyReleased(KeyEvent e) {}

	public void mousePressed(MouseEvent e) {
		if(currentCategory > 0) {
			int index = library.locationToIndex(e.getPoint());
			objectName = fields.get(index).toString();////////////
System.out.println("Object Name = " + objectName);			
			getDimensions(getItemType(objectName));
			if(draggedObject != null) {
				System.out.println("Object Name = " + draggedObject.Name);
				System.out.println("X = " + draggedObject.X + " m");
				System.out.println("Y = " + draggedObject.Y + " m");
				System.out.println("Z = " + draggedObject.Z + " m");
			} else if(!objectName.equals(dashedSeparator) && !objectName.equals(backButtonText)) {
				System.out.println("Object '" + objectName + "' Not Found In Database 'TYPE'");
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		draggedObject = null;
	}

	public void mouseClicked(MouseEvent e) {
		int index = library.getSelectedIndex();
		typeName = fields.get(index).toString();
		Object obj = fields.get(index);
		typeID = getTypeID(typeName);
		System.out.println("TYPE ID === "+typeID);
		if(obj != dashedSeparator || obj != backButtonText) 
		{ 
				showImage(obj,typeID);
		}
		if(e.getClickCount() == 2) {
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
}
