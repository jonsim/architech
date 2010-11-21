package exp;
 
import com.jme3.app.*;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.math.ColorRGBA;


public class Helloexp extends RobinApp {

    public static void main(String[] args){
    	 Helloexp app = new Helloexp();
    	 AppSettings settings = new AppSettings(true);
    	 settings.setResolution(1360,720);
    	 String blah = "robins awesome play";
    	 settings.setTitle(blah);
         settings.setSamples(4);
    	 app.setSettings(settings);
    	 app.start();
    }
    
    @Override
    public void initialize(){
    	super.initialize();
}
    
    void addwall(Vector3f centre, int x, int y, int z, ColorRGBA something)
    {
        Box blah = new Box(centre, x, y, z);
    	Geometry geom = new Geometry("Box", blah);
    	Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat.setColor("m_Color", something);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
    }
    
     
	@Override
	public void simpleInitApp() {		
		
        Box blah = new Box(Vector3f.ZERO, 200, 100, 1);
    	Geometry geom = new Geometry("Box", blah);
        Material mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        Texture tex_ml = assetManager.loadTexture("Common/wall1.jpg");
        mat_stl.setTexture("m_ColorMap", tex_ml);
        geom.setMaterial(mat_stl);
        rootNode.attachChild(geom);        
        
                                    
        Box boxshape1 = new Box(new Vector3f(0,-100, 100),200,1,100);
        geom = new Geometry("My Textured Box", boxshape1);
    	Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat.setColor("m_Color", ColorRGBA.LightGray);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
        
        boxshape1 = new Box(new Vector3f(90,-30,1),30,70,1);
        Geometry cube = new Geometry("My Textured Box", boxshape1);
        mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        tex_ml = assetManager.loadTexture("Common/door.jpg");
        mat_stl.setTexture("m_ColorMap", tex_ml);
        cube.setMaterial(mat_stl);
        rootNode.attachChild(cube);
        
       
        blah = new Box(new Vector3f(200,0,100), 1, 100, 100);
    	geom = new Geometry("Box", blah);
        mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        tex_ml = assetManager.loadTexture("Common/wall2.jpg");
        mat_stl.setTexture("m_ColorMap", tex_ml);
        geom.setMaterial(mat_stl);
        rootNode.attachChild(geom);      
        
        Vector3f centre =  new Vector3f(0,-101, 100);
        ColorRGBA col = ColorRGBA.White;
        addwall(centre,2000,1,1000,col);
      

        rootNode.attachChild(SkyFactory.createSky(
        assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
               
        
		
	}
    
    
    
}