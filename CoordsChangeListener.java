import java.util.EventListener;

interface CoordsChangeListener extends EventListener {
   public void coordsChangeOccurred(CoordsChangeEvent e);
}
