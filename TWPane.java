import java.awt.Canvas;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

/** Provides a canvas (with getCanvas()) containing the 3d window */
public class TWPane{
   private TWApp canvasApplication;

   /** Used by JMEPaneEmpty to disable 3D */
   TWPane() {
   }

   TWPane(Dimension cansize) {   
      
   }
   
   public void make3d(Dimension cansize) {   
      // create new JME appsettings
      AppSettings settings = new AppSettings(true);
      settings.setWidth(640);
      settings.setHeight(480);
      // create new canvas application
      canvasApplication = new TWApp();
      canvasApplication.setSettings(settings);
      Logger.getLogger("").setLevel(Level.SEVERE);
      canvasApplication.createCanvas(); // create canvas!
      JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
      ctx.getCanvas().setPreferredSize(cansize);
      ctx.setSystemListener(canvasApplication);
      startcan();		
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
   public void additem(String path, String fname, int id){canvasApplication.additem(path,fname,id);return;}
   public void removeitem(int id){canvasApplication.removeitem(id);}
   public void moveitem(int id,char dir){canvasApplication.moveitem(id,dir);return;}
   public void paintitem(int id,int swit,String path, String name, float tex,float red, float green, float blue){canvasApplication.paintitem(id,swit,path,name,tex,red,green,blue);return;}
   public void saveitem(String name, String description, String type,ObjectBrowser obrow){canvasApplication.saveitem(name,description,type,obrow);}
   
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
