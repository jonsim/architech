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
   private boolean hasMoved = false;
   private LinkedList<Edge> edgeList = new LinkedList<Edge>();

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
      hasMoved = false;
      edgeList.clear();
   }

   public void middle(Point p, boolean snapToGrid) {
      if (v == null) return;

      hasMoved = true;

      coords.set(v, p.x, p.y, 0, snapToGrid);
      coords.findEdgeSplits(v, false);

      isCollided = coords.detectVertexCollisions(v);
   }

   public void stop(Point p, boolean snapToGrid) {
      if (v == null || !hasMoved) return;

      isCollided = coords.detectVertexCollisions(v);

      if (!isCollided) {
         // put it at the right xy-coordinates but different z so it wont be considered to be at that point <-- woah sketchy! <-- sketchy perhaps... but awesome and solves the problem nonetheless
         coords.set(v, p.x, p.y, -1000, snapToGrid);
         Point mergedPoint;
         mergedPoint = coords.mergeVertices(v, p.x, p.y, 0, snapToGrid);
         if(mergedPoint.x != -1 && mergedPoint.y != 1) coords.set(v, mergedPoint.x, mergedPoint.y, 0, snapToGrid);
         else coords.set(v, p.x, p.y, 0, snapToGrid);
         coords.findEdgeSplits(v, true);
         edgeList = coords.getSplitEdges();
	 coords.splitEdges(null, v);
      } else {
         //reset to start
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
