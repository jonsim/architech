import javax.swing.*;
import javax.swing.border.EtchedBorder;

import java.awt.*;

/** Starts the program running and catches "few!" uncaught exceptions gracefully */
public class Main {
   public static final boolean disable3D = false;

   public FrontEnd frontEnd;
   public ObjectBrowser objectBrowser;
   public ObjectButtons objectButtons;
   public Viewport3D viewport3D;

   /** Does the business making other classes and remembering their pointers. Be
    *  careful editing the order things are created here, to avoid race conditions */
   Main() {
      viewport3D = disable3D ? new Viewport3DEmpty(this) : new Viewport3D(this);
      objectButtons = new ObjectButtons(this);
      objectBrowser = new ObjectBrowser(this);
      frontEnd = new FrontEnd(this);
   }

   /** Starts everything in the program running */
   private void run() {
	   //ENABLE/DISABLE SPLASH SCREEN
	   boolean splash_enabled = false;
	   if(splash_enabled){
	   JFrame splash = new JFrame();
       splash.setTitle("Splash Screen");
       splash.setResizable(false);
       splash.setSize(884, 457);
       splash.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
       splash.setLocationRelativeTo(null);
       splash.dispose();
       splash.setUndecorated(true);
       splash.setVisible(true);
       Image ic = FrontEnd.getImage(this, "img/frontend/icon.png");
       splash.setIconImage(ic);
         Image imag = FrontEnd.getImage(this, "img/frontend/logo.png");
         Image newimg = imag.getScaledInstance( 884, 457,  java.awt.Image.SCALE_SMOOTH ) ;  
         ImageIcon icon = new ImageIcon( newimg );
         JLabel piclabel = new JLabel(icon);
         piclabel.setPreferredSize(new Dimension(884,457));
         piclabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
         JPanel content = new JPanel();
         content.add(piclabel);
         splash.add(content);
         splash.pack();
         content.revalidate();                   
         try{Thread.sleep(2000);}
         catch (InterruptedException ie){}               
         frontEnd.display();
         splash.setVisible(true);
         splash.dispose();}
	   else{frontEnd.display();}
   }

   /** Sets the default look and feel. (must be done before anything else)
    * Runs the program. Catches any un-handled exceptions and displays them. */
   public static void main(String[] args) {
      try {
         setSystemLookAndFeel();
         Main program = new Main();
         program.run();
         
      } catch (Exception e) {
         showFatalExceptionTraceWindow(e);
      }
   }

   /** Shows a window with the exception details inside.
    * Blocks until user presses OK, then calls exit(0) */
   public static void showFatalExceptionTraceWindow(Exception e) {
      if (e == null) {
         e = new Exception("Null Exception Given");
      }

      /* Make a custom stack trace in a string to look pretty much like the normal one */
      String trace;
      if (e.getMessage() != null) {
         trace = e.getClass().getName() + ": " + e.getMessage();
      } else {
         trace = e.getClass().getName() + ": No exception message (null message)";
      }

      StackTraceElement[] traceArray = e.getStackTrace();
      if (traceArray == null) {
         trace += "\n    Unknown stack trace (null trace array)";
      } else {
         for (StackTraceElement traceElement : traceArray) {
            trace += "\n    at " + traceElement;
         }
      }

      /* Make the exception details box */
      JTextArea traceArea = new JTextArea(trace, 25, 70);
      traceArea.setEditable(false);
      traceArea.setMargin(new Insets(10, 10, 10, 10));
      JScrollPane traceScroller = new JScrollPane(traceArea);
      traceScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

      /* Make the writing above the exception details box */
      JPanel apology = new JPanel(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.weightx = 0.5;

      c.gridy = 0;
      apology.add(new JLabel("Sorry, an unhandled exception has occurred."), c);

      c.gridy = 1;
      c.insets = new Insets(0, 0, 12, 0);
      apology.add(new JLabel("The program has to close."), c);
      c.insets = new Insets(0, 0, 0, 0);

      c.gridy = 2;
      apology.add(new JLabel("(HINT: select the error text and ctrl-V to copy it)"), c);

      /* add the exception details box */
      c.gridy = 3;
      apology.add(traceScroller, c);

      JOptionPane.showMessageDialog(
              null,
              apology,
              "Fatal Exception",
              JOptionPane.ERROR_MESSAGE);

      System.exit(0);
   }

   /** There is a chance this won't work but who cares if the UI looks shit */
   private static void setSystemLookAndFeel() {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (UnsupportedLookAndFeelException e) {
         /* Who cares? */
      } catch (ClassNotFoundException e) {
         /* Who cares? */
      } catch (InstantiationException e) {
         /* Who cares? */
      } catch (IllegalAccessException e) {
         /* Who cares? */
      }
      
      try {
         System.setProperty("apple.laf.useScreenMenuBar", "true");
         System.setProperty("com.apple.mrj.application.apple.menu.about.name", FrontEnd.WINDOW_TITLE);
      } catch (SecurityException e) {
         /* Who cares? */
      } catch (NullPointerException e) {
         /* Who cares? */
      } catch (IllegalArgumentException e) {
         /* Who cares? */
      }
   }
}
