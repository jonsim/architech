import java.awt.*;
import java.util.*;

/**
 *
 * @author James
 */
public class HandlerVertexMove {
   private final Coords coords;
   
   private Coords.Vertex v = null;
   private final Point revert = new Point();
   private boolean isCollided = false;

   HandlerVertexMove(Coords coords) {
      if (coords == null) throw new IllegalArgumentException("null coords");
      this.coords = coords;
   }

   public void start(Point p) {
      if (v != null) {
         // already started
         System.err.println("START CALLED WHILST RUNNING");
      }

      v = coords.vertexAt(p);

      if (v != null) {
         revert.setLocation(v.getX(), v.getY());
      }

      isCollided = false;
   }

   public void middle(Point p, boolean snapToGrid) {
      if (v == null) return;

      coords.set(v, p.x, p.y, 0, snapToGrid);

      isCollided = coords.detectVertexCollisions(v);

      // update revert point if no collision, so it doesn't jump back too far
      if (!isCollided) {
         revert.setLocation(v.getX(), v.getY());
      }
   }

   public void stop(Point p, boolean snapToGrid) {
      if (v == null) return;

      // put it at the right xy-coordinates but different z so it wont be considered to be at that point <-- woah sketchy!
      /* coords.set(v, p.x, p.y, 0, snapToGrid); <- SILLY */
      coords.mergeVertices(v, p.x, p.y, 0, snapToGrid);
      /* coords.set(v, p.x, p.y, 0, snapToGrid); <- SILLY */

      isCollided = coords.detectVertexCollisions(v);

      if (isCollided) {
         //reset to last known
         coords.set(v, (float) revert.getX(), (float) revert.getY(), 0, false);
         isCollided = false;
      }

      // Delete length 0 edges
      ListIterator<Edge> edgeIterator = v.getEdges().listIterator();
      while (edgeIterator.hasNext()) {
         Edge edge = edgeIterator.next();
         if (edge.getV1() == edge.getV2()) {
            coords.delete(edge);
         }
      }

      v = null;
   }

   public Coords.Vertex getVertex() {
      return v;
   }

   public boolean isCollided() {
      return isCollided;
   }
}
