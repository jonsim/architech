import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author God
 */
public class TWPalette extends JPopupMenu implements ChangeListener, MouseListener{
    private JButton parent;
    private float red,green,blue;
    public float getr() {return red;}
    public float getg() {return green;}
    public float getb() {return blue;}

    TWPalette(JButton wind) {
    	parent = wind;
    	this.addMouseListener(this);  
        JPanel colour = new JPanel();
        colour.setBorder(BorderFactory.createTitledBorder("Colour"));
        colour.setPreferredSize(new Dimension(500,300));  
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


}

