import java.awt.Color;
import com.jme3.system.JmeCanvasContext;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import com.jme3.system.JmeCanvasContext;

public class Tweaker extends JFrame implements ActionListener {
	
	private TWPane preview;
	public float red;
	public float green;
	public float blue;
	JPanel buttons;
	JPanel controlarea;
    JScrollPane controlScroller;
	int controlcount = 0;
	int idcount = 0;
	JLabel piclabel;
	JPanel pane = new JPanel(new GridBagLayout());
	JPanel picture;
	public String picpath,picname;
	JTextField namet;
	JTextField desct;
	JComboBox typelist;
	TwoDPanel parent;
	Main main;

   public void addtodb(String object, int type,String desc,String image, String model,float width,float length,float height){
      main.objectBrowser.addObject(object,type,desc,image,model, width,length,height);
   }

	public String getname(){
		return namet.getText();
	}
	
	public TwoDPanel pan(){
		return parent;
	}
	
	public String getdesc(){
		return desct.getText();
	}
	
	public String gettype(){
		return (String)typelist.getSelectedItem();
	}
	
    public Tweaker(TwoDPanel pan, Main main) {
    	parent = pan;
      this.main = main;
       setVisible(true);
       setTitle("ArchiTECH Tweaker");
       this.setMinimumSize(this.getSize());
       this.setExtendedState(JFrame.MAXIMIZED_BOTH);
       red = (float) 0.1;
       setLocationRelativeTo(null);
       setDefaultCloseOperation(EXIT_ON_CLOSE);
       
       GridBagConstraints c = new GridBagConstraints();
       getContentPane().add(pane);
       
       JPanel tog = new JPanel(new GridBagLayout());
       JPanel top = new JPanel(new GridBagLayout());
       JPanel bottom = new JPanel();

       JPanel graphics = new JPanel();
       preview = new TWPane(this); 
       graphics.add(preview.getCanvas());
       graphics.setBorder(BorderFactory.createRaisedBevelBorder());
       addItem(pane,graphics,0,0,1,1, GridBagConstraints.CENTER);

       JPanel con = new JPanel(new GridBagLayout());
       Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();
       controlarea = new JPanel(new GridBagLayout());
       controlScroller = new JScrollPane(controlarea);
       controlScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       controlScroller.setPreferredSize(new Dimension( 500, (int) scrDim.getHeight()-520 ));
       controlScroller.setMinimumSize(new Dimension( 500, (int) scrDim.getHeight()-520 ));
       addItem(con,controlScroller,0,0,1,1, GridBagConstraints.CENTER);
       
       ImageIcon brush = new ImageIcon("img/add.png");
       JButton but = new JButton("<html><h1><font face='Gill Sans MT'>Add Object",brush);
       but.setActionCommand("add");
       but.addActionListener(this);
       addItem(con,but,0,1,1,1, GridBagConstraints.CENTER); 
       addItem(pane,con,1,0,1,1, GridBagConstraints.CENTER);

       JPanel color = new JPanel();
       color.setSize(10,10);  
       final JColorChooser colorChooser = new JColorChooser();
	    ColorSelectionModel model = colorChooser.getSelectionModel();
	    ChangeListener changeListener = new ChangeListener() {
	      public void stateChanged(ChangeEvent changeEvent) {
	        Color newc = colorChooser.getColor();
	        red = (float) newc.getRed() / (float) 255.0;
	        green = (float) newc.getGreen()  / (float)255.0;
	        blue = (float) newc.getBlue()  / (float) 255.0;
	      }
	    };
	    AbstractColorChooserPanel [] chooserPanels = colorChooser.getChooserPanels();
	    colorChooser.removeChooserPanel(chooserPanels[2]);
	    colorChooser.setPreviewPanel(new JPanel());
	    model.addChangeListener(changeListener);
	    color.add(colorChooser);
	    addItem(bottom,color,0,0,1,1, GridBagConstraints.EAST);

        picture = new JPanel(new GridBagLayout());
	    piclabel = new JLabel("<html><h3><font face='Gill Sans MT'>No Texture Selected",SwingConstants.CENTER);
	    piclabel.setVerticalAlignment(SwingConstants.CENTER);
	    picpath = null;
	    piclabel.setPreferredSize(new Dimension(200,200));
        piclabel.setMinimumSize(new Dimension(200,200));
	    piclabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	    addItem(picture,piclabel,0,0,1,1, GridBagConstraints.CENTER);

	    but = new JButton("Browse...");
	    but.setActionCommand("brow");
	    but.addActionListener(this);
	    addItem(picture,but,0,1,1,1, GridBagConstraints.CENTER);
	    
	    addItem(bottom,picture,0,1,1,1, GridBagConstraints.CENTER);
	    addItem(pane,bottom,0,1,1,1, GridBagConstraints.CENTER);

	   
       //addItem(tog, but, 0, 0, 1, 1, GridBagConstraints.CENTER);
	   ImageIcon save = new ImageIcon("save.png");
       but = new JButton("<html><h1><font face='Gill Sans MT'>Save",save);
       but.setActionCommand("save");
       but.addActionListener(this);
       addItem(tog, but, 0, 3, 2, 1, GridBagConstraints.CENTER);       
       
	    JLabel lab = new JLabel("<html><font face='Gill Sans MT' size=4> Object Name:");
	    namet = new JTextField(20);
	    addItem(tog,namet,1,0,1,1,GridBagConstraints.WEST);
	    addItem(tog,lab,0,0,1,1,GridBagConstraints.EAST); 
	    
	    lab = new JLabel("<html><font face='Gill Sans MT' size=4> Description:");
	    desct = new JTextField(20);
	    addItem(tog,desct,1,1,1,1,GridBagConstraints.WEST);
	    addItem(tog,lab,0,1,1,1,GridBagConstraints.EAST); 
	    
	    String[] types = {"Chair", "Armchair", "Sofa (2 person)", "Stool", "Bench", "Dining Table", "Desk", "Coffee Table", "Bedside Table", "Desk Lamp", "Table Lamp", "Floor Lamp", "Wall Light", "Ceiling Light", "Cupboard", "Drawers", "Wardrobe", "Bookcase", "Wall-mounted Cupboard", "Kitchen Units", "Single Bed", "Bath (w/Shower)", "Shower", "Bathroom Sink", "Toilet", "Oven", "Fridge", "Freezer", "Kitchen Sink", "DishWasher", "Rug", "Double Bed", "Sofa (3 person)", "Large Plant", "Pot Plant"};
	    Arrays.sort(types);
	    lab = new JLabel("<html><font face='Gill Sans MT' size=4> Item Type:");
	    addItem(tog,lab,0,2,1,1,GridBagConstraints.WEST);
	    typelist = new JComboBox(types);
	    typelist.insertItemAt("None",0);
	    typelist.setSelectedIndex(0);
	    typelist.addActionListener(this);
	  	addItem(tog,typelist,1,2,1,1,GridBagConstraints.CENTER); 

       addItem(pane, tog, 1, 1, 1, 1, GridBagConstraints.CENTER);

	   //addItem(pane,bottom,0,1,1,1, GridBagConstraints.WEST);

	   preview.focus();
    }
    
    /*public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	setSystemLookAndFeel();
                UIManager.put("Panel.background", Color.WHITE);
                Tweaker ex = new Tweaker();
                ex.setVisible(true);
            }
        });
    }*/
    
    JPanel addcontrols(int id, String fname){    	
    	buttons = new JPanel();
    	//controls.add(buttons);
    	buttons.setLayout(new GridBagLayout());
    	
        JLabel label = new JLabel("--------------------------------------------------------");
        addItem(buttons,label,0,0,4,1, GridBagConstraints.EAST);
        label = new JLabel("<html><b>Item ID " + id + ": " + fname);        
        addItem(buttons,label,0,1,2,1, GridBagConstraints.WEST);
        
        label = new JLabel("Left/Right");
        addItem(buttons,label,0,2,1,1, GridBagConstraints.EAST);
        
        JButton but = new JButton("<-");
        but.setActionCommand("!l" + id);
        but.addActionListener(this);
        addItem(buttons, but, 1, 2, 1, 1, GridBagConstraints.EAST);
        
        but = new JButton("->");
        but.setActionCommand("!r" + id);
        but.addActionListener(this);
        addItem(buttons, but, 2, 2, 1, 1, GridBagConstraints.EAST);
        
        label = new JLabel("Forward/Backward");
        addItem(buttons,label,0,3,1,1, GridBagConstraints.EAST);
        
        but = new JButton("<-");
        but.setActionCommand("!f" + id);
        but.addActionListener(this);
        addItem(buttons, but, 1, 3, 1, 1, GridBagConstraints.EAST);
        
        but = new JButton("->");
        but.setActionCommand("!b" + id);
        but.addActionListener(this);
        addItem(buttons, but, 2, 3, 1, 1, GridBagConstraints.EAST);
        
        label = new JLabel("Up/Down");
        addItem(buttons,label,0,4,1,1, GridBagConstraints.EAST);
        
        but = new JButton("^");
        but.setActionCommand("!u" + id);
        but.addActionListener(this);
        addItem(buttons, but, 1, 4, 1, 1, GridBagConstraints.EAST);
        
        but = new JButton("v");
        but.setActionCommand("!d" + id);
        but.addActionListener(this);
        addItem(buttons, but, 2, 4, 1, 1, GridBagConstraints.EAST);      
        
        label = new JLabel("Texture Tiling");
        addItem(buttons,label,0,7,1,1, GridBagConstraints.EAST);
        
        but = new JButton("+");
        but.setActionCommand("txp" + id);
        but.addActionListener(this);
        addItem(buttons, but, 1, 7, 1, 1, GridBagConstraints.EAST);
        
        but = new JButton("-");
        but.setActionCommand("txm" + id);
        but.addActionListener(this);
        addItem(buttons, but, 2, 7, 1, 1, GridBagConstraints.EAST);   

        ImageIcon brush = new ImageIcon("pas.png");
        but = new JButton("<html>Paint with<P> current<p>colour</html>", brush);
        but.setActionCommand("paint" + id);
        but.addActionListener(this);
        addItem(buttons, but, 3, 2, 1, 2, GridBagConstraints.EAST);
        
        brush = new ImageIcon("tartan.gif");
        but = new JButton("<html>Texture with<P> current<p>image</html>", brush);
        but.setActionCommand("texture" + id);
        but.addActionListener(this);
        addItem(buttons, but, 3, 6, 1, 2, GridBagConstraints.EAST);
        
        ImageIcon bin = new ImageIcon("binn.png");
        but = new JButton("Remove",bin);
        but.setActionCommand("remove" + id);
        but.addActionListener(this);
        addItem(buttons, but, 3, 4, 1, 2, GridBagConstraints.EAST);   
        
        label = new JLabel("Bigger/Smaller");
        addItem(buttons,label,0,5,1,1, GridBagConstraints.EAST);
        
        but = new JButton("+");
        but.setActionCommand("!+" + id);
        but.addActionListener(this);
        addItem(buttons, but, 1, 5, 1, 1, GridBagConstraints.EAST);
        
        but = new JButton("-");
        but.setActionCommand("!-" + id);
        but.addActionListener(this);
        addItem(buttons, but, 2, 5, 1, 1, GridBagConstraints.EAST); 
        
        label = new JLabel("Rotate");
        addItem(buttons,label,0,6,1,1, GridBagConstraints.EAST);
        
        but = new JButton("<");
        but.setActionCommand("!<" + id);
        but.addActionListener(this);
        addItem(buttons, but, 1, 6, 1, 1, GridBagConstraints.EAST);
        
        but = new JButton(">");
        but.setActionCommand("!>" + id);
        but.addActionListener(this);
        addItem(buttons, but, 2, 6, 1, 1, GridBagConstraints.EAST);
        
        label = new JLabel("--------------------------------------------------------");
        addItem(buttons,label,0,8,4,1, GridBagConstraints.EAST);

        addItem(controlarea,buttons,0,controlcount,1,1, GridBagConstraints.EAST);
        controlarea.validate();
        controlScroller.validate();
        pane.revalidate();
        return buttons;
    }
    
    private static void setSystemLookAndFeel() {
        try {
           UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {} 
        
        try {
           System.setProperty("apple.laf.useScreenMenuBar", "true");
        } catch (Exception e) {}
     }
    
    public void remove(JPanel control){
    	controlarea.remove(control);
        controlarea.revalidate();
		pane.revalidate();
    }
    
	@Override
	public void actionPerformed (ActionEvent e) {
		 String comm = e.getActionCommand();
		 if (comm.contains("paint")) {
			   preview.paintitem(Character.digit(comm.charAt(5),10),0,picpath,picname,0f);
		 }
		 else if(comm.contains("texture")){
			 if(picpath==null){JOptionPane.showMessageDialog(null, "No Picture Selected!","Texture Error", 1);
		 }else{
			  {preview.paintitem(Character.digit(comm.charAt(7),10),1,picpath,picname,0f);
		 }}}
		 else if(comm.substring(0,2).equals("tx")){
			 if(comm.charAt(2)=='p') {preview.paintitem(Character.digit(comm.charAt(3),10),1,picpath,picname,1f);}
			 if(comm.charAt(2)=='m') {preview.paintitem(Character.digit(comm.charAt(3),10),1,picpath,picname,-1f);}
		 }
		 else if(comm.equals("save")){
			 preview.saveitem();
		 }
		 /*else if(comm.equals("eeb")){
			 String path = "C:\\Users\\Robin\\Documents\\University\\SPE\\google cvs\\beta resources backup\\req main\\bench\\";
			 JPanel control = addcontrols(idcount,"shower.obj");
			 preview.additem(path,"bench.obj",control);
		     controlcount++;
		     idcount++;
		 }*/
		 else if ("add".equals(comm)){
			    final JFileChooser fc = new JFileChooser("C:/Users/Robin/Documents/University/SPE/google cvs/beta resources backup/req main/");
			    FileFilter objf = new ExtensionGroup("OBJ Files", new String[] {".obj"});
			    fc.setAcceptAllFileFilterUsed(false);
			    fc.addChoosableFileFilter(objf);
			    fc.setFileFilter(objf);
			    int returnVal = fc.showOpenDialog(this);
			    if (returnVal==0){
				    File file = fc.getSelectedFile();
				    String path = file.getPath();
				    String fname = file.getName();
				    path = path.substring(0,path.lastIndexOf(File.separator));
			 		//String path = null;
			 		//String fname = "merge.obj";
				    JPanel control = addcontrols(idcount,fname);
				    preview.additem(path,fname,control);
				    controlcount++;
				    idcount++;
				    //JPanel control = addcontrols(idcount,"shower.obj");
				    //String path = "C:\\Users\\Robin\\Documents\\University\\SPE\\google cvs\\beta resources backup\\req main\\bench\\";
				    //String path = "C:/Users/Robin/Documents/University/SPE/google cvs/beta resources backup/req main/bedside_table2";
				    //preview.additem(path,"bedside_table2.obj",control);
				    //preview.additem(path,"bench.obj",control);
				    //controlcount++;
				    //idcount++;
			    }
			    else{JOptionPane.showMessageDialog(null, "The file you selected didn't open properly. Try another.","File Error", 1);}
		 }
		 else{
			 if(comm.substring(0,1).equals("!")){
				 int code = Integer.parseInt(comm.substring(2,3));
				 preview.moveitem(code,comm.charAt(1));
			 }
			 else if(comm.contains("remove")){
				 JPanel control = preview.removeitem(Character.digit(comm.charAt(6),10));
				 controlarea.remove(control);
		         controlarea.revalidate();
				 pane.revalidate();
			 } else if (comm.equals("brow")){
				 final JFileChooser fc = new JFileChooser("req");
				 FileFilter img = new ExtensionGroup("Supported Image Files", new String[] {".png",".jpg",".gif"});
				 fc.addChoosableFileFilter(img);
				 fc.setFileFilter(img);
				 int returnVal = fc.showOpenDialog(this);    
				 if (returnVal==0){
					    BufferedImage myPicture = null;	   
					    try{
				    	File file = fc.getSelectedFile();
					    picpath = file.getPath();
					   
					    if(picpath.lastIndexOf('/')==-1){
					     picname = file.getPath().split("\\\\")[file.getPath().split("\\\\").length-1];
					     picpath = picpath.substring(0,picpath.lastIndexOf('\\'));}
					    else{
					    	picname = file.getPath().split("//")[file.getPath().split("/").length-1];
					    	picpath = picpath.substring(0,picpath.lastIndexOf('/'));}		
					    System.out.println(picpath + picname);
					    myPicture = ImageIO.read(file);
					    picture.remove(piclabel);}
					    catch(IOException x){};
					    Image imag = (new ImageIcon(myPicture)).getImage();
					    Image newimg = imag.getScaledInstance( 190, 190,  java.awt.Image.SCALE_SMOOTH ) ;  
					    ImageIcon icon = new ImageIcon( newimg );
					    piclabel = new JLabel(icon);
					    piclabel.setPreferredSize(new Dimension(200,200));
					    piclabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
					    addItem(picture,piclabel,0,1,1,1, GridBagConstraints.CENTER);
					    picture.revalidate();
				 }
			 }
		 }
	}
	
	private void addItem(JPanel p, JComponent c, int x, int y, int width, int height, int align) {
	    GridBagConstraints gc = new GridBagConstraints();
	    gc.gridx = x;
	    gc.gridy = y;
	    gc.gridwidth = width;
	    gc.gridheight = height;
	    gc.weightx = 0.0;
	    gc.weighty = 0.0;
	    gc.insets = new Insets(5, 5, 5, 5);
	    gc.anchor = align;
	    gc.fill = GridBagConstraints.NONE;
	    p.add(c, gc);
	  }
}

class ExtensionGroup extends FileFilter {
    private String ext[];
    private String description;
    public ExtensionGroup(String description, String extension) {
      this(description, new String[] { extension });
    }
    public ExtensionGroup(String description, String extensions[]) {
      this.description = description;
      this.ext = (String[]) extensions.clone();
    }
    public boolean accept(File file) {
      if (file.isDirectory()) {
        return true;
      }
      int count = ext.length;
      String path = file.getAbsolutePath();
      for (int j = 0; j < count; j++) {
        String exts = ext[j];
        if (path.endsWith(exts)
            && (path.charAt(path.length() - exts.length()) == '.')) {
          return true;
        }
      }
      return false;
    }
    public String getDescription() {return description; }
  }