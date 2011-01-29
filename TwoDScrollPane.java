
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D.*;
import java.io.*;

/** Contains TwoDScrollPane, TwoDViewport, TwoDPanel, TwoDDropListener */
public class TwoDScrollPane extends JScrollPane {
   private TwoDPanel twoDPanel;

   /** Sets up all the classes mentioned above in the one ScrollPane */
   TwoDScrollPane(File file, String nameIfNullFile, DesignButtons designButtons) throws Exception {
      super(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      twoDPanel = new TwoDPanel(file, nameIfNullFile, designButtons);
      setViewportView(twoDPanel);
   }
   
   /** Subclasses may override this method to return a subclass of JViewport.  */
   @Override
   protected JViewport createViewport() {
      return new TwoDViewport();
   }

   /** Public getter for the 3D view */
   public Coords getCoords() {
      return twoDPanel.getCoords();
   }

   /** Requests the focus be on the associated underlying JPanel */
   public void requestFocusToPanel() {
      twoDPanel.requestFocus();
      twoDPanel.repaint();
   }


   /** Overrides 2 JViewport methods in order to help zoom work right */
   private class TwoDViewport extends JViewport {
      /** Converts a size in pixel coordinates to view coordinates. */
      @Override
      public Dimension toViewCoordinates(Dimension size) {
         // calculate the difference between normal un-scaled size and scaled size
         double scaleX = twoDPanel.getZoomScale();
         double scaleY = twoDPanel.getZoomScale();
         
         return super.toViewCoordinates(size);
      }

      /** Converts a point in pixel coordinates to view coordinates. */
      @Override
      public Point toViewCoordinates(Point p) {
         // calculate the difference between normal un-scaled size and scaled size
         double scaleX = twoDPanel.getZoomScale();
         double scaleY = twoDPanel.getZoomScale();
         
         return super.toViewCoordinates(p);
      }
   }
}
