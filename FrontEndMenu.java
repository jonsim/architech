import java.net.URL;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 *
 */
public class FrontEndMenu extends JMenuBar implements ActionListener {

   private Main main;
   private JMenuItem save, saveAs, helpContents, undo, fullScreen, tweaker;

   private void addFileMenu() {
      JMenu menu = new JMenu("File");
      menu.setMnemonic(KeyEvent.VK_F);
      menu.getAccessibleContext().setAccessibleDescription("File option menu");

      save = new JMenuItem("Save", KeyEvent.VK_S);
      save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
      save.getAccessibleContext().setAccessibleDescription("Save any changes");
      menu.add(save);

      saveAs = new JMenuItem("Save As", KeyEvent.VK_E);
      saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
      saveAs.getAccessibleContext().setAccessibleDescription("Save as a new file");
      menu.add(saveAs);

      this.add(menu);
   }

   private void addEditMenu() {
      JMenu menu;

      menu = new JMenu("Edit");
      menu.setMnemonic(KeyEvent.VK_E);
      menu.getAccessibleContext().setAccessibleDescription("Edit option menu");

      undo = new JMenuItem("Undo", KeyEvent.VK_Z);
      undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
      undo.getAccessibleContext().setAccessibleDescription("Undo the last action");
      menu.add(undo);

      this.add(menu);
   }

   private void addViewMenu() {
      JMenu menu;

      menu = new JMenu("View");
      menu.setMnemonic(KeyEvent.VK_V);
      menu.getAccessibleContext().setAccessibleDescription("View option menu");

      fullScreen = new JMenuItem("Full Screen", KeyEvent.VK_F11);
      fullScreen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
      fullScreen.getAccessibleContext().setAccessibleDescription("Display window in full screen");
      menu.add(fullScreen);

      this.add(menu);
   }

   private void addCustomisationMenu() {
      JMenu menu;

      menu = new JMenu("Customisation");
      menu.setMnemonic(KeyEvent.VK_C);
      menu.getAccessibleContext().setAccessibleDescription("Customisation option menu");

      tweaker = new JMenuItem("Model Tweaker", KeyEvent.VK_T);
      tweaker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
      tweaker.getAccessibleContext().setAccessibleDescription("Show the model tweaker");
      menu.add(tweaker);

      this.add(menu);
   }

   private void addHelpMenu() {
      JMenu menu;

      menu = new JMenu("Help");
      menu.setMnemonic(KeyEvent.VK_H);
      menu.getAccessibleContext().setAccessibleDescription("Help option menu");

      helpContents = new JMenuItem("Help Contents", KeyEvent.VK_H);
      helpContents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
      helpContents.getAccessibleContext().setAccessibleDescription("Show Help Contents");
      menu.add(helpContents);

      this.add(menu);
   }

   FrontEndMenu(Main main) {
      this.main = main;

      addFileMenu();
      save.addActionListener(this);
      saveAs.addActionListener(this);

      addEditMenu();
      undo.addActionListener(this);

      addViewMenu();
      fullScreen.addActionListener(this);

      addCustomisationMenu();
      tweaker.addActionListener(this);

      addHelpMenu();
      helpContents.addActionListener(this);
   }

   /** Invoked when an action occurs. */
   public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();

      if (source == save) {
         System.out.println("Save...");

      } else if (source == saveAs) {
         System.out.println("Save As...");

      } else if (source == helpContents) {
         System.out.println("Help Contents...");

      } else if (source == undo) {
         System.out.println("Undo...");

      } else if (source == fullScreen) {
         System.out.println("Full Screen...");
         
      } else if (source == tweaker) {
         System.out.println("Model Tweaker...");
         
      } else {
         Main.showFatalExceptionTraceWindow(
                 new Exception("BUG: Action ocurred with unexpected source (" + e.getSource().toString() + ")"));
      }
   }
}
