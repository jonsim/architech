import javax.swing.*;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

/**  */
public class Viewport3D extends JPanel implements ActionListener{

   private DragSource dragSource;
   private DragGestureListener dgListener;
   private ThreeD canvasApplication;
   private JFrame tdwind;

   private Main main;

   Viewport3D(Main main) {
      super(new GridBagLayout());
      this.setBackground(Color.WHITE);
      this.setBorder(BorderFactory.createLineBorder(Color.GRAY));

      this.main = main;
      
      dragSource = DragSource.getDefaultDragSource();
      dgListener = new SQLDragListener();

      // component, action, listener
      dragSource.createDefaultDragGestureRecognizer(this, SQLDragListener.dragAction, dgListener);
   
     
//add clumsy 3D Window
      // create new JME appsettings
      AppSettings settings = new AppSettings(true);
      settings.setWidth(640);
      settings.setHeight(480);
      // create new canvas application
      canvasApplication = new ThreeD();
      canvasApplication.setSettings(settings);
      canvasApplication.createCanvas(); // create canvas!
      JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
      ctx.setSystemListener(canvasApplication);
      Dimension dim = new Dimension(640, 480);
      ctx.getCanvas().setPreferredSize(dim);
      // Create Swing window
      tdwind = new JFrame("3D Window");
      tdwind.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      // Fill Swing window with canvas and swing components
      JPanel panel = new JPanel(new FlowLayout()); // a panel
      panel.add(ctx.getCanvas());                  // add JME canvas
      //add update button
      JButton b3;
      b3 = new JButton("Update");
      b3.addActionListener(this);
      b3.setActionCommand("update");
      panel.add(b3);
      tdwind.add(panel);
      tdwind.pack();
      // Display 3D preview window including JME canvas
      tdwind.setVisible(true);
      canvasApplication.startCanvas();
   }
   
   @Override
   public void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;

      g2.drawImage(main.frontEnd.getIcon(main.frontEnd.ICON_LOCATION), new AffineTransform(), null);

   }

   public JPanel getPane() {
      return this;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
   	// TODO Auto-generated method stub
   	if(e.getActionCommand().equals("update"))
       {
   			canvasApplication.clearall();
   			canvasApplication.updateroot();
   			canvasApplication.addedges(main.coordStore.getEdges());   			
       }
   }
}
