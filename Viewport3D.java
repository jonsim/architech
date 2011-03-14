import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Provides a canvas (with getCanvas()) containing the 3d window */
public class Viewport3D implements CoordsChangeListener {
   private ArchApp canvasApplication;
   private Main main;

   /** Used by Viewport3DEmpty to disable 3D */
   Viewport3D() {
   }
   
   public ArchApp getapp(){
	   return canvasApplication;
   }

   Viewport3D(Main main) {
      this.main = main;

//add clumsy 3D Window
      // create new JME appsettings
      AppSettings settings = new AppSettings(true);
      settings.setWidth(640);
      settings.setHeight(480);
      // create new canvas application
      canvasApplication = new ArchApp(main);
      canvasApplication.setSettings(settings);
      Logger.getLogger("").setLevel(Level.SEVERE);
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
   
   public void remake3D ()
   {
	      AppSettings settings = new AppSettings(true);
	      settings.setWidth(640);
	      settings.setHeight(480);
	      canvasApplication = new ArchApp(main);
	      canvasApplication.setSettings(settings);
	      Logger.getLogger("").setLevel(Level.OFF);
	      canvasApplication.createCanvas();
	      JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
	      ctx.setSystemListener(canvasApplication);
	      ctx.getCanvas().setPreferredSize(new Dimension(640, 480));
	      canvasApplication.startCanvas();
   }

   /** Nicely disposes of the 3D stuff so that everything can close without exit(0) */
   public void shutdown3D() {
      canvasApplication.stop();
   }
   
   /** toggle day/night mode. this is almost certainly the wrong way to do this! */
   public void toggleDay()
   {
	   canvasApplication.toggleDay();
	   getCanvas().requestFocus();
   }

   /** FrontEnd will put this in the main window */
   public Canvas getCanvas() {
      JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
      return ctx.getCanvas();
   }

   /** coords can be null if there is no tab selected. This is synchronized as
    *  ArchApp might call tabChanged to do an initial full load, and it will stop
    *  both FrontEnd and ArchApp calling this method at the same time */
   public void tabChanged(Coords coords) {
      canvasApplication.tabChanged(coords);
   }

   /** Forgets about the edges in the coords. If the coords is added again using
    *  tabChanged, then it will recreate edges, so call when its not needed anymore */
   public void tabRemoved(Coords coords) {
      canvasApplication.tabRemoved(coords);
   }

   public void coordsChangeOccurred(CoordsChangeEvent e) {
      if (e.isEdgeRelated()) {
         Edge hasChanged = e.getEdgeChanges();

         switch (e.getChangeType()) {
            case CoordsChangeEvent.EDGE_ADDED:
               // add the new edge
               canvasApplication.addEdge(e.getSource(), hasChanged);
               break;
            case CoordsChangeEvent.EDGE_CHANGED:
               // update the edge location
               canvasApplication.updateEdgeChanged(e.getSource(), hasChanged);
               break;
            case CoordsChangeEvent.EDGE_REMOVED:
               // remove the edge
               canvasApplication.removeEdge(e.getSource(), hasChanged);
               break;
         }

      } else if (e.isFurnitureRelated()) {
         Furniture hasChanged = e.getFurnitureChanged();

         switch (e.getChangeType()) {
            case CoordsChangeEvent.FURNITURE_ADDED:
               // add the new furniture
               canvasApplication.addFurniture(e.getSource(), hasChanged);
               break;
            case CoordsChangeEvent.FURNITURE_CHANGED:
               // update the furniture location
               canvasApplication.updateFurnitureChanged(e.getSource(), hasChanged);
               break;
            case CoordsChangeEvent.FURNITURE_REMOVED:
               // remove the furniture
               canvasApplication.removeFurniture(e.getSource(), hasChanged);
               break;
         }
      }

      getCanvas().requestFocus(); // Stops you having to click to update the 3D (for lines & furniture)
   }
}
