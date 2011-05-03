import java.awt.*;
import java.util.LinkedList;
import java.util.ArrayList;

/**
 *
 * @author James
 */
public class HandlerEdgeCurve {
   private final Coords coords;

   private Edge edge = null;
   private final Point revert = new Point();
   private boolean isCollided = false;
   private LinkedList<Edge> edgeList = new LinkedList<Edge>();


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
	  
	  isCollided = false;
          edgeList.clear();
   }

   public void middle(Point p) {
      if (edge == null) return;

      coords.setEdgeCtrl(edge, p);
	  coords.findEdgeSplits(edge, false);

      // I think isCollisionVertex also is true if the line collides with anything
      // if it doesn't this should check the line, theres no point checking vertex
      // ends as they will not be changing by moving the ctrl point
      isCollided = coords.detectVertexCollisions(edge.getV1())
                   || coords.detectVertexCollisions(edge.getV2());
   }

   public void stop(Point p) {
      if (edge == null) return;

      // move the ctrl point to its final position
      // check for line intersection, if so reset to the last known revert point
      isCollided = coords.detectVertexCollisions(edge.getV1())
                   || coords.detectVertexCollisions(edge.getV2());

      if (isCollided) coords.setEdgeCtrl(edge, revert);
      else {
		coords.setEdgeCtrl(edge, p);
                coords.findEdgeSplits(edge, true);
                edgeList = coords.getSplitEdges();
		coords.splitEdges(edge, null);
	  }

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
