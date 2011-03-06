import java.awt.*;

/**
 *
 * @author James
 */
public class HandlerEdgeCurve {
   private final Coords coords;

   private Edge edge = null;
   private final Point revert = new Point();

   HandlerEdgeCurve(Coords coords) {
      if (coords == null) throw new IllegalArgumentException("null coords");
      this.coords = coords;
   }

   public void start(Point p) {
      if (edge != null) {
         // already started
         System.err.println("START CALLED WHILST RUNNING");
      }

      edge = coords.ctrlAt(p);

      if (edge != null) revert.setLocation(edge.getCtrlX(), edge.getCtrlY());
   }

   public void middle(Point p) {
      if (edge == null) return;

      coords.setEdgeCtrl(edge, p);

      // I think isCollisionVertex also is true if the line collides with anything
      // if it doesn't this should check the line, theres no point checking vertex
      // ends as they will not be changing by moving the ctrl point
      boolean isCollisionVertex = coords.detectVertexCollisions(edge.getV1())
                               || coords.detectVertexCollisions(edge.getV2());

      if (!isCollisionVertex) {
         revert.setLocation(p);
      }
   }

   public void stop(Point p) {
      if (edge == null) return;

      // move the ctrl point to its final position
      // check for line intersection, if so reset to the last known revert point
      boolean isCollisionVertex = coords.detectVertexCollisions(edge.getV1())
                               || coords.detectVertexCollisions(edge.getV2());

      if (isCollisionVertex) coords.setEdgeCtrl(edge, revert);
      else coords.setEdgeCtrl(edge, p);

      edge = null;
   }
}
