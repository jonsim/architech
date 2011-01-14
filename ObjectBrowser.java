
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

/**
 *
 * @author Michael
 */
public class ObjectBrowser {

   private Main main;

   private JPanel pane;
   private ArrayList<String> fields = new ArrayList<String>();
   private JList library;
   // This will get the database as long as it is in the current directory
   private String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "\\ObjectDatabase.sqlite";
   private Font f = new Font("sansserif", Font.PLAIN, 14);
   private Connection connection = null;
   private PreparedStatement statement = null;
   private ResultSet rs = null;
   private boolean InCategories;

   ObjectBrowser(Main main) {
      this.main = main;

      initPane();
   }

   public FurnitureSQLData getSelectedFurnitureOrNull() {
      return null; // not done yet
   }

   private void initPane() {
      pane = new JPanel(new GridBagLayout());
      pane.setBackground(Color.WHITE);
      pane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

      // Load up the list of categories
      SQLStatement("select * from CATEGORIES", "", 0, "Category");
      AddLibrary();
      InCategories = true;
   }

   private void AddLibrary() {
      library = null;
      library = new JList(fields.toArray());
      library.setFont(f);

      Insets top_left_bottom_right = new Insets(10,10,10,10);
      GridBagConstraints c;
      c = FrontEnd.buildGBC(0, 0, 0.5, 0.5, GridBagConstraints.NORTHWEST, top_left_bottom_right);
      pane.add(library, c);
   }

   // Criteria argument changes what column of the table is returned
   // Changes is the number of ?s in the request
   // Type is what you swap those ?s for
   private void SQLStatement(String request, String criteria, int changes, String Type) {
      try {
         Class.forName("org.sqlite.JDBC");
         connection = DriverManager.getConnection(url);
         statement = connection.prepareStatement(request);
         int i = 1;
         while (i <= changes) {
            statement.setString(i, criteria);
            i++;
         }
         rs = statement.executeQuery();
         while (rs.next()) {
            fields.add(rs.getString(Type));
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            rs.close();
            statement.close();
            connection.close();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public void selectCategory() {
      int index = library.getSelectedIndex();
      index++;
      if (index > 0 && InCategories == true) {
         InCategories = false;
         fields.clear();
         pane.remove(library);
         pane.repaint();
         SQLStatement("select * from TYPE where Category1=? or Category2=? or Category3=?", Integer.toString(index), 3, "Type");
         AddLibrary();
         pane.revalidate();
      }
   }

   public void toCategories() {
      if (InCategories == false) {
         InCategories = true;
         fields.clear();
         pane.remove(library);
         pane.repaint();
         SQLStatement("select * from CATEGORIES", "", 0, "Category");
         AddLibrary();
         pane.revalidate();
      }
   }

   public JPanel getPane() {
      return pane;
   }
}
