import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/** Provides a canvas (with getCanvas()) containing the 3d window */
public class TWPane{
   private TWApp canvasApplication;
   private Tweaker main;

   /** Used by JMEPaneEmpty to disable 3D */
   TWPane() {
   }

   TWPane(Tweaker main) {
      this.main = main;    
      Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();

//add clumsy 3D Window
      // create new JME appsettings
      AppSettings settings = new AppSettings(true);
      settings.setWidth(200);
      settings.setHeight(300);
      // create new canvas application
      canvasApplication = new TWApp(main);
      canvasApplication.setSettings(settings);
      Logger.getLogger("").setLevel(Level.SEVERE);
      canvasApplication.createCanvas(); // create canvas!
      JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
      ctx.getCanvas().setPreferredSize(new Dimension( (int) scrDim.getWidth()-650, (int) scrDim.getHeight()-550));
      ctx.setSystemListener(canvasApplication);
      }

   /** Nicely disposes of the 3D stuff so that everything can close without exit(0) */
   public void shutdown3D() {
      canvasApplication.stop();
   }
   
   public void startcan(){
	   canvasApplication.startCanvas();

   }
   
   public void addf(String name){
	   //canvasApplication.addf(name);
   }
   public void additem(String path, String fname, JPanel control){canvasApplication.additem(path,fname,control);return;}
   public JPanel removeitem(int id){return canvasApplication.removeitem(id);}
   public void moveitem(int id,char dir){canvasApplication.moveitem(id,dir);return;}
   public void paintitem(int id,int swit,String path, String name, float tex){canvasApplication.paintitem(id,swit,path,name,tex);return;}
   public void saveitem(){canvasApplication.saveitem();}
   
   public void look3d(){
	 //canvasApplication.look3d();
   }
   
   public void focus(){
   	 canvasApplication.focus();
}
   
   public TWApp getapp(){
	   return canvasApplication;
   }

   /** FrontEnd will put this in the main window */
   public Canvas getCanvas() {
      JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
      return ctx.getCanvas();
   }

}
