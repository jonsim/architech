import java.util.*;

public class CoordsChangeEvent extends EventObject {
   public static final int EDGE_ADDED = 0;
   public static final int EDGE_REMOVED = 1;
   public static final int EDGE_CHANGED = 2;
   public static final int FURNITURE_ADDED = 3;
   public static final int FURNITURE_REMOVED = 4;
   public static final int FURNITURE_CHANGED = 5;
   public static final int DOORWINDOW_ADDED = 6;
   public static final int DOORWINDOW_REMOVED = 7;
   public static final int DOORWINDOW_CHANGED = 8;

   private int changeType;
   private Edge edgeChanged;
   private Furniture furnitureChanged;

   /** edgeChanges is null if the event is not edge related */
   public CoordsChangeEvent(Object source, int changeType, Edge edgeChanged) {
      super(source);

      if (!this.isEdgeRelated(changeType)) {
         throw new IllegalArgumentException("changeType must be Edge related");
      }

      setEventFields(changeType, edgeChanged, null);
   }

   public CoordsChangeEvent(Object source, int changeType, Furniture furnitureChanged) {
      super(source);

      if ( !this.isFurnitureRelated(changeType) && !this.isDoorWindowRelated(changeType) ) {
         throw new IllegalArgumentException("changeType must be Furniture related");
      }

      setEventFields(changeType, null, furnitureChanged);
   }

   private void setEventFields(int changeType, Edge edgeChanged, Furniture furnitureChanged) {
      this.changeType = changeType;
      this.edgeChanged = edgeChanged;
      this.furnitureChanged = furnitureChanged;
   }

   /** Might be null if event changeType is not edge related */
   public Edge getEdgeChanges() {
      return edgeChanged;
   }

   /** Might be null if event changeType is not furniture related */
   public Furniture getFurnitureChanged() {
      return furnitureChanged;
   }

   /** Tells you whether the event was Edge or Furniture related and what happened */
   public int getChangeType() {
      return changeType;
   }

   @Override
   public String toString() {
      String s = "Event Type \"" + eventTypeToString() + "\", ";

      if (isEdgeRelated()) {
         s += "Edge " + edgeChanged.hashCode();
      } else if (isFurnitureRelated()) {
         s += "Furniture " + furnitureChanged.hashCode();
      } else return "Unknown Event Type";

      return s;
   }

   private String eventTypeToString() {
      if (changeType == EDGE_ADDED) return "EDGE_ADDED";
      else if(changeType == EDGE_REMOVED) return "EDGE_REMOVED";
      else if (changeType == EDGE_CHANGED) return "EDGE_CHANGED";
      else if (changeType == FURNITURE_ADDED) return "FURNITURE_ADDED";
      else if (changeType == FURNITURE_REMOVED) return "FURNITURE_REMOVED";
      else if (changeType == FURNITURE_CHANGED) return "FURNITURE_CHANGED";
      else if (changeType == DOORWINDOW_ADDED) return "DOORWINDOW_ADDED";
      else if (changeType == DOORWINDOW_REMOVED) return "DOORWINDOW_REMOVED";
      else if (changeType == DOORWINDOW_CHANGED) return "DOORWINDOW_CHANGED";

      else return "UNKNOWN";
   }

   private boolean isEdgeRelated(int changeType) {
      if (changeType == EDGE_ADDED || changeType == EDGE_REMOVED || changeType == EDGE_CHANGED) return true;
      else return false;
   }

   private boolean isFurnitureRelated(int changeType) {
      if (changeType == FURNITURE_ADDED || changeType == FURNITURE_REMOVED || changeType == FURNITURE_CHANGED) return true;
      else return false;
   }

   private boolean isDoorWindowRelated(int changeType) {
      if (changeType == DOORWINDOW_ADDED || changeType == DOORWINDOW_REMOVED || changeType == DOORWINDOW_CHANGED) return true;
      else return false;
   }

   public boolean isEdgeRelated() {
      return isEdgeRelated(changeType);
   }

   public boolean isFurnitureRelated() {
      return isFurnitureRelated(changeType);
   }

   @Override
   public Coords getSource() {
      return (Coords) super.source;
   }
}
