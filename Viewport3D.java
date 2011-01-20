import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Provides a canvas (with getCanvas()) containing the 3d window */
public class Viewport3D implements ActionListener {
   private ArchApp canvasApplication;
   private Main main;

   /** Used by Viewport3DEmpty to disable 3D */
   Viewport3D() {
   }

   Viewport3D(Main main) {
      this.main = main;

//add clumsy 3D Window
      // create new JME appsettings
      AppSettings settings = new AppSettings(true);
      settings.setWidth(640);
      settings.setHeight(480);
      // create new canvas application
      canvasApplication = new ArchApp();
      canvasApplication.setSettings(settings);
      canvasApplication.createCanvas(); // create canvas!

      // Fill Swing window with canvas and swing components
      JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
      ctx.setSystemListener(canvasApplication);
      ctx.getCanvas().setPreferredSize(new Dimension(640, 480));

      // add JME canvas - FrontEnd will call getCanvas() and put it in the window

      //add update button is done in DesignButtons (couldn't cleanly/easily get
      //3d to resize properly if the button was in the same area..)

      canvasApplication.startCanvas();

      // By now FrontEnd has displayed 3D preview window including JME canvas
   }

   /** Nicely disposes of the 3D stuff so that everything can close without exit(0) */
   public void shutdown3D() {
      canvasApplication.stop();
   }

   /** FrontEnd will put this in the main window */
   public Canvas getCanvas() {
      JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
      return ctx.getCanvas();
   }

   public void tabChanged(Coords coords) {
      
   }

   /** Called by the update button, which is in DesignButtons.java */
   @Override
   public void actionPerformed(ActionEvent e) {
      // TODO Auto-generated method stub
      if (e.getActionCommand().equals("update")) {
         TwoDScrollPane currentTab = main.frontEnd.getCurrentTab();
         if (currentTab != null) {
            canvasApplication.clearall();
            canvasApplication.updateroot();
            canvasApplication.addedges(currentTab.getCoords().getEdges());
         }
         
      }
   }
}
