
import java.awt.*;
import java.util.*;

/**
 *
 * @author Brent
 */
public class HandlerVertexSelect {

    private final Coords coords;
    private final ObjectBrowser objectBrowser;
    private ArrayList<Coords.Vertex> selectedArray = new ArrayList<Coords.Vertex>();
    private ArrayList<Edge> wallEdges = new ArrayList<Edge>();
    private int selCount = 0;

    HandlerVertexSelect(Coords coords, ObjectBrowser objectBrowser) {
        if (coords == null) {
            throw new IllegalArgumentException("null coords");
        }
        this.coords = coords;
        this.objectBrowser = objectBrowser;
    }

    /** User has clicked (mouse released, doing it on mouse press is really hard
     *  as moving vertices also uses mouse press and the select tool so its a bit
     *  ambiguous! Mouse release was a lot easier) */
    public void click(Point p) {
        boolean adjVertex = false;

        Coords.Vertex v = coords.vertexAt(p);

        System.out.println("click v=" + v);

        if (v != null) {
            // add the vertex
            if (selectedArray.contains(v) == false) {
                System.out.println("Vertex selected ....");
                if (selectedArray.size() == 0) {
                    selectedArray.add(v);
                }
                adjVertex = isAdjacent(v, selectedArray);
                System.out.println("adjVertex := " + adjVertex);
                if (adjVertex == true) {
                    for (int k = 0; k < selectedArray.size(); k++) {
                        System.out.println(selectedArray.get(k));
                    }
                }
            }
            //FOR DEBUGGING - CAN BE REMOVED
            printEdges();
        } else {
            // reset
            //selectedArray.clear();
            //wallEdges.clear();
            //selCount = 0;
            //objectBrowser.wall = false;
            //objectBrowser.toReset();
        }

        if (selectedArray.size() > 1) {
            objectBrowser.toDecoration();
        }
    }

    //Checks if the selected vertex is adjacent to any of the vertices in the selected list and adds it.
    private boolean isAdjacent(Coords.Vertex v, ArrayList<Coords.Vertex> selectedVertices) {
        Edge[] e = coords.getEdges();
        boolean valid = false;
        if (selectedVertices.size() > 0) {
            for (int i = 0; i < selectedVertices.size(); i++) {
                for (int k = 0; k < e.length; k++) {
                    Coords.Vertex v1 = e[k].getV1();
                    Coords.Vertex v2 = e[k].getV2();
                    if ((v1 == v && v2 == selectedVertices.get(i)) || (v1 == selectedVertices.get(i) && v2 == v)) {
                        selectedArray.add(v);
                        wallEdges.add(e[k]);
                        valid = true;
                    }
                }
            }
        }
        if (valid == false) {
            if (selCount != 0) {
                System.out.println("Sorry. Invalid Vertex Selected . . . . .");
            }
        }
        selCount++;
        return valid;
    }

    //prints the list of edges. (for debugging)
    private void printEdges() {
        System.out.println("Edge List:");
        for (int k = 0; k < wallEdges.size(); k++) {
            System.out.println(wallEdges.get(k));
        }
    }

    public ArrayList<Coords.Vertex> getSelectedV() {
        return selectedArray;
    }

    public ArrayList<Edge> getSelectedE() {
        return wallEdges;
    }

    /** As your class handles selection it makes sense to have control of deletes :) */
    public void deleteSelected() {
       System.out.println("delete");

      for (int i=0; i<selectedArray.size();i++){
         coords.delete(selectedArray.get(i));
      }

      // clear arraylists
      selectedArray.clear();
      wallEdges.clear();
      selCount = 0;
      objectBrowser.wall = false;
      objectBrowser.toReset();
    }

    public void forgetSelectedVertices() {
        selectedArray.clear();
        wallEdges.clear();
    }
}
