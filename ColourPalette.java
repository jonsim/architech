
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author God
 */
public class ColourPalette extends JPopupMenu implements ChangeListener, MouseListener {

    protected JColorChooser tcc;
    public Color fillColour = Color.black;

    ColourPalette() {
        tcc = new JColorChooser(Color.black);
        tcc.getSelectionModel().addChangeListener(this);
        tcc.setBorder(BorderFactory.createTitledBorder("Choose Fill Colour"));
        this.add(tcc, BorderLayout.PAGE_END);
        tcc.setPreviewPanel(new JPanel());
        this.addMouseListener(this);
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
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        //hide();
    }
}
