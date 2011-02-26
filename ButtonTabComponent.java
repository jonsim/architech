
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class ButtonTabComponent extends JPanel implements ActionListener {
   private final JTabbedPane tabbedPane;

   public ButtonTabComponent(final JTabbedPane tabbedPane) {
      //unset default FlowLayout' gaps
      super(new FlowLayout(FlowLayout.LEFT, 0, 0));
      if (tabbedPane == null) {
         throw new NullPointerException("TabbedPane is null");
      }
      this.tabbedPane = tabbedPane;
      setOpaque(false);

      //make JLabel read titles from JTabbedPane
      JLabel label = new JLabel() {
         @Override
         public String getText() {
            int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
               return tabbedPane.getTitleAt(i);
            }
            return "";
         }
      };

      add(label);

      //add more space between the label and the button
      label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

      //tab button
      JButton button = new TabCloseButton();
      button.addActionListener(this);

      add(button);

      //add more space to the top of the component
      setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
   }

   public void actionPerformed(ActionEvent e) {
      int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
      if (i != -1) {
         tabbedPane.remove(i);
      }
   }

   private static class TabCloseButton extends JButton {
      private class TabCloseButtonMouseListener implements MouseListener {
         public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
         }

         public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
         }

         public void mousePressed(MouseEvent e) {
         }

         public void mouseReleased(MouseEvent e) {
         }

         public void mouseClicked(MouseEvent e) {
         }
      }

      /** Creates a new tab close button */
      public TabCloseButton() {
         int size = 17;
         setPreferredSize(new Dimension(size, size));
         setToolTipText("Close this tab");

         setUI(new BasicButtonUI()); //Make the button looks the same for all Laf's

         setContentAreaFilled(false); //Make it transparent

         setFocusable(false);
         setBorder(BorderFactory.createEtchedBorder());
         setBorderPainted(false);

         //Making nice rollover effect
         addMouseListener(new TabCloseButtonMouseListener());
         setRolloverEnabled(true);
      }

      /** we don't want to update UI for this button */
      @Override
      public void updateUI() {
      }

      //paint the cross
      @Override
      protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g.create();

         //shift the image for pressed buttons
         if (getModel().isPressed()) g2.translate(1, 1);

         g2.setStroke(new BasicStroke(2));

         if (getModel().isRollover()) g2.setColor(Color.MAGENTA);
         else g2.setColor(Color.BLACK);

         int delta = 6;
         g2.drawLine(delta, delta, getWidth() - delta, getHeight() - delta);
         g2.drawLine(getWidth() - delta, delta, delta, getHeight() - delta);

         g2.dispose();
      }
   }
}




