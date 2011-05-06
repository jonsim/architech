import java.awt.*;
import java.util.*;
import java.io.*;
import java.awt.geom.*;

/** This is the class that holds all the coordinates, vertices, edges, planes
 *  etc. for one "design". If there are two designs loaded, i.e. in tabs, just
 *  make a new CoordStore() with the relevant vertices for the second design;
 *  
 */
public class Coords {

   /** This is inside the CoordStore as it needs access to some methods that
    *  should really be private to the Vertex. This way is a bit odd but much
    *  better in the long run prevention of bugs wise!
    *  
    */
   public static class Vertex {
      private int diameter = 10;
      private final Loc3f p = new Loc3f(0,0,0);
      private final LinkedList<Edge> edgeUses = new LinkedList<Edge>();
      private final Ellipse2D.Float topDownView = new Ellipse2D.Float();

      /** Convenience: Creates a blank, unused vertex at 0,0,0 */
      Vertex() {
         this(0,0,0);
      }

      /** Convenience: does no rounding, simply casts the doubles back down to float */
      Vertex(double x, double y, double z) {
         this((float) x, (float) y, (float) z);
      }

      /** Creates and initialises an unused vertex at the given coordinates */
      Vertex(float x, float y, float z) {
         set(x, y, z);
      }

      /** Sets this vertex's points to the new ones and update anything connected */
      private void set(float x, float y, float z) {
         p.set(x, y, z);
         recalcTopDownView();

         // update all the edges, simply calling the set() method will update them
         ListIterator<Edge> ite = edgeUses.listIterator();
         while (ite.hasNext()) {
            ite.next().recalcTopDownView();
         }
      }

      /** Adds the given object to the list of objects using this vertex */
      private void setUse(Edge e) {
         if (e == null) return;
         if (edgeUses.contains(e)) return; // already used by that object
         edgeUses.add(e);
      }

      /** Returns true if the list of objects using this vertex is not empty */
      public boolean isUsed() {
         if (edgeUses.size() > 0) return true;
         else return false;
      }

      /** Get the X component */
      public float getX() {
         return p.x();
      }

      /** Get the Y component */
      public float getY() {
         return p.y();
      }

      /** Get the Z component */
      public float getZ() {
         return p.z();
      }

      /** Recalculates the size and location of the topDownView representation */
      private void recalcTopDownView() {
         topDownView.setFrame(p.x() - diameter / 2, p.y() - diameter / 2, diameter, diameter);
      }

      /** Returns true if the vertex at its current diameter contains the point */
      public boolean contains(Point p) {
         Point temp = new Point();
         temp.setLocation(p.getX(), p.getY());
         return topDownView.contains(temp);
      }

      /** Draws the vertex at its current diameter, and the current graphics colour */
      public void paint(Graphics2D g2, int diameter) {
         this.diameter = diameter;
         recalcTopDownView();

         g2.fill(topDownView);
      }

      /** Takes all the edges that are "in use" by the given vertex and adds
       *  them to the current vertex (if not already added). It updates the
       *  vertex(s) remember by edges/objects etc. to this (the new) vertex */
      private void addUsesCutFrom(Vertex v) {
         if (v == null || this == v) return;

         ListIterator<Edge> vIte = v.edgeUses.listIterator();
         while (vIte.hasNext()) {
            Edge e = vIte.next();

            if (e.getV1() == v) {
               e.setV1(this, true);
            } else if (e.getV2() == v) {
               e.setV2(this, true);
            } else Main.showFatalExceptionTraceWindow(new Exception("Never Happen Case"));

            this.setUse(e);
         }

         v.edgeUses.clear();
      }

      /** Each Vertex remembers the Coords class that it was created with. This
       *  method returns that Coords instance */
      /* public Coords getOuterInstance() {
         return Coords.this;
      }*/

      /** Returns true iff the x,y,z coords of v match the parameters */
      public boolean equalsLocation(float x, float y, float z) {
         return p.x() == x && p.y() == y && p.z() == z;
      }

      /** Returns true iff the x,y,z coords of the given vertex match this
       *  vertex's coords */
      public boolean equalsLocation(Vertex v) {
         if (v == null) return false;
         return equalsLocation(v.p.x(), v.p.y(), v.p.z());
      }
	  
	  public LinkedList<Edge> getEdges() {
		 return edgeUses;
	  }

      /** @see Coords.Vertex.equalsLocation */
      @Override
      public boolean equals(Object obj) {
         if (!(obj instanceof Coords.Vertex)) return false;
         return equalsLocation((Coords.Vertex) obj);
      }

      /** hashCode is for only the location of this vertex, not edges using it etc. */
      @Override
      public int hashCode() {
         return p.hashCode();
      }
   }

   /*!-START OF COORDS--------------------------------------------------------*/
   private String associatedSaveName = null;
   private File associatedSave = null;
   private boolean saveRequired = false;
   
   private final LinkedList<Vertex> vertices = new LinkedList<Vertex>();
   private final LinkedList<Edge> edges = new LinkedList<Edge>();
   private final LinkedList<Furniture> furniture = new LinkedList<Furniture>();
   private LinkedList<Point> lineSplits = new LinkedList<Point>();
   private LinkedList<Edge> splitEdges = new LinkedList<Edge>();
   private LinkedList<Edge> rememberedEdges = new LinkedList<Edge>();
   private Furniture invalidDW = null;
   private static ObjectBrowser objectBrowser;
   private int gridWidth = 50; // makes grid lines at 0,30,60,...
  
   /** Creates a blank coordinate system */
   Coords(String associatedSaveName, ObjectBrowser ob) {
      this.associatedSaveName = associatedSaveName;
      this.objectBrowser = ob;
   }

   /** Blindly makes vertices and edges, there might be orphaned/dup vertices */
   Coords(File loadedFrom, float[][] vertices, int[][] edges, Furniture[] furniture, ObjectBrowser ob) throws IllegalArgumentException {
      if (loadedFrom == null || vertices == null || edges == null || furniture == null)
         throw new IllegalArgumentException("null argument");
      if (!loadedFrom.isFile()) {
         throw new IllegalArgumentException("File is a directory or it doesn't exist");
      }
      this.associatedSave = loadedFrom;
      this.associatedSaveName = loadedFrom.getName();
      this.objectBrowser = ob;

      Vertex[] vertexA = new Vertex[vertices.length];

      for (int i=0; i < vertices.length; i++) {
         if (vertices[i] == null || vertices[i].length != 3) {
            throw new IllegalArgumentException("Vertices array needs to be of size n by 3");
         }

         vertexA[i] = new Vertex(vertices[i][0], vertices[i][1], vertices[i][2]);
         this.vertices.add(vertexA[i]);
      }

      for (int i=0; i < edges.length; i++) {
         if (edges[i] == null || edges[i].length != 4) {
            throw new IllegalArgumentException("Edges array needs to be of size m by 2");
         }

         int v1Index = edges[i][0];
         int v2Index = edges[i][1];
         Point ctrl = new Point(edges[i][2], edges[i][3]);
         
         if (v1Index < 0 || v1Index >= vertexA.length || v2Index < 0 || v2Index >= vertexA.length) {
            throw new IllegalArgumentException("A given edge indexes a vertex that doesn't exist (OOB)");
         }

         Vertex v1 = vertexA[v1Index];
         Vertex v2 = vertexA[v2Index];

         Edge e = new Edge(v1, v2, ctrl);
         v1.setUse(e);
         v2.setUse(e);
         this.edges.add(e);

         //no point throwing events as noone can register as a listener, we're still in constructor!
      }

      for (Furniture f : furniture) {
         // check for collisions
         if( f.isDoorWindow() )
            addDoorWindow(f);
         else
            this.furniture.add(f);
      }
   }

   public void changegw(int val){
	   gridWidth = val;
   }
   
   /** Returns a string with the file name i.e. if the file is C:\hello.txt this
    *  will return hello.txt. Alternatively if the associated save file has not
    *  been set (this coords was not loaded from a file, it was created blank),
    *  then it will return the string that was given to the constructor */
   public String getAssociatedSaveName() {
      return associatedSaveName;
   }

   /** Adds the furniture item if it doesn't already exist */
   public void addFurniture(Furniture f) {
      if ( f == null || furniture.contains(f) ) return;
      furniture.add(f);
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.FURNITURE_ADDED, f));
   }

   /** Moves the furniture item to the new location */
   public void moveFurniture(Furniture f, Point newCenter) {
      if (f == null || !furniture.contains(f) ) return;
      f.set(newCenter);
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.FURNITURE_CHANGED, f));
   }

   public Edge getDoorWindowEdge(Furniture f) {
      ListIterator<Edge> ite = edges.listIterator();

      while( ite.hasNext() ) {
         Edge e = ite.next();
         if( e.containsDoorWindow(f) ) return e;
      }

      return null;
   }

   /** Adds the door/window if it doesn't already exist */
   public void addDoorWindow(Furniture f) {
      if( f == null || getDoorWindowEdge(f) != null ) return;

      ListIterator<Edge> ite = edges.listIterator();
      
      while( ite.hasNext() ) {
         Edge e = ite.next();
         if( e.isStraight() && furnitureWallIntersect(f, e) ) {
            f.setRotation( e.getRotation() );
            e.addDoorWindow(f);
            fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.DOORWINDOW_ADDED, f));
            fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, e));
            return;
         }
      }

      //if we've reached this point then the door/window isn't over an edge
      invalidDW = f;
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.DOORWINDOW_ADDED, f));
   }

   /** Moves the door/window to the new location */
   public void moveDoorWindow(Furniture f, Point newCenter) {
      if( f == null ) return;
      Edge e = getDoorWindowEdge(f);

      if( e == null ) {
         if( invalidDW == f )
            invalidDW = null;

         newCenter = snapToEdge(newCenter);
         f.set(newCenter);
         addDoorWindow(f);
         return;
      }

      //move to new position
      newCenter = snapToEdge(newCenter);
      f.set(newCenter);

      if( !furnitureWallIntersect(f, e) ) {
         //moved off the current edge so we remove from the current edge...
         e.deleteDoorWindow(f);
         fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, e));

         //...and add it to the new one
         addDoorWindow(f);
      }

      //still on the same edge
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.DOORWINDOW_CHANGED, f));
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, e));
    }
   
    public boolean detectVertexCollisions(Vertex v) {
        Edge e;
        Furniture f;
        ListIterator<Edge> edgeIterator = v.getEdges().listIterator();
        ListIterator<Furniture> furnitureIterator = furniture.listIterator();
        while (edgeIterator.hasNext()) {
            e = edgeIterator.next();
            while (furnitureIterator.hasNext()) {
                f = furnitureIterator.next();
                if(furnitureWallIntersect(f, e)) return true;
            }
            furnitureIterator = furniture.listIterator();
        }
	return false;
   }

   public boolean detectCollisions(Furniture f) {
       ListIterator<Edge> edgeIterator = edges.listIterator();
       while (edgeIterator.hasNext()) {
           Edge e = edgeIterator.next();
           if(furnitureWallIntersect(f, e)) return true;
       }
       ListIterator<Furniture> furnitureIterator = furniture.listIterator();
       while (furnitureIterator.hasNext()) {
           Furniture fur = furnitureIterator.next();
           if (fur != f) {
               if (rectangleIntersect(f, fur)) {
                   return true;
               }
           }
       }
       return false;
   }

   /** Checks for collisions with other doors/windows and whether it goes off the end of an edge */
   public boolean doorWindowInvalidPosition(Furniture f) {
      Edge e = getDoorWindowEdge(f);

      if( e == null )
         return true;

      Furniture[] dws = e.getDoorWindow();
      
      for( Furniture dw : dws ) {
         if( dw != f && rectangleIntersect( dw, f ) )
            return true;
      }

      return false;
   }

   public LinkedList<Edge> getSplitEdges() {
       return new LinkedList<Edge>(splitEdges);
   }

   public void splitEdges(Edge edge, Vertex vertex) {
       if(edge == null && vertex == null) return;
       rememberedEdges.clear();
       if(edge != null) rememberedEdges.add(edge);
       else rememberedEdges.addAll(vertex.edgeUses);
       LinkedList<Edge> used = new LinkedList<Edge>();
       Edge e;
       Edge e1;
       Edge e2;
       Vertex locationCheck;
       int skipTest;
       int i;
       while(!rememberedEdges.isEmpty()) {
           e = rememberedEdges.get(0);
           rememberedEdges.remove(0);
           splitEdges.clear();
           lineSplits.clear();
           findEdgeSplits(e, true);
           while(!splitEdges.isEmpty()) {
               skipTest = 0;
               i = 0;
               locationCheck = vertexAt(lineSplits.get(0));
               e1 = splitEdges.get(0);
               splitEdges.remove(0);
               e2 = splitEdges.get(0);
               splitEdges.remove(0);
               if(locationCheck != null) {
                   if(locationCheck.equals(e1.getV1()) || locationCheck.equals(e1.getV2())) skipTest++;
                   if(locationCheck.equals(e2.getV1()) || locationCheck.equals(e2.getV2())) skipTest += 2;
               }
               while(i < used.size()) {
                   if(e1.equals(used.get(i))) {
                       skipTest += 11;
                       break;
                   }
                   if(e2.equals(used.get(i))) {
                       skipTest += 3;
                       break;
                   }
                   i++;
               }
               if(skipTest == 0) {
                   splitCurve(e1.topDownViewCurve, lineSplits.get(0).getX(), lineSplits.get(0).getY());
                   used.add(e1);
                   delete(e1);
                   splitCurve(e2.topDownViewCurve, lineSplits.get(0).getX(), lineSplits.get(0).getY());
                   used.add(e2);
                   delete(e2);
                   break;
               } else if(skipTest == 1) {
                   splitCurve(e2.topDownViewCurve, lineSplits.get(0).getX(), lineSplits.get(0).getY());
                   used.add(e2);
                   delete(e2);
               } else if(skipTest == 2) {
                   splitCurve(e1.topDownViewCurve, lineSplits.get(0).getX(), lineSplits.get(0).getY());
                   used.add(e1);
                   delete(e1);
                   break;
               } else if(skipTest > 10) {
                   break;
               }
               lineSplits.remove(0);
           }
       }
   }
   
   private void splitCurve(QuadCurve2D quad, double x, double y) {
       if (quad == null) return;
       double sx0 = quad.getX1();
       double sx1 = quad.getX2();
       double scx = quad.getCtrlX();
       double scy = quad.getCtrlY();
       double sy0 = quad.getY1();
       double sy1 = quad.getY2();
       double p0x = 0;
       double p0y = 0;
       double p1x = 0;
       double p1y = 0;
       double dpx = -1;
       double dpy = -1;
       double tSplit = 0;
       double change = 0.00001;
       Edge edge1;
       Edge edge2;
       while (dpx >= x + 1 || dpx <= x - 1 || dpy >= y + 1 || dpy <= y - 1) {
           p0x = sx0 + (tSplit * (scx - sx0));
           p0y = sy0 + (tSplit * (scy - sy0));
           p1x = scx + (tSplit * (sx1 - scx));
           p1y = scy + (tSplit * (sy1 - scy));
           dpx = p0x + (tSplit * (p1x - p0x));
           dpy = p0y + (tSplit * (p1y - p0y));
           if(dpx <= x + 1 && dpx >= x - 1 && dpy <= y + 1 && dpy >= y - 1) break;
           tSplit += change;
           if(tSplit > 1) {
               return;
           }
       }
       Vertex v = vertexAt(new Point((int)x, (int)y));
       if(v == null) v = new Vertex(x, y, 0);
       edge1 = newEdge(new Vertex(sx0, sy0, 0), v, false);
       edge1.setCtrl(new Point((int)p0x, (int)p0y));
       edge2 = newEdge(v, new Vertex(sx1, sy1, 0), false);
       edge2.setCtrl(new Point((int)p1x, (int)p1y));
       rememberedEdges.add(edge1);
       rememberedEdges.add(edge2);
       return;
  }

   public void findEdgeSplits(Edge e, boolean storeEdges) {
       ListIterator<Edge> allEdgeIterator = edges.listIterator();
       Edge e1;
       splitEdges.clear();
       lineSplits.clear();
       while(allEdgeIterator.hasNext()) {
           e1 = allEdgeIterator.next();
           if(e != e1) {
               edgeOverlap(e, e1, storeEdges);
           }
       }
   }

   public void findEdgeSplits(Vertex v, boolean storeEdges) {
       ListIterator<Edge> vEdgeIterator = v.edgeUses.listIterator();
       ListIterator<Edge> allEdgeIterator = edges.listIterator();
       Edge e1;
       Edge e2;
       splitEdges.clear();
       lineSplits.clear();
       while(vEdgeIterator.hasNext()) {
           e1 = vEdgeIterator.next();
           while(allEdgeIterator.hasNext()) {
               e2 = allEdgeIterator.next();
               if(e1 != e2) {
                   edgeOverlap(e1, e2, storeEdges);
               }
           }
           allEdgeIterator = edges.listIterator();
       }
   }

   private void edgeOverlap(Edge e1, Edge e2, boolean storeEdges) {
       QuadCurve2D.Float qc1 = e1.topDownViewCurve;
       QuadCurve2D.Float qc2 = e2.topDownViewCurve;
       // This is just a speed improvement, ignores iterating through distant curves
       boolean collision = false;
       if(qc1.getBounds2D().intersects(qc2.getBounds2D())) collision = true;
       // getBounds2D intersection doesn't work for horizontal and vertical lines
       // but they are very fast to check because they return only one path element
       if(qc1.x1 == qc1.ctrlx && qc1.x1 == qc1.x2) collision = true;
       if(qc2.x1 == qc2.ctrlx && qc2.x1 == qc2.x2) collision = true;
       if(qc1.y1 == qc1.ctrly && qc1.y1 == qc1.y2) collision = true;
       if(qc2.y1 == qc2.ctrly && qc2.y1 == qc2.y2) collision = true;
       if(!collision) return;
       PathIterator ite1;
       PathIterator ite2;
       double[] oldCoords1 = new double[2];
       double[] newCoords1 = new double[2];
       double[] oldCoords2 = new double[2];
       double[] newCoords2 = new double[2];
       Point p1 = new Point();
       Point p2 = new Point();
       Point q1 = new Point();
       Point q2 = new Point();
       ite1 = qc1.getPathIterator(null, 0.1);
       if (!ite1.isDone()) {
           ite1.currentSegment(newCoords1);
           ite1.next();
       }
       while (!ite1.isDone()) {
           oldCoords1[0] = newCoords1[0];
           oldCoords1[1] = newCoords1[1];
           ite1.currentSegment(newCoords1);
           ite2 = qc2.getPathIterator(null, 0.1);
           if (!ite2.isDone()) {
               ite2.currentSegment(newCoords2);
               ite2.next();
           }
           while (!ite2.isDone()) {
               oldCoords2[0] = newCoords2[0];
               oldCoords2[1] = newCoords2[1];
               ite2.currentSegment(newCoords2);
               p1.setLocation(oldCoords1[0], oldCoords1[1]);
               p2.setLocation(newCoords1[0], newCoords1[1]);
               q1.setLocation(oldCoords2[0], oldCoords2[1]);
               q2.setLocation(newCoords2[0], newCoords2[1]);
               if(straightLineIntersect(p1, p2, q1, q2, true) && storeEdges) {
                   splitEdges.add(e1);
                   splitEdges.add(e2);
               }
               ite2.next();
           }
           ite1.next();
       }
   }

   public void paintLineSplits(Graphics2D g2, int diameter) {
      g2.setColor(Color.BLACK);
      ListIterator<Point> ite = lineSplits.listIterator();
      Point p;
      Ellipse2D.Float circle = new Ellipse2D.Float();
      while (ite.hasNext()) {
         p = ite.next();
         circle.setFrame(p.getX() - diameter / 2, p.getY() - diameter / 2, diameter, diameter);
         g2.fill(circle);
      }
      lineSplits.clear();
      splitEdges.clear();
   }

   private boolean furnitureWallIntersect(Furniture f, Edge e) {
       double y1;
       double y2;
       double x1;
       double x2;
       double eqn[] = new double[3];
       double ctrlY;
       double ctrlX;
       QuadCurve2D quadCurve = new QuadCurve2D.Float();
       quadCurve = rotateQuad(e.topDownViewCurve, -f.getRotation(), f.getRotationCenterX(), f.getRotationCenterY());
       x1 = quadCurve.getX1();
       x2 = quadCurve.getX2();
       y1 = quadCurve.getY1();
       y2 = quadCurve.getY2();
       ctrlX = quadCurve.getCtrlX();
       ctrlY = quadCurve.getCtrlY();
       if (intersectsLine(eqn, y1, ctrlY, y2, f.getRotationCenterY() - f.getHeight() / 2, x1, ctrlX, x2, f.getRotationCenterX() - f.getWidth() / 2, f.getRotationCenterX() + f.getWidth() / 2)
               || intersectsLine(eqn, y1, ctrlY, y2, f.getRotationCenterY() + f.getHeight() / 2, x1, ctrlX, x2, f.getRotationCenterX() - f.getWidth() / 2, f.getRotationCenterX() + f.getWidth() / 2)
               || intersectsLine(eqn, x1, ctrlX, x2, f.getRotationCenterX() - f.getWidth() / 2, y1, ctrlY, y2, f.getRotationCenterY() - f.getHeight() / 2, f.getRotationCenterY() + f.getHeight() / 2)
               || intersectsLine(eqn, x1, ctrlX, x2, f.getRotationCenterX() + f.getWidth() / 2, y1, ctrlY, y2, f.getRotationCenterY() - f.getHeight() / 2, f.getRotationCenterY() + f.getHeight() / 2)) {
           return true;
       }
       return false;
   }

   private boolean rectangleIntersect(Furniture f1, Furniture f2) {
       Point p1 = new Point();
       Point p2 = new Point();
	   Point insideTest;
       Point topLeft = f2.getTopLeft();
       Point topRight = f2.getTopRight();
       Point bottomLeft = f2.getBottomLeft();
       Point bottomRight = f2.getBottomRight();
       int i = 0;
       while(i < 4) {
           if(i == 0) {
               p1 = f1.getTopLeft();
               p2 = f1.getTopRight();
           } else if(i == 1) {
               p1 = f1.getBottomLeft();
               p2 = f1.getBottomRight();
           } else if(i == 2) {
               p1 = f1.getTopLeft();
               p2 = f1.getBottomLeft();
           } else if(i == 3) {
               p1 = f1.getTopRight();
               p2 = f1.getBottomRight();
           }
           if(straightLineIntersect(p1, p2, topLeft, topRight, false)) return true;
           if(straightLineIntersect(p1, p2, bottomLeft, bottomRight, false)) return true;
           if(straightLineIntersect(p1, p2, topLeft, bottomLeft, false)) return true;
           if(straightLineIntersect(p1, p2, topRight, bottomRight, false)) return true;
           i++;
       }
	   insideTest = rotatePoint(f1.getRotation(), f2.getRotationCenterX(), f2.getRotationCenterY(), f1.getRotationCenterX(), f1.getRotationCenterY());
       if(insideTest.x < f1.getRotationCenterX()+f1.getWidth()/2 && insideTest.x > f1.getRotationCenterX()-f1.getWidth()/2
          && insideTest.y < f1.getRotationCenterY()+f1.getHeight()/2 && insideTest.y > f1.getRotationCenterY()-f1.getHeight()/2) return true;
       insideTest = rotatePoint(f2.getRotation(), f1.getRotationCenterX(), f1.getRotationCenterY(), f2.getRotationCenterX(), f2.getRotationCenterY());
       if(insideTest.x < f2.getRotationCenterX()+f2.getWidth()/2 && insideTest.x > f2.getRotationCenterX()-f2.getWidth()/2
          && insideTest.y < f2.getRotationCenterY()+f2.getHeight()/2 && insideTest.y > f2.getRotationCenterY()-f2.getHeight()/2) return true;
       return false;
   }

   private boolean straightLineIntersect(Point a1, Point a2, Point b1, Point b2, boolean isEdges) {
       double[] a = getEquation(a1, a2);
       double[] b = getEquation(b1, b2);
       // null is for vertical lines
       if(a == null && b == null) return false;
       double x;
       double c;
       double y;
       int maxaX = Math.max(a1.x, a2.x);
       int maxbX = Math.max(b1.x, b2.x);
       int minaX = Math.min(a1.x, a2.x);
       int minbX = Math.min(b1.x, b2.x);
       int maxaY = Math.max(a1.y, a2.y);
       int maxbY = Math.max(b1.y, b2.y);
       int minaY = Math.min(a1.y, a2.y);
       int minbY = Math.min(b1.y, b2.y);
       if(a == null) {
           y = b[0]*a1.x + b[1];
           if(y <= maxaY && y >= minaY) {
               if(maxbX >= a1.x && minbX <= a1.x) {
                   if(isEdges) {
                       Point p = new Point();
                       p.setLocation(a1.x, y);
                       lineSplits.add(p);
                   }
                   return true;
               }
           }
           return false;
       }
       if(b == null) {
           y = a[0]*b1.x + a[1];
           if(y <= maxbY && y >= minbY) {
               if(maxaX >= b1.x && minaX <= b1.x) {
                   if(isEdges) {
                       Point p = new Point();
                       p.setLocation(b1.x, y);
                       lineSplits.add(p);
                   }
                   return true;
               }
           }
           return false;
       }
       if(a[0] == b[0]) {
           return false;
       }
       if(a[0] == 0) {
           if(isEdges) {
               if(b1.y < b2.y) return horizontalOverlap(maxbY, minbY, maxbX, minbX, maxaX, minaX, a[1], b1);
               else return horizontalOverlap(maxbY, minbY, maxbX, minbX, maxaX, minaX, a[1], b2);
           }
       }
       if(b[0] == 0) {
           if(isEdges) {
               if(a1.y < a2.y) return horizontalOverlap(maxaY, minaY, maxaX, minaX, maxbX, minbX, b[1], a1);
               else return horizontalOverlap(maxaY, minaY, maxaX, minaX, maxbX, minbX, b[1], a2);
           }
       }
       if(a[0] > b[0]) {
           x = a[0]-b[0];
           c = b[1]-a[1];
       } else {
           x = b[0]-a[0];
           c = a[1]-b[1];
       }
       c = c/x;
       // now intersection is where x = c
       y = c*a[0] + a[1];
       if(y <= maxaY && y >= minaY && y <= maxbY && y >= minbY
          && c <= maxaX && c >= minaX && c <= maxbX && c >= minbX) {
           if(isEdges) {
               Point p = new Point();
               p.setLocation(c, y);
               lineSplits.add(p);
           }
           return true;
       }
       return false;
   }

    private boolean horizontalOverlap(int maxaY, int minaY, int maxaX, int minaX,
                                      int maxbX, int minbX, double y, Point p) {
        if (maxaY >= y && minaY <= y) {
            double dist = (y - minaY) * (maxaX - minaX) / (maxaY - minaY);
            double XPoint;
            if (p.x == minaX) XPoint = p.x + dist;
            else XPoint = p.x - dist;
            if (XPoint <= maxbX && XPoint >= minbX) {
                Point point = new Point();
                point.setLocation(XPoint, y);
                lineSplits.add(point);
                return true;
            }
        }
        return false;
   }

   // eqn[0] is the x coefficient, eqn[1] is the constant
   private double[] getEquation(Point p1, Point p2) {
       double[] eqn = new double[2];
       double dy;
       double dx;
       // null means it is vertical
       if(p1.x == p2.x) return null;
       Point max = (p1.x > p2.x) ? p1 : p2;
       Point min = (p1.x > p2.x) ? p2 : p1;
       dy = max.y - min.y;
       dx = max.x - min.x;
       eqn[0] = dy/dx;
       eqn[1] = min.y-(eqn[0]*min.x);
       return eqn;
   }
   
   private QuadCurve2D.Float rotateQuad(QuadCurve2D quadCurve, double rotation, double centreX, double centreY) {
	double x1 = quadCurve.getX1();
	double x2 = quadCurve.getX2();
	double y1 = quadCurve.getY1();
	double y2 = quadCurve.getY2();
	double ctrlX = quadCurve.getCtrlX();
	double ctrlY = quadCurve.getCtrlY();
	Point start = rotatePoint(rotation, x1, y1, centreX, centreY);
	Point end = rotatePoint(rotation, x2, y2, centreX, centreY);
	Point ctrl = rotatePoint(rotation, ctrlX, ctrlY, centreX, centreY);
	return new QuadCurve2D.Float((float)start.getX(), (float)start.getY(),
								 (float)ctrl.getX(), (float)ctrl.getY(),
								 (float)end.getX(), (float)end.getY());
  }
  
   private Point rotatePoint(double rotation, double pointX, double pointY, double centreX, double centreY) {
      pointX -= centreX;
	  pointY -= centreY;
      double hypotenuse = Math.sqrt(Math.pow(pointX, 2) + Math.pow(pointY, 2));
      double initialRotation;
      if (pointX == 0) {
         initialRotation = (pointY >= 0) ? Math.PI / 2 : -Math.PI / 2;
      } else {
         initialRotation = Math.atan2(pointY, pointX);
      }
      double finalRotation = initialRotation + rotation;
      double endX = hypotenuse * Math.cos(finalRotation);
      double endY = hypotenuse * Math.sin(finalRotation);
	  endX += centreX;
	  endY += centreY;
      Point p = new Point();
      p.setLocation(endX, endY);
      return p;
   }

////////////////////////////////////////////////////////////////////////////////////////////////////// 
////// CODE BELOW FOUND ON : http://www.codng.com/2005/07/intersecting-quadcurve2d-part-ii.html //////
////////////////////////////////////// Author : Juan Cruz Nores //////////////////////////////////////
   private boolean intersectsLine(double[] eqn, double p0, double p1, double p2, double c,
                                   double pb0, double pb1, double pb2,
                                   double from, double to) {
        eqn[2] = p0 - 2*p1 + p2;
        eqn[1] = 2*p1-2*p0;
        eqn[0] = p0 - c;
        int nRoots = QuadCurve2D.solveQuadratic(eqn);
        boolean result;
        switch(nRoots) {
			case 1:
				result = (eqn[0] >= 0) && (eqn[0] <= 1);
				if(result) {
					double intersection = evalQuadraticCurve(pb0,pb1,pb2,eqn[0]);
					result = (intersection >= from) && (intersection <= to);
				}
				break;
			case 2:
				result = (eqn[0] >= 0) && (eqn[0] <= 1);
				if(result) {
					double intersection = evalQuadraticCurve(pb0,pb1,pb2,eqn[0]);
					result = (intersection >= from) && (intersection <= to);
				}
				if(!result) {
					result = (eqn[1] >= 0) && (eqn[1] <= 1);
					if(result) {
						double intersection = evalQuadraticCurve(pb0,pb1,pb2,eqn[1]);
						result = (intersection >= from) && (intersection <= to);
					}
				}
				break;
			default:
				result = false;
		}
		return result;
    }
   
   public double evalQuadraticCurve(double c1, double ctrl, double c2, double t) {
        double u = 1 - t;
        double res = c1 * u * u + 2 * ctrl * t * u + c2 * t * t;
        return res;
    }
////// CODE ABOVE FOUND ON : http://www.codng.com/2005/07/intersecting-quadcurve2d-part-ii.html //////
////////////////////////////////////// Author : Juan Cruz Nores //////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////

   public void rotateFurniture(Furniture f, double radians) {
	  if(f == null || !furniture.contains(f) ) return;
	  f.setRotation(radians);
        fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.FURNITURE_CHANGED, f));
   }

   /** Removes the furniture item */
   public void delete(Furniture f) {
      if (f == null || !furniture.contains(f) ) return;

      furniture.remove(f);

      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.FURNITURE_REMOVED, f));
   }

   /** Removes the door/window */
   public void deleteDoorWindow(Furniture f) {
      if ( f == null ) return;

      Edge e = getDoorWindowEdge(f);
      if( e == null ) {
         if( invalidDW == f )
            invalidDW = null;

         return;
      }
      
      e.deleteDoorWindow(f);

      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.DOORWINDOW_REMOVED, f));
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, e));
   }

   /** Returns a new edge object, already added to the coordStore */
   public Edge newEdge(Vertex v1, Vertex v2, boolean snapToGrid) {
      v1 = addVertex(v1.getX(), v1.getY(), v1.getZ(), null, snapToGrid);
      v2 = addVertex(v2.getX(), v2.getY(), v2.getZ(), null, snapToGrid);

      Edge e = new Edge(v1, v2, null);
      v1.setUse(e);
      v2.setUse(e);

      edges.add(e);
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_ADDED, e));

      return e;
   }

   /** returns an array containing all the Edges in the current design */
   public Edge[] getEdges() {
      return edges.toArray(new Edge[0]);
   }
   
   /** returns an array containing all the Furniture in the current design */
   public Furniture[] getFurniture(){
      return furniture.toArray(new Furniture[0]);
   }
   
   public boolean edgeFurnitureCollision(double pointX, double pointY) {
	  ListIterator<Furniture> furnitureIterator = furniture.listIterator();
      while(furnitureIterator.hasNext()) {
         Furniture f = furnitureIterator.next();
		 if(pointIsOverFurniture(f, pointX, pointY)) return true;
	  }
	  return false;
   }

   private boolean pointIsOverFurniture(Furniture f, double pointX, double pointY) {
      pointX -= f.getRotationCenterX();
      pointY -= f.getRotationCenterY();
      double hypotenuse = Math.sqrt(Math.pow(pointX, 2) + Math.pow(pointY, 2));
      double initialRotation;
      if(pointX == 0) initialRotation = (pointY >= 0) ? Math.PI/2: -Math.PI/2;
      else initialRotation = Math.atan2(pointY, pointX);
      double finalRotation = initialRotation - f.getRotation();
      double endX = hypotenuse * Math.cos(finalRotation);
      double endY = hypotenuse * Math.sin(finalRotation);
      if(endX <= f.getWidth()/2  && endX >= -f.getWidth()/2 &&
         endY <= f.getHeight()/2 && endY >= -f.getHeight()/2) {
            return true;
      }
      return false;
   }

   public Furniture furnitureAt(double MouseX, double MouseY) {
      ListIterator<Furniture> ite = furniture.listIterator();
      while(ite.hasNext()) {
            Furniture f = ite.next();
            if(pointIsOverFurniture(f, MouseX, MouseY)) return f;
      }
      return null;
   }

   /** returns the vertex that the Point p lies within, or null if none */
   public Vertex vertexAt(Point p) {
      ListIterator<Vertex> ite = vertices.listIterator();

      while (ite.hasNext()) {
         Vertex v = ite.next();
         if (v.contains(p)) return v;
      }
      
      return null;
   }

   /** Returns the edge that is associated with the curve control circle at p,
    *  or null if p doesn't lie within a ctrl point */
   public Edge ctrlAt(Point p) {
      ListIterator<Edge> ite = edges.listIterator();
		
      while (ite.hasNext()) {
         Edge e = ite.next();
         if (e.curveCtrlContains(p)) return e;
      }
		
      return null;
   }

   public Furniture doorWindowAt(Point p) {
      ListIterator<Edge> ite = edges.listIterator();

      while( ite.hasNext() ) {
         Edge e = ite.next();
         if( e.doorWindowAt(p) ) return e.getDoorWindowAt(p);
      }

      return null;
   }

   /** If there is already a vertex with the given coords then that vertex is
    *  returned otherwise null is returned */
   private Vertex vertexInUse(float x, float y, float z) {
      ListIterator<Vertex> ite = vertices.listIterator();
      
      while (ite.hasNext()) {
         Vertex v = ite.next();
         if (v.equalsLocation(x, y, z)) return v;
      }
      
      return null;
   }
   
   /** Draws things like lines and curves as well as doors and windows on the given Graphics canvas */
   public void paintEdges(Graphics2D g2, boolean isCurveTool) {
      g2.setColor(Color.BLACK);

      ListIterator<Edge> ite = edges.listIterator();
      while (ite.hasNext()) {
         ite.next().paint(g2, isCurveTool);
      }
   }

   /** Draws the small vertex circles on the given Graphics canvas */
   public void paintVertices(Graphics2D g2, int diameter) {
      g2.setColor(Color.BLACK);

      ListIterator<Vertex> ite = vertices.listIterator();
      while (ite.hasNext()) {
         ite.next().paint(g2, diameter);
      }
   }

   /** Draws all the furniture objects, highlights collisions with walls */
   public void paintFurniture(Graphics2D g2) {
      g2.setColor(Color.BLUE);

      ListIterator<Furniture> ite = furniture.listIterator();
      while (ite.hasNext()) {
         ite.next().paint(g2);
      }
   }

   public void paintInvalidDW(Graphics2D g2) {
      g2.setColor(Color.RED);

      if( invalidDW != null )
         invalidDW.paint(g2);
   }

   /** Updates the given vertices coordinates to the given ones and returns the
    *  new vertex that should be used now. It may return the same vertex or a
    *  new one if you try to set the coordinates to a vertex that already exists,
    *  it will merge the two to avoid duplicate entries. This is essentially
    *  snapping, but to a very precise level!! */
   public void set(Vertex v, float x, float y, float z, boolean snapToGrid) {
      if (v == null || !vertices.contains(v)) return;

      if (snapToGrid) {
         x = snapToGrid(x);
         y = snapToGrid(y);
         z = snapToGrid(z);
      }

      // if the vertex is currently snapped, it might not be changing position, don't fire events
      if (!(v.p.x()==x && v.p.y()==y && v.p.z()==z)) {
         Edge[] affectedEdges = v.edgeUses.toArray(new Edge[0]);
         for (Edge e : affectedEdges) {
            if( e.isStraight() ) {
               e.wasStraight = true;
            }

            if( e.getV1() == v )
               e.storeRotation(1);
            else if( e.getV2() == v )
               e.storeRotation(2);
         }

         v.set(x, y, z);

         // fire a shit load of events
         for (Edge e : affectedEdges) {
            if( e.wasStraight ) {
               e.resetCtrlPositionToHalfway();
               e.wasStraight = false;
            } else {
               e.resetCurveCtrlPosition();
            }

            e.resetDoorsWindows(this);
            e.recalcTopDownView();
            e.discardRotation();
            fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, e));
         }
      }
   }
   
   public Point mergeVertices(Vertex v, float x, float y, float z, boolean snapToGrid) {
      Point oldPoint = new Point();
      oldPoint.setLocation(-1,-1);
      if (v == null || !vertices.contains(v)) return oldPoint;
	  
	  Vertex vAlt = null;
	  
      if (snapToGrid) {
         x = snapToGrid(x);
         y = snapToGrid(y);
         z = snapToGrid(z);
		 vAlt = vertexInUse(x, y, z);
      } else {
		 Point p = new Point();
		 p.setLocation(x, y);
	     ListIterator<Vertex> ite = vertices.listIterator();
		
         while (ite.hasNext()) {
            Vertex vertex = ite.next();
            if(vertex.contains(p) && vertex != v) {
				vAlt = vertex;
				break;
			}
		 }
      }

      if (vAlt != null && vAlt != v) {
         oldPoint.setLocation(vAlt.p.x(), vAlt.p.y());
         v.addUsesCutFrom(vAlt);
         vertices.remove(vAlt);
      }

      // if the vertex is currently snapped, it might not be changing position, don't fire events
      if (!(v.p.x()==x && v.p.y()==y && v.p.z()==z)) {
         v.set(x, y, z);

         // fire a shit load of events
         Edge[] affectedEdges = v.edgeUses.toArray(new Edge[0]);
         for (Edge e : affectedEdges) {
            fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, e));
         }
      }
      return oldPoint;
   }

   /** Updates the given vertex so that it no longer remembers Object o as something
    *  that uses it. If the vertex no longer has any uses then it gets deleted! */
   private void removeUse(Vertex v, Edge e) {
      if (v == null) return;

      v.edgeUses.remove(e);

      if (!v.isUsed()) vertices.remove(v);
   }

   /** Returns the snapped to grid version of the given coord part (x,y,z) */
   private float snapToGrid(float coord) {
      float distIntoCell = coord % gridWidth;

      if (Math.abs(distIntoCell) < 0.5 * gridWidth) {
         coord -= distIntoCell;
      } else if (coord < 0) {
         coord -= gridWidth + (coord % gridWidth);
      } else { // x >= 0
         coord += gridWidth - (coord % gridWidth);
      }

      return coord;
   }

   /** Snaps a point to the nearest edge. Works on straight edges only */
   public Point snapToEdge(Point p) {
      ListIterator<Edge> ite = edges.listIterator();
      Rectangle2D proximity = new Rectangle2D.Double( p.getX()-10, p.getY()-10, 20, 20 );

      while(ite.hasNext()) {
         Edge e = ite.next();

         if( e.isStraight() && e.getqcurve().intersects(proximity) ) {
            Vertex v1 = e.getV1();
            Vertex v2 = new Vertex( p.getX(), p.getY(), v1.getZ() );

            double theta1 = (Math.PI/2) - e.getRotation();
            double theta2 = e.getRotation() - Edge.getRotation( v1, v2 );

            double a = Math.abs(v1.getX() - v2.getX());
            double b = Math.abs(v1.getY() - v2.getY());
            double len = Math.sqrt(a*a + b*b) * Math.cos(theta2);

            double dy = len * Math.cos(theta1);
            double dx = len * Math.sin(theta1);
            p.setLocation( v1.getX() - dx, v1.getY() - dy );

            break;
         }
      }

      return p;
   }

   /** If the vertex exists already it prevents duplicate entries.
    *  usefor is the object that will be added to the (perhaps new) vertex's
    *  list of objects that are using it. If Edge useFor is null, then it will
    *  not be added to the vertex's list of uses */
   private Vertex addVertex(float x, float y, float z, Edge useFor, boolean snapToGrid) {
      if (snapToGrid) {
         x = snapToGrid(x);
         y = snapToGrid(y);
         z = snapToGrid(z);
      }

      Vertex inUse = vertexInUse(x, y, z);
      if (inUse != null) {
         // there is already a vertex with these points
         inUse.setUse(useFor); // will do nothing if useFor is null
         return inUse;

      } else {
         // vertex doesn't exist yet
         Vertex newV = new Vertex(x, y, z);
         newV.setUse(useFor); // will do nothing if useFor is null
         vertices.add(newV);
         return newV;
      }
   }

   /** Moves one end of this line to the new coordinates given. If the vertex
    *  end being moved is used by a different object too (i.e. the end of this
    *  line is snapped to the other object) then it will create a new vertex for
    *  the end of this line, to essentially unsnap (split) from that other
    *  object's vertex */
   public void vertexMoveOrSplit(Edge e, boolean isV1, float x, float y, float z, boolean snapToGrid) {
      Vertex toMove = (isV1 ? e.getV1() : e.getV2());

      // tell vertex its no longer used by this edge. if v1 == v2 then we
      // certainly don't want to remove the only vertex thats in use!
      if (e.getV1() != e.getV2()) removeUse(toMove, e);
      
      Vertex newV = addVertex(x, y, z, e, snapToGrid);
      boolean fireChangeEventNecessary = !toMove.equals(newV);

      if (isV1) {
         e.setV1(newV, false);
      } else {
         e.setV2(newV, false);
      }

      if (fireChangeEventNecessary) {
         fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, e));
      }
   }

   /** Returns true if the given vertex is in the coordStore. */
   public boolean exists(Coords.Vertex v) {
      return vertices.contains(v);
   }

   /** Looks at each of the objects attached in some way to the given
    *  vertex. This means that everything "snapped" to the vertex will be deleted. */
   public void delete(Coords.Vertex v) {
      if (v == null || !vertices.contains(v)) return;

      // delete edges. This is not a listIterator to stop concurrent exceptions!
      Edge[] edgeUsesArray = v.edgeUses.toArray(new Edge[0]);
      for (int i=0; i < edgeUsesArray.length; i++) {
         Edge e = edgeUsesArray[i];
         delete(e);
      }

      //if this doesn't remove the vertex, then there is a bug elsewhere
   }

   /** Deletes the given edge from its two endpoint vertices and the edges list */
   public void delete(Edge e) {
      if (e == null || !edges.contains(e)) return;

      Coords.Vertex v1 = e.getV1(), v2 = e.getV2();
      if (v1 == null || v2 == null) return;

      removeUse(v1, e);
      removeUse(v2, e);

      edges.remove(e);

      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_REMOVED, e));
   }

   /** calls setCtrl on the edge with the new point and fires an edge changed event */
   public void setEdgeCtrl(Edge dragEdge, Point loc) {
      if (dragEdge == null || !edges.contains(dragEdge) || loc == null) {
         throw new IllegalArgumentException("null parameter");
      }
      
      dragEdge.setCtrl(loc);
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, dragEdge));
   }

   /** Draws the grid on the given Graphics canvas, from 0,0 to width,height */
   public void drawGrid(Graphics2D g2, int width, int height) {
      g2.setColor(Color.LIGHT_GRAY);

      for (int i = (int) (gridWidth); i < width; i += gridWidth) {
         g2.drawLine(i, 0, i, height);
      }

      for (int i = (int) (gridWidth); i < height; i += gridWidth) {
         g2.drawLine(0, i, width, i);
      }
   }
   
   //-SAVING-STUFF--------------------------------------------------------------

   /** Returns true if the coords were modified since it was created, or if save
    *  failed or false if nothing has changed or a save was successful */
   public boolean saveRequired() {
      return saveRequired;
   }

   /** Returns the associated save file (which might be null) */
   public File getAssociatedSaveFile() {
      return associatedSave;
   }

   /** Returns the canonical save file path if there is a save file or the name if there is none set */
   public String getAssociatedSaveFileAsString() {
      if (associatedSave == null) return associatedSaveName;

      try {
         return associatedSave.getCanonicalPath();

      } catch (IOException e) {
         // If an I/O error occurs, which is possible because the construction of the canonical pathname may require filesystem queries
         return "unable to access file location (IO error)";

      } catch (SecurityException e) {
         // If a required system property value cannot be accessed.
         return "unable to access file location (Security Error)";
      }
   }

   /** Saves with the currently associated save file. Throws an IOException in the
    *  case that there is no associated save file yet, in this case you should be
    *  using saveAs(). It also sets requiredSave=false if everything saved OK */
   public void save() throws IOException {
      if (associatedSave == null) {
         throw new IOException("Save file has not been set, you should use saveAs in this case");
      }
      save(associatedSave, true, false);
   }

   /** Saves as the given file. Updates the associated save file if its different
    *  and also sets requiredSave=false if everything saved OK */
   public void saveAs(File saveAs) throws IOException {
      save(saveAs, true, true);
   }

   /** Saves as the given file. Does not change the associated save file, it will
    *  essentially make a copy in the background. Does not set requiredSave=false
    *  so if the user made changes they will still be required to save them to this
    *  Coords instance */
   public void saveCopyAs(File saveAs) throws IOException {
      save(saveAs, false, false);
   }

   /** save the stuff to the given file, if you save to a different file than the
    *  associated one, this coords instance has a new associated file set */
   private void save(File saveAs, boolean updateSaveRequired, boolean updateAssociatedSave) throws IOException {
      // make the list of vertices that will be saved
      Vertex[] vArray = vertices.toArray(new Vertex[0]);
      float[][] saveVerts = new float[vArray.length][3];
      for (int i=0; i < vArray.length; i++) {
         saveVerts[i][0] = vArray[i].getX();
         saveVerts[i][1] = vArray[i].getY();
         saveVerts[i][2] = vArray[i].getZ();
      }

      // make the list of edges that will be saved
      Edge[] eArray = edges.toArray(new Edge[0]);
      int[][] saveEdges = new int[eArray.length][4];
      for (int i=0; i < eArray.length; i++) {
         // find the index of the two endpoint vertices. If there is an error
         // finding v1 or v2, the save file will be corrupt, but it might be
         // recoverable, so continue anyway!
         saveEdges[i][0] = saveIndexIn(vArray, eArray[i].getV1());
         saveEdges[i][1] = saveIndexIn(vArray, eArray[i].getV2());
         saveEdges[i][2] = eArray[i].getCtrlX();
         saveEdges[i][3] = eArray[i].getCtrlY();
      }

      // make the list of furniture items which will be saved
      LinkedList<Furniture> saveFurniture = new LinkedList<Furniture>();

      /* Add furniture from the normal list */
      ListIterator<Furniture> li = furniture.listIterator();
      while (li.hasNext()) saveFurniture.add(li.next());

      /* Add furniture from the edge lists */
      ListIterator<Edge> edgeIterator = edges.listIterator();
      while (edgeIterator.hasNext()) {
          Furniture[] doorWindows = edgeIterator.next().getDoorWindow();
          if (doorWindows == null) continue;

          for (int i=0; i < doorWindows.length; i++) {
              saveFurniture.add(doorWindows[i]);
          }
      }
      Furniture[] fArray = saveFurniture.toArray(new Furniture[0]);

      FileManager.save(saveAs, saveVerts, saveEdges, fArray);
      // if save() throws an IllegalArgumentException, saveRequired is (rightly) not reset
      if (updateSaveRequired) saveRequired = false;
      if (updateAssociatedSave) {
         associatedSaveName = saveAs.getName();
         associatedSave = saveAs;
      }
   }

   /** Returns the index in vArray that v is at, or -1 if it is not found */
   private int saveIndexIn(Vertex[] vArray, Vertex v) {
      for (int i=0; i < vArray.length; i++) {
         if (vArray[i] == v) return i;
      }
      return -1;
   }

   public static void testCoords() {
      // Edge related tests
      {
         Coords c = new Coords("blank save name", objectBrowser);
         Vertex v1 = new Vertex(0,0,0);
         Vertex v2 = new Vertex(121,121,0);
         Edge e1, e2;
         
         // test e1 has snapped to grid and e2 hasn't
         e1 = c.newEdge(v1, v2, true); // snap to grid
         e2 = c.newEdge(v1, v2, false); // no snap
         if (c.vertices.size() != 3 || c.edges.size() != 2 ||
               !(c.vertices.contains(v1)
                 && c.vertices.contains(v2)
                 && c.vertices.contains(new Vertex(120, 120, 0))
               )) {
            System.err.println("e1 - edge didn't snap to grid correctly / vertex merge error");
         }

         // test edge deletion, edge 2 should be removed but not the vertices
         c.delete(e2);
         if (c.vertices.size() != 2 || c.edges.size() != 1 ||
               !(c.vertices.contains(v1)
                 && c.vertices.contains(new Vertex(120, 120, 0))
               )) {
            System.err.println("e2 - edge deletion error");
         }

         // every edge and vertex should be deleted with this
         c.delete(e1.getV1());
         if (c.vertices.size() != 0 || c.edges.size() != 0) {
            System.err.println("e3 - vertex deletion error (unexpected remaining nodes/edges)");
         }

         // test furniture intersections
         e1 = null; e2 = null;
         e1 = c.newEdge(v1, v2, false);
         e2 = null;
         Furniture f = new Furniture("15,80.0,45.0,121.0,121.0,-0.051237167403418805,sofa_1.obj,false,false,false,false,true", objectBrowser);
         if (!c.furnitureWallIntersect(f,e1)) {
            System.err.println("e4 - furniture intersection error");
         }

         // test getEdges()
         Edge[] shouldContainE1 = c.getEdges();
         if (shouldContainE1 == null || shouldContainE1.length != 1 || shouldContainE1[0] != e1) {
            System.err.println("e5 - getEdges() error, it contains incorrect elements");
         }
         c.delete(e1);

         // test mergeVertices to make sure it properly merges vertices
         e1 = c.newEdge(v1, v2, false);
         Vertex v3 = new Vertex(120,120,0);
         e2 = c.newEdge(v1, new Vertex(120,120,0), false);

         c.mergeVertices(e2.getV2(), 121, 121, 0, false);

         if (c.vertices.size() != 2 || c.edges.size() != 2) {
            System.err.println("e6 - set has corrupted the edges or vertices list, incorrect size");
         }
         if (!c.vertices.contains(v1) || !c.vertices.contains(e2.getV2())) {
            System.err.println("e7 - wrong vertex was moved with set, the one to be changed was deleted/replaced");
         }
         if (c.vertices.contains(v3)) {
            System.err.println("e8 - wrong vertex was replaced with set, the one to be replaced is still there");
         }
         if (!(e2.getV2().p.x() == 121 && e2.getV2().p.y() == 121 && e2.getV2().p.z() == 0) ) {
            System.err.println("e9 - vertex to be moved isn't in the right place");
         }

         // test the coord system can lookup where the points are
         Point lookup = new Point(121,121);
         Vertex at = c.vertexAt(lookup);
         if  (at == null || at != e2.getV2()) {
            System.err.println("e10 - vertex lookup failure");
         }

         // test the coord systen can still lookup points
         Vertex inUseLookup = c.vertexInUse((float) 121, (float) 121, (float) 0);
         if (inUseLookup == null || inUseLookup != e2.getV2() || inUseLookup.p.x() != 121 || inUseLookup.p.y() != 121 || inUseLookup.p.z() != 0) {
            System.err.println("e11 - vertex in use lookup failure");
         }

         // test the splitting from merged vertices function
         c.delete(e1);
         c.delete(e2);
         v3 = new Vertex(30,30,0);
         e1 = c.newEdge(v1, v2, false);
         e2 = c.newEdge(v2, v3, false);
         c.vertexMoveOrSplit(e2, true, 121, 160, 0, false);
         if (!(c.vertices.size() == 4)
               || !(c.vertices.contains(v1)
                    && c.vertices.contains(v2)
                    && c.vertices.contains(v3)
                    && c.vertices.contains(new Vertex(121,160,0))
               )) {
            System.err.println("e12 - vertex didn't split correctly");
         }
      }

      System.out.println("Coords test complete");
   }

   //-CHANGE-EVENT-STUFF--------------------------------------------------------
   
   private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
   void addCoordsChangeListener(CoordsChangeListener listener) {
      listenerList.add(CoordsChangeListener.class, listener);
   }
   void removeCoordsChangeListener(CoordsChangeListener listener) {
      listenerList.remove(CoordsChangeListener.class, listener);
   }
   private void fireCoordsChangeEvent(CoordsChangeEvent event) {
      saveRequired = true;
      CoordsChangeListener[] listeners = listenerList.getListeners(CoordsChangeListener.class);
      for (CoordsChangeListener listener : listeners) {
         try {
            listener.coordsChangeOccurred(event);
         } catch (Exception e) {
            /* Catch so we can still fire events if one of the listeners crash */
         }
      }
   }
   public void somethingChanged(Edge e) {
      if (e == null || !edges.contains(e)) return;
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.EDGE_CHANGED, e));
   }
   public void somethingChanged(Furniture f) {
      if (f == null || !furniture.contains(f) ) return;
      fireCoordsChangeEvent(new CoordsChangeEvent(this, CoordsChangeEvent.FURNITURE_CHANGED, f));
   }
}