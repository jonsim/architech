
import javax.swing.*;
import java.awt.*;

/** Starts the program running and catches "few!" uncaught exceptions gracefully */
public class Main {

   public FrontEnd frontEnd;
   public FrontEndMenu frontEndMenu;

   public ObjectBrowser objectBrowser;
   public ObjectButtons objectButtons;

   public DesignButtons designButtons;
   public Viewport2D viewport2D;
   
   public Viewport3D viewport3D;

   public Coords coordStore;

   /** Does the business making other classes and remembering their pointers. Be
    *  careful editing the order things are created here, to avoid race conditions */
   Main() {
      coordStore = new Coords();

      designButtons = new DesignButtons(this);
      objectButtons = new ObjectButtons(this);

      viewport2D = new Viewport2D(this);
      viewport3D = new Viewport3D(this);
      objectBrowser = new ObjectBrowser(this);

      frontEndMenu = new FrontEndMenu(this);
      frontEnd = new FrontEnd(this);
   }

   /** Starts everything in the program running */
   private void run() {
      frontEnd.display();
      viewport2D.getScrollPane().setPreferredSize(new Dimension(2000,1000));
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
