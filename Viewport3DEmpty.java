import java.awt.Canvas;

/** Provides a canvas (with getCanvas()) containing the 3d window */
public class Viewport3DEmpty extends Viewport3D {

   private Canvas canvas = new Canvas();

   Viewport3DEmpty(Main main) {
      super();
   }

   /** Nicely disposes of the 3D stuff so that everything can close without exit(0) */
   @Override
   public void shutdown3D() {
   }

   /** FrontEnd will put this in the main window */
   @Override
   public Canvas getCanvas() {
      return canvas;
   }

   @Override
   public void tabChanged(Coords coords) {
   }

   @Override
   public void coordsChangeOccurred(CoordsChangeEvent e) {
   }
}
