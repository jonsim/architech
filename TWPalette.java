import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.*;

/**
 *
 * @author God
 */
public class TWPalette extends JPopupMenu implements KeyListener,ChangeListener, MouseListener, ActionListener {

    protected JColorChooser tcc;
    public Color fillColour = Color.black;
    boolean stickonce;
    private JButton browbut;
    private JPanel picture;
    private JLabel piclabel;
    private String picpath;
    private String picname;
    private float red,green,blue;    
    public float getr() {return red;}
    public float getg() {return green;}
    public float getb() {return blue;}
    public String ppath(){return picpath;}
    public String pname(){return picname;}    

    TWPalette() {
    	this.addMouseListener(this);  
        JPanel colour = new JPanel();
    	colour.addKeyListener(this);
        colour.setBorder(BorderFactory.createTitledBorder("Colour"));
        colour.setPreferredSize(new Dimension(500,220));  
        final JColorChooser colourChooser = new JColorChooser();
        ColorSelectionModel model = colourChooser.getSelectionModel();
        ChangeListener changeListener = new ChangeListener() {
           public void stateChanged(ChangeEvent changeEvent) {
             Color newc = colourChooser.getColor();
             red = (float) newc.getRed() / (float) 255.0;
             green = (float) newc.getGreen()  / (float)255.0;
             blue = (float) newc.getBlue()  / (float) 255.0;
           }
         };
         AbstractColorChooserPanel [] chooserPanels = colourChooser.getChooserPanels();
         if(chooserPanels.length>=2)
        	    colourChooser.removeChooserPanel(chooserPanels[2]);
         colourChooser.setPreviewPanel(new JPanel());
         model.addChangeListener(changeListener);
         colour.add(colourChooser);    
         this.add(colour, BorderLayout.CENTER);

         picture = new JPanel(new FlowLayout(FlowLayout.CENTER));
         picture.setBorder(BorderFactory.createTitledBorder("Texture"));
        // add texture picture to picture container
         piclabel = new JLabel("<html><h3>No Texture Selected",SwingConstants.CENTER);
         piclabel.setVerticalAlignment(SwingConstants.CENTER);
         picpath = null;
         piclabel.setPreferredSize(new Dimension(200,200));
         piclabel.setMinimumSize(new Dimension(200,200));
         piclabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
         picture.add(piclabel);
         browbut = new JButton("<html><h3>Browse");
         browbut.setActionCommand("brow");
         browbut.addActionListener(this);
         picture.add(browbut);
         this.add(picture, BorderLayout.CENTER); 
    }

    @Override
    public void hide() {
        this.setVisible(false);
    }

    public void stateChanged(ChangeEvent e) {
        fillColour = tcc.getColor();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    	if (e.isPopupTrigger()) {
    		hide();
    	}
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		String comm = e.getActionCommand();
		if (comm.equals("brow")){				 
			 final JFileChooser fc = new JFileChooser(getClass().getResource("img/tex").getPath());
			 FileFilter img = new ExtensionGroup("Supported Image Files", new String[] {".png",".jpg",".gif",".bmp"});
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
				    picture.add(piclabel);
				    picture.remove(browbut);
				    picture.add(browbut);
				    picture.revalidate();
			 }
		 }
	 }	

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		System.out.println(e.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
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
