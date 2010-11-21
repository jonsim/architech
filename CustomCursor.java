import java.awt.*;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 *
 * @author James
 */
public class CustomCursor {
   /** used to determine the cursor icon directory, same as this class. */
   CustomCursor() {
   }

   /** returns the default cursor */
   public static Cursor createDefault() {
      return new Cursor(Cursor.DEFAULT_CURSOR);
   }

   /** returns the default cursor if the specified one cannot be made */
   public static Cursor create(String nameLocation, Point hotSpot, String name) {
      Toolkit toolkit = Toolkit.getDefaultToolkit();

      Image image = getIcon(nameLocation);
      if (image == null) return createDefault();
      else return toolkit.createCustomCursor(image, hotSpot, name);
   }

   /** Returns an image for use as the icon or null if it failed somehow */
   private static Image getIcon(String location) {
      URL iconResource = (new CustomCursor()).getClass().getResource(location);
      if (iconResource == null) return null;

      Image icon = (new ImageIcon(iconResource)).getImage();
      return icon;
   }
}
