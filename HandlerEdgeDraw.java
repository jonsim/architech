import java.awt.*;

/**
 *
 * @author James
 */
public class HandlerEdgeDraw {
   private final Coords coords;
   
   private Edge edge = null;
   private final Point revertV2 = new Point();
   private boolean isCollided = false;

   HandlerEdgeDraw(Coords coords) {
      if (coords == null) throw new IllegalArgumentException("null coords");
      this.coords = coords;
   }

   public void start(Point p, boolean snapToGrid) {
      if (edge != null) {
         // already started
         System.err.println("START CALLED WHILST RUNNING");
      }

      if (coords.edgeFurnitureCollision(p.getX(), p.getY())) {
         System.err.println("DRAG EDGE STARTING OVER FURNITURE");
         return;
      }

      Coords.Vertex v = new Coords.Vertex(p.x, p.y, 0);
      edge = coords.newEdge(v, v, snapToGrid);

      revertV2.setLocation(edge.getV2().getX(), edge.getV2().getY());

      isCollided = false;
   }

   public void middle(Point p, boolean snapToAxis, boolean snapToGrid) {
      if (edge == null) return;

      float newX = p.x;
      float newY = p.y;

      if (snapToAxis) {
         Coords.Vertex origin = edge.getV1();

         float hrizDifference = Math.abs(origin.getX() - newX);
         float vertDifference = Math.abs(origin.getY() - newY);

         if (hrizDifference > vertDifference) {
            newY = origin.getY();
         } else {
            newX = origin.getX();
         }
      }

      coords.vertexMoveOrSplit(edge, false, newX, newY, 0, snapToGrid);

      // check if the line is over a furniture item
      isCollided = coords.detectVertexCollisions(edge.getV2());

      // if its not over a furniture item set a new valid revert point
      if (!isCollided) {
         revertV2.setLocation(edge.getV2().getX(), edge.getV2().getY());
      }
   }

   public void stop(Point p, boolean snapToGrid) {
      if (edge == null) return;

      coords.mergeVertices(edge.getV2(), p.x, p.y, 0, snapToGrid);

      // check if the line is over a furniture item, if it is revert if possible
      // to the latest otherwise delete.
      isCollided = coords.detectVertexCollisions(edge.getV2());

      if (isCollided) {
         // revert
         coords.set(edge.getV2(), revertV2.x, revertV2.y, 0, false);
         isCollided = false;
      }

      // delete length 0 lines
      if (edge.getV1() == edge.getV2()) coords.delete(edge);
      
      edge = null;
   }

   public Edge getEdge() {
      return edge;
   }

   public boolean isCollided() {
      return isCollided;
   }
}
