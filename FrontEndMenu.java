import java.awt.event.*;
import java.awt.Toolkit;
import javax.swing.*;
import java.util.Arrays;
import javax.swing.JOptionPane;

/** Creates the menu that appears at the top of the main window */
public class FrontEndMenu extends JMenuBar implements ActionListener {
   private FrontEnd frontEnd;
   private JMenuItem create, open, save, saveAs, saveCopyAs, helpContents;//, undo, fullScreen, tweaker;
   private final static String url = "https://sites.google.com/site/architechsoftware/";

   FrontEndMenu(FrontEnd frontEnd) {
      this.frontEnd = frontEnd;

      addFileMenu();
      create.addActionListener(this);
      open.addActionListener(this);
      save.addActionListener(this);
      saveAs.addActionListener(this);
      saveCopyAs.addActionListener(this);

      //addEditMenu();
      //undo.addActionListener(this);

      //addViewMenu();
      //fullScreen.addActionListener(this);

      //addCustomisationMenu();
      //tweaker.addActionListener(this);

      addHelpMenu();
      helpContents.addActionListener(this);
   }

   private void addFileMenu() {
      JMenu menu;
      int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

      menu = new JMenu("File");
      menu.setMnemonic(KeyEvent.VK_F);
      menu.getAccessibleContext().setAccessibleDescription("File option menu");

      create = new JMenuItem("New", KeyEvent.VK_N);
      create.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutMask));
      create.getAccessibleContext().setAccessibleDescription("Create a blank file");
      menu.add(create);

      open = new JMenuItem("Open", KeyEvent.VK_O);
      open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutMask));
      open.getAccessibleContext().setAccessibleDescription("Open a file");
      menu.add(open);

      save = new JMenuItem("Save", KeyEvent.VK_S);
      save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutMask));
      save.getAccessibleContext().setAccessibleDescription("Save any changes");
      menu.add(save);

      saveAs = new JMenuItem("Save As", KeyEvent.VK_E);
      saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutMask));
      saveAs.getAccessibleContext().setAccessibleDescription("Save as a new file");
      menu.add(saveAs);

      saveCopyAs = new JMenuItem("Save Copy As", KeyEvent.VK_C);
      saveCopyAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutMask));
      saveCopyAs.getAccessibleContext().setAccessibleDescription("Save a copy of this file");
      menu.add(saveCopyAs);

      this.add(menu);
   }
/*
   private void addEditMenu() {
      JMenu menu;
      int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

      menu = new JMenu("Edit");
      menu.setMnemonic(KeyEvent.VK_E);
      menu.getAccessibleContext().setAccessibleDescription("Edit option menu");

      undo = new JMenuItem("Undo", KeyEvent.VK_Z);
      undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutMask));
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
      int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

      menu = new JMenu("Customisation");
      menu.setMnemonic(KeyEvent.VK_C);
      menu.getAccessibleContext().setAccessibleDescription("Customisation option menu");

      tweaker = new JMenuItem("Model Tweaker", KeyEvent.VK_T);
      tweaker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, shortcutMask));
      tweaker.getAccessibleContext().setAccessibleDescription("Show the model tweaker");
      menu.add(tweaker);

      this.add(menu);
   }
*/
   private void addHelpMenu() {
      JMenu menu;
      int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

      menu = new JMenu("Help");
      menu.setMnemonic(KeyEvent.VK_H);
      menu.getAccessibleContext().setAccessibleDescription("Help option menu (online)");

      helpContents = new JMenuItem("Help Contents (Online)", KeyEvent.VK_H);
      helpContents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, shortcutMask));
      helpContents.getAccessibleContext().setAccessibleDescription("Show Help Contents (Online)");
      menu.add(helpContents);

      this.add(menu);
   }

   /** Invoked when an action occurs. */
   public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();

      if (source == create) {
         frontEnd.newCoords();
      } else if (source == open) {
         frontEnd.openCoords();
      } else if (source == save) {
         frontEnd.currentCoordsSave();
      } else if (source == saveAs) {
         frontEnd.currentCoordsSaveAs();
      } else if (source == saveCopyAs) {
         frontEnd.currentCoordsSaveCopyAs();
      } else if (source == helpContents) {

         /*******************************************************************
          * BELOW CODE SUPPLIED FROM http://www.centerkey.com/java/browser/ *
          *******************************************************************/
         final String[] browsers = { "google-chrome", "firefox", "opera",
            "epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla" };
      
         try {
            //attempt to use Desktop library from JDK 1.6+
            Class<?> d = Class.forName("java.awt.Desktop");
            d.getDeclaredMethod("browse", new Class[] {java.net.URI.class}).invoke(
               d.getDeclaredMethod("getDesktop").invoke(null), new Object[] {java.net.URI.create(url)});
            //above code mimicks: java.awt.Desktop.getDesktop().browse()
         } catch (Exception ignore) {
            //library not available or failed
            String osName = System.getProperty("os.name");
            try {
               if (osName.startsWith("Mac OS")) {
                  Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
                     "openURL", new Class[] {String.class}).invoke(null, new Object[] {url});
               } else if (osName.startsWith("Windows")) {
                  Runtime.getRuntime().exec( "rundll32 url.dll,FileProtocolHandler " + url);
               } else {
                  //assume Unix or Linux
                  String browser = null;
                  for (String b : browsers) {
                     if (browser == null && Runtime.getRuntime().exec(
                           new String[] {"which", b}).getInputStream().read() != -1) {
                        Runtime.getRuntime().exec(new String[] {browser = b, url});
                     }
                  }
                  if (browser == null) {
                     throw new Exception(Arrays.toString(browsers));
                  }
               }
            } catch (Exception err) {
               JOptionPane.showMessageDialog(null, "Error attempting to launch web browser\n" + err.toString());
            }
         }


      //} else if (source == undo) {
      //   System.out.println("Undo...");

      //} else if (source == fullScreen) {
      //   System.out.println("Full Screen...");

      //} else if (source == tweaker) {
      //   System.out.println("Model Tweaker...");

      } else {
         Main.showFatalExceptionTraceWindow(
                 new Exception("BUG: Action ocurred with unexpected source (" + e.getSource().toString() + ")"));
      }
   }
}
