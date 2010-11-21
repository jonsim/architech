
import java.net.URL;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.vecmath.Point3f;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Float;
import java.util.*;

/** This is the main
 *  
 *
 * @author James
 */
public class CoordStore {

   /**
    *
    * @author James
    */
   public class Vertex {
      public static final int diameter = 10;

      private Point3f p;
      private LinkedList uses = new LinkedList();

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
         p = new Point3f(x, y, z);
      }

      /** Sets this vertex's points to the new ones */
      private void set(float x, float y, float z) {
         p.set(x, y, z);
      }

      /** Adds the given object to the list of objects using this vertex */
      private void setUse(Object o) {
         if (uses.contains(o)) return; // already used by that object
         uses.add(o);
      }

      /** Removes the given object from the list of objects using this vertex */
      private void removeUse(Object o) {
         uses.remove(o);
      }

      /** Returns true if the list of objects using this vertex is not empty */
      public boolean isUsed() {
         if (uses.size() > 0) return true;
         else return false;
      }

      /** Get the X component */
      public float getX() {
         return p.x;
      }

      /** Get the Y component */
      public float getY() {
         return p.y;
      }

      /** Get the Z component */
      public float getZ() {
         return p.z;
      }

      /** Returns the top down (2D) representation of this vertex, i.e. a circle.
       *  This class can decide how big a circle representation it wants to give */
      public Ellipse2D.Float topDownView() {
         return new Ellipse2D.Float(p.x - diameter / 2, p.y - diameter / 2, diameter, diameter);
      }

      /** Returns the list iterator of this vertex's uses. It can be used to
       *  iterate through all the objects using it, i.e. edges or curves */
      public ListIterator getUsesIterator() {
         return uses.listIterator();
      }

      /** Returns true iff the x,y,z coords of the given vertex match this
       *  vertex's coords */
      public boolean equals(Vertex v) {
         return equals(v.p.x, v.p.y, v.p.z);
      }

      /** Returns true iff the x,y,z coords of v match the parameters */
      public boolean equals(float x, float y, float z) {
         if (p.x == x && p.y == y && p.z == z) return true;
         else return false;
      }
   }

   /*!------------------------------------------------------------------------*/
   
   private LinkedList<Vertex> vertices = new LinkedList<Vertex>();

   /** Just for demonstration this makes a few edges  */
   CoordStore() {
      new Edge(new Vertex(650.0,71.0,0.0), new Vertex(646.0,72.0,0.0), this);
      new Edge(new Vertex(641.0,77.0,0.0), new Vertex(636.0,81.0,0.0), this);
      new Edge(new Vertex(632.0,85.0,0.0), new Vertex(627.0,92.0,0.0), this);
      new Edge(new Vertex(614.0,107.0,0.0), new Vertex(607.0,113.0,0.0), this);
      new Edge(new Vertex(601.0,127.0,0.0), new Vertex(598.0,132.0,0.0), this);
      new Edge(new Vertex(593.0,139.0,0.0), new Vertex(590.0,144.0,0.0), this);
      new Edge(new Vertex(585.0,153.0,0.0), new Vertex(581.0,161.0,0.0), this);
      new Edge(new Vertex(578.0,169.0,0.0), new Vertex(576.0,182.0,0.0), this);
      new Edge(new Vertex(575.0,189.0,0.0), new Vertex(576.0,198.0,0.0), this);
      new Edge(new Vertex(578.0,205.0,0.0), new Vertex(580.0,208.0,0.0), this);
      new Edge(new Vertex(584.0,208.0,0.0), new Vertex(593.0,209.0,0.0), this);
      new Edge(new Vertex(597.0,209.0,0.0), new Vertex(604.0,209.0,0.0), this);
      new Edge(new Vertex(608.0,208.0,0.0), new Vertex(615.0,205.0,0.0), this);
      new Edge(new Vertex(619.0,202.0,0.0), new Vertex(627.0,194.0,0.0), this);
      new Edge(new Vertex(631.0,189.0,0.0), new Vertex(640.0,179.0,0.0), this);
      new Edge(new Vertex(644.0,173.0,0.0), new Vertex(650.0,164.0,0.0), this);
      new Edge(new Vertex(660.0,152.0,0.0), new Vertex(672.0,140.0,0.0), this);
      new Edge(new Vertex(678.0,134.0,0.0), new Vertex(685.0,122.0,0.0), this);
      new Edge(new Vertex(689.0,115.0,0.0), new Vertex(700.0,106.0,0.0), this);
      new Edge(new Vertex(706.0,102.0,0.0), new Vertex(711.0,95.0,0.0), this);
      new Edge(new Vertex(714.0,92.0,0.0), new Vertex(718.0,87.0,0.0), this);
      new Edge(new Vertex(655.0,65.0,0.0), new Vertex(664.0,55.0,0.0), this);
      new Edge(new Vertex(668.0,54.0,0.0), new Vertex(668.0,53.0,0.0), this);
      new Edge(new Vertex(663.0,50.0,0.0), new Vertex(661.0,51.0,0.0), this);
      new Edge(new Vertex(656.0,53.0,0.0), new Vertex(640.0,52.0,0.0), this);
      new Edge(new Vertex(635.0,52.0,0.0), new Vertex(630.0,52.0,0.0), this);
      new Edge(new Vertex(627.0,50.0,0.0), new Vertex(624.0,46.0,0.0), this);
      new Edge(new Vertex(620.0,42.0,0.0), new Vertex(619.0,37.0,0.0), this);
      new Edge(new Vertex(618.0,31.0,0.0), new Vertex(618.0,25.0,0.0), this);
      new Edge(new Vertex(618.0,17.0,0.0), new Vertex(622.0,10.0,0.0), this);
      new Edge(new Vertex(623.0,7.0,0.0), new Vertex(632.0,7.0,0.0), this);
      new Edge(new Vertex(637.0,6.0,0.0), new Vertex(650.0,6.0,0.0), this);
      new Edge(new Vertex(656.0,6.0,0.0), new Vertex(662.0,9.0,0.0), this);
      new Edge(new Vertex(674.0,15.0,0.0), new Vertex(675.0,16.0,0.0), this);
      new Edge(new Vertex(685.0,19.0,0.0), new Vertex(685.0,19.0,0.0), this);
      new Edge(new Vertex(702.0,33.0,0.0), new Vertex(735.0,32.0,0.0), this);
      new Edge(new Vertex(736.0,33.0,0.0), new Vertex(755.0,41.0,0.0), this);
      new Edge(new Vertex(762.0,47.0,0.0), new Vertex(770.0,59.0,0.0), this);
      new Edge(new Vertex(772.0,67.0,0.0), new Vertex(772.0,73.0,0.0), this);
      new Edge(new Vertex(772.0,80.0,0.0), new Vertex(766.0,88.0,0.0), this);
      new Edge(new Vertex(762.0,90.0,0.0), new Vertex(750.0,91.0,0.0), this);
      new Edge(new Vertex(729.0,90.0,0.0), new Vertex(726.0,89.0,0.0), this);
      new Edge(new Vertex(697.0,29.0,0.0), new Vertex(693.0,19.0,0.0), this);
      new Edge(new Vertex(711.0,32.0,0.0), new Vertex(720.0,33.0,0.0), this);
      new Edge(new Vertex(592.0,145.0,0.0), new Vertex(595.0,155.0,0.0), this);
      new Edge(new Vertex(596.0,155.0,0.0), new Vertex(601.0,160.0,0.0), this);
      new Edge(new Vertex(602.0,163.0,0.0), new Vertex(608.0,168.0,0.0), this);
      new Edge(new Vertex(611.0,171.0,0.0), new Vertex(623.0,171.0,0.0), this);
      new Edge(new Vertex(624.0,171.0,0.0), new Vertex(630.0,171.0,0.0), this);
      new Edge(new Vertex(635.0,172.0,0.0), new Vertex(642.0,172.0,0.0), this);
      new Edge(new Vertex(594.0,188.0,0.0), new Vertex(589.0,198.0,0.0), this);
      new Edge(new Vertex(589.0,198.0,0.0), new Vertex(586.0,202.0,0.0), this);
      new Edge(new Vertex(568.0,224.0,0.0), new Vertex(566.0,228.0,0.0), this);
      new Edge(new Vertex(562.0,237.0,0.0), new Vertex(557.0,244.0,0.0), this);
      new Edge(new Vertex(547.0,267.0,0.0), new Vertex(543.0,274.0,0.0), this);
      new Edge(new Vertex(512.0,319.0,0.0), new Vertex(509.0,323.0,0.0), this);
      new Edge(new Vertex(492.0,371.0,0.0), new Vertex(490.0,376.0,0.0), this);
      new Edge(new Vertex(486.0,398.0,0.0), new Vertex(485.0,403.0,0.0), this);
      new Edge(new Vertex(492.0,431.0,0.0), new Vertex(466.0,433.0,0.0), this);
      new Edge(new Vertex(446.0,435.0,0.0), new Vertex(456.0,443.0,0.0), this);
      new Edge(new Vertex(470.0,445.0,0.0), new Vertex(484.0,445.0,0.0), this);
      new Edge(new Vertex(503.0,444.0,0.0), new Vertex(503.0,419.0,0.0), this);
      new Edge(new Vertex(475.0,426.0,0.0), new Vertex(462.0,428.0,0.0), this);
      new Edge(new Vertex(423.0,430.0,0.0), new Vertex(411.0,430.0,0.0), this);
      new Edge(new Vertex(401.0,432.0,0.0), new Vertex(439.0,438.0,0.0), this);
      new Edge(new Vertex(440.0,442.0,0.0), new Vertex(524.0,448.0,0.0), this);
      new Edge(new Vertex(540.0,448.0,0.0), new Vertex(541.0,440.0,0.0), this);
      new Edge(new Vertex(535.0,434.0,0.0), new Vertex(525.0,434.0,0.0), this);
      new Edge(new Vertex(513.0,434.0,0.0), new Vertex(498.0,432.0,0.0), this);
      new Edge(new Vertex(460.0,424.0,0.0), new Vertex(455.0,424.0,0.0), this);
      new Edge(new Vertex(382.0,123.0,0.0), new Vertex(342.0,307.0,0.0), this);
      new Edge(new Vertex(145.0,290.0,0.0), new Vertex(41.0,300.0,0.0), this);
      new Edge(new Vertex(178.0,122.0,0.0), new Vertex(328.0,252.0,0.0), this);
      new Edge(new Vertex(439.0,203.0,0.0), new Vertex(525.0,321.0,0.0), this);
      new Edge(new Vertex(792.0,337.0,0.0), new Vertex(781.0,311.0,0.0), this);
      new Edge(new Vertex(901.0,129.0,0.0), new Vertex(1041.0,225.0,0.0), this);
      new Edge(new Vertex(1096.0,247.0,0.0), new Vertex(781.0,374.0,0.0), this);
      new Edge(new Vertex(980.0,115.0,0.0), new Vertex(833.0,353.0,0.0), this);
      new Edge(new Vertex(747.0,152.0,0.0), new Vertex(979.0,316.0,0.0), this);
      new Edge(new Vertex(1090.0,29.0,0.0), new Vertex(1057.0,259.0,0.0), this);
      new Edge(new Vertex(909.0,74.0,0.0), new Vertex(1130.0,188.0,0.0), this);
      new Edge(new Vertex(378.0,245.0,0.0), new Vertex(218.0,377.0,0.0), this);
      new Edge(new Vertex(152.0,269.0,0.0), new Vertex(330.0,444.0,0.0), this);
      new Edge(new Vertex(84.0,375.0,0.0), new Vertex(405.0,400.0,0.0), this);
      new Edge(new Vertex(938.0,415.0,0.0), new Vertex(650.0,336.0,0.0), this);
      new Edge(new Vertex(672.0,377.0,0.0), new Vertex(866.0,511.0,0.0), this);
      new Edge(new Vertex(1031.0,291.0,0.0), new Vertex(1168.0,378.0,0.0), this);
      new Edge(new Vertex(1099.0,399.0,0.0), new Vertex(917.0,298.0,0.0), this);
      new Edge(new Vertex(721.0,33.0,0.0), new Vertex(721.0,33.0,0.0), this);
      new Edge(new Vertex(114.0,12.0,0.0), new Vertex(101.0,29.0,0.0), this);
      new Edge(new Vertex(101.0,29.0,0.0), new Vertex(119.0,46.0,0.0), this);
      new Edge(new Vertex(119.0,46.0,0.0), new Vertex(132.0,28.0,0.0), this);
      new Edge(new Vertex(132.0,28.0,0.0), new Vertex(114.0,12.0,0.0), this);
      new Edge(new Vertex(147.0,9.0,0.0), new Vertex(147.0,45.0,0.0), this);
      new Edge(new Vertex(147.0,28.0,0.0), new Vertex(167.0,28.0,0.0), this);
      new Edge(new Vertex(167.0,28.0,0.0), new Vertex(169.0,49.0,0.0), this);
      new Edge(new Vertex(203.0,11.0,0.0), new Vertex(204.0,51.0,0.0), this);
      new Edge(new Vertex(202.0,32.0,0.0), new Vertex(223.0,32.0,0.0), this);
      new Edge(new Vertex(223.0,32.0,0.0), new Vertex(224.0,11.0,0.0), this);
      new Edge(new Vertex(224.0,32.0,0.0), new Vertex(224.0,51.0,0.0), this);
      new Edge(new Vertex(238.0,50.0,0.0), new Vertex(255.0,10.0,0.0), this);
      new Edge(new Vertex(255.0,10.0,0.0), new Vertex(272.0,50.0,0.0), this);
      new Edge(new Vertex(248.0,33.0,0.0), new Vertex(264.0,35.0,0.0), this);
      new Edge(new Vertex(285.0,11.0,0.0), new Vertex(292.0,52.0,0.0), this);
      new Edge(new Vertex(282.0,52.0,0.0), new Vertex(305.0,51.0,0.0), this);
      new Edge(new Vertex(278.0,9.0,0.0), new Vertex(293.0,9.0,0.0), this);
      new Edge(new Vertex(105.0,71.0,0.0), new Vertex(104.0,108.0,0.0), this);
      new Edge(new Vertex(82.0,69.0,0.0), new Vertex(131.0,71.0,0.0), this);
      new Edge(new Vertex(146.0,71.0,0.0), new Vertex(147.0,111.0,0.0), this);
      new Edge(new Vertex(147.0,92.0,0.0), new Vertex(174.0,91.0,0.0), this);
      new Edge(new Vertex(174.0,91.0,0.0), new Vertex(173.0,111.0,0.0), this);
      new Edge(new Vertex(189.0,100.0,0.0), new Vertex(215.0,100.0,0.0), this);
      new Edge(new Vertex(215.0,100.0,0.0), new Vertex(197.0,82.0,0.0), this);
      new Edge(new Vertex(197.0,82.0,0.0), new Vertex(187.0,101.0,0.0), this);
      new Edge(new Vertex(188.0,102.0,0.0), new Vertex(206.0,118.0,0.0), this);
      new Edge(new Vertex(206.0,118.0,0.0), new Vertex(219.0,115.0,0.0), this);
      new Edge(new Vertex(234.0,116.0,0.0), new Vertex(231.0,86.0,0.0), this);
      new Edge(new Vertex(231.0,85.0,0.0), new Vertex(267.0,84.0,0.0), this);
      new Edge(new Vertex(283.0,105.0,0.0), new Vertex(313.0,103.0,0.0), this);
      new Edge(new Vertex(313.0,103.0,0.0), new Vertex(297.0,87.0,0.0), this);
      new Edge(new Vertex(297.0,87.0,0.0), new Vertex(284.0,104.0,0.0), this);
      new Edge(new Vertex(283.0,105.0,0.0), new Vertex(302.0,122.0,0.0), this);
      new Edge(new Vertex(302.0,122.0,0.0), new Vertex(326.0,122.0,0.0), this);
   }

   public void set(Vertex v, float x, float y, float z) {
      v.set(x, y, z);
   }

   public void removeUse(Vertex v, Object o) {
      v.removeUse(o);
      if (!v.isUsed()) vertices.remove(v);
   }

   /** You can use the returned ListIterator to iterate through all the vertices */
   public ListIterator<Vertex> getVerticesIterator() {
      return vertices.listIterator();
   }

   /** If there is already a vertex with the given coords that vertex is
    *  returned otherwise null is returned */
   private Vertex vertexInUse(float x, float y, float z) {
      ListIterator<Vertex> ite = vertices.listIterator();
      while (ite.hasNext()) {
         if (ite.next().equals(x, y, z)) {
            return ite.previous();
         }
      }
      return null;
   }

   /** If the vertex exists already it prevents duplicate entries.
    *  usefor is the object that will be added to the (perhaps new) vertex's
    *  list of objects that are using it */
   public Vertex addVertex(float x, float y, float z, Object useFor) {
      if (useFor == null) {
         Main.showFatalExceptionTraceWindow(new NullPointerException("useFor object is null"));
         return null;
      }

      Vertex inUse = vertexInUse(x, y, z);
      if (inUse != null) {
         // there is already a vertex with these points
         inUse.setUse(useFor);
         return inUse;

      } else {
         // vertex doesn't exist yet
         Vertex newV = new Vertex(x, y, z);
         newV.setUse(useFor);
         vertices.add(newV);
         return newV;
      }
   }

}
