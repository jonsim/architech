import java.awt.*;
import java.util.*;

/**
 *
 * @author James
 */
public class HandlerEdgeDraw {
   private final Coords coords;
   
   private Edge edge = null;
   private boolean isCollided = false;
   private float startX;
   private float startY;
   private LinkedList<Edge> edgeList = new LinkedList<Edge>();

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

      Coords.Vertex mergeCheck = coords.vertexAt(p);
      Coords.Vertex v;

      if(mergeCheck == null) {
          v = new Coords.Vertex(p.x, p.y, 0);
          startX = p.x;
          startY = p.y;
      } else {
          v = new Coords.Vertex(mergeCheck.getX(), mergeCheck.getY(), 0);
          startX = mergeCheck.getX();
          startY = mergeCheck.getY();
      }
      edge = coords.newEdge(v, v, snapToGrid);

      isCollided = false;
      edgeList.clear();
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
	  coords.findEdgeSplits(edge, false);

      // check if the line is over a furniture item
      isCollided = coords.detectVertexCollisions(edge.getV2());
   }

   public void stop(Point p, boolean snapToAxis, boolean snapToGrid) {
      if (edge == null) return;

      float newX = p.x;
      float newY = p.y;

      if(snapToAxis) {
         Coords.Vertex origin = edge.getV1();

         float hrizDifference = Math.abs(origin.getX() - newX);
         float vertDifference = Math.abs(origin.getY() - newY);

         if (hrizDifference > vertDifference) {
            newY = origin.getY();
         } else {
            newX = origin.getX();
         }
      }

      coords.vertexMoveOrSplit(edge, false, newX, newY, -1000, snapToGrid);

      // check if the line is over a furniture item, if it is revert if possible
      // to the latest otherwise delete.
      isCollided = coords.detectVertexCollisions(edge.getV2());

      if (!isCollided) {
         Point mergedPoint;
         mergedPoint = coords.mergeVertices(edge.getV1(), startX, startY, 0, snapToGrid);
         if(mergedPoint.x != -1 && mergedPoint.y != 1) coords.set(edge.getV1(), mergedPoint.x, mergedPoint.y, 0, snapToGrid);
         mergedPoint = coords.mergeVertices(edge.getV2(), newX, newY, 0, snapToGrid);
         if(mergedPoint.x != -1 && mergedPoint.y != 1) coords.set(edge.getV2(), mergedPoint.x, mergedPoint.y, 0, snapToGrid);
         else coords.set(edge.getV2(), newX, newY, 0, snapToGrid);
         coords.findEdgeSplits(edge, true);
         edgeList = coords.getSplitEdges();
	 coords.splitEdges(edge, null);
      } else {
         // revert
         coords.delete(edge);
         isCollided = false;
         edge = null;
         return;
      }

      // delete length 0 lines
      if (edge.getV1() == edge.getV2() || edge.length() == 0) coords.delete(edge);

      edge = null;
   }

   public Edge getEdge() {
      return edge;
   }

   public boolean isCollided() {
      return isCollided;
   }

   public ArrayList<Coords.Vertex> getVertexList() {
       ArrayList<Coords.Vertex> vList = new ArrayList<Coords.Vertex>();
       int i = 0;
       while(i < edgeList.size()) {
           vList.add(edgeList.get(i).getV1());
           vList.add(edgeList.get(i).getV2());
           i++;
       }
       edgeList.clear();
       return vList;
   }
}
