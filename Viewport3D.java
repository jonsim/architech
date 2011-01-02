import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Provides a canvas (with getCanvas()) containing the 3d window */
public class Viewport3D implements ActionListener {

   private ThreeD canvasApplication;
   private Main main;

   Viewport3D(Main main) {
      this.main = main;

//add clumsy 3D Window
      // create new JME appsettings
      AppSettings settings = new AppSettings(true);
      settings.setWidth(640);
      settings.setHeight(480);
      // create new canvas application
      canvasApplication = new ThreeD();
      canvasApplication.setSettings(settings);
      canvasApplication.createCanvas(); // create canvas!

      // Fill Swing window with canvas and swing components
      JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
      ctx.setSystemListener(canvasApplication);
      ctx.getCanvas().setPreferredSize(new Dimension(640, 480));

      // add JME canvas
      //panel.add(ctx.getCanvas());

      //add update button is done in DesignButtons (couldn't cleanly/easily get
      //3d to resize properly if the button was in the same area..)

      canvasApplication.startCanvas();

      // By now FrontEnd has displayed 3D preview window including JME canvas
   }

   /** Doesn't quite do it yet, perhaps the ThreeD class has a method to stop? */
   public void shutdown3D() {
      canvasApplication.stop();
   }

   /** FrontEnd will put this in the main window */
   public Canvas getCanvas() {
      JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
      return ctx.getCanvas();
   }

   /** Called by the update button, which is in DesignButtons.java */
   @Override
   public void actionPerformed(ActionEvent e) {
      // TODO Auto-generated method stub
      if (e.getActionCommand().equals("update")) {
         canvasApplication.clearall();
         canvasApplication.updateroot();
         canvasApplication.addedges(main.coordStore.getEdges());
      }
   }
}
