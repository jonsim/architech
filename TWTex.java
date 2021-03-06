import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author God
 */
public class TWTex extends JPopupMenu implements ChangeListener, MouseListener, ActionListener {
    private JButton browbut;
    private JPanel picture;
    private JLabel piclabel;
    private String picpath;
    private String picname;
    private JButton parent;
    public String ppath(){return picpath;}
    public String pname(){return picname;}

    TWTex(JButton wind) {
    	parent = wind;
    	this.addMouseListener(this);          
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
			 int returnVal = fc.showOpenDialog(parent);    
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