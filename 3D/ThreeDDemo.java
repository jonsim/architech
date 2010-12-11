package exp;

import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.math.ColorRGBA;
import static java.lang.Math.PI;

public class ThreeDDemo extends ArchApp {

/* This file specifies how JME should compose the test scene, using
 * OBJ files and textures we have created */
	
    public static void main(String[] args){
    	ThreeDDemo app = new ThreeDDemo();
    	 AppSettings settings = new AppSettings(true);
    	 settings.setResolution(1280,700);
    	 String titre = "Architech Alpha - 3D Demo";
    	 settings.setTitle(titre);
         settings.setSamples(4);
    	 app.setSettings(settings);
    	 app.start();
    }
    
    @Override
    public void initialize(){
    	super.initialize();
    }
    
    //add all the objects, walls and ceilings
    
    @Override
	public void simpleInitApp() {		
   	    flyCam.setDragToRotate(true);
   	    
        Quad blah = new Quad(400,200);
    	Geometry geom = new Geometry("Box", blah);
        Material mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        Texture tex_ml = assetManager.loadTexture("req/wall1.jpg");
        mat_stl.setTexture("m_ColorMap", tex_ml);
        geom.setMaterial(mat_stl);
        geom.setLocalTranslation(new Vector3f(-200,-100,0));
        rootNode.attachChild(geom);      
        
        blah = new Quad(400,200);
    	geom = new Geometry("Box", blah);
        mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        tex_ml = assetManager.loadTexture("req/wall1.jpg");
        mat_stl.setTexture("m_ColorMap", tex_ml);
        geom.setMaterial(mat_stl);
        geom.setLocalTranslation(new Vector3f(200,-100,400));
        geom.rotate(0.0f, (float) -(PI), 0.0f);
        rootNode.attachChild(geom);
                                    
        Box boxshape1 = new Box(new Vector3f(0,100, 200),200,1,200);
        geom = new Geometry("My Textured Box", boxshape1);
    	Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
        mat.setColor("m_Color", ColorRGBA.LightGray);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
        
        boxshape1 = new Box(new Vector3f(0,-100, 200),200,1,200);
        geom = new Geometry("My Textured Box", boxshape1);
        mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        tex_ml = assetManager.loadTexture("req/carp.jpg");
        mat_stl.setTexture("m_ColorMap", tex_ml);
        geom.setMaterial(mat_stl);
        rootNode.attachChild(geom);
        
        boxshape1 = new Box(new Vector3f(90,-30,1),30,70,1);
        Geometry cube = new Geometry("My Textured Box", boxshape1);
        mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        tex_ml = assetManager.loadTexture("req/door.jpg");
        mat_stl.setTexture("m_ColorMap", tex_ml);
        cube.setMaterial(mat_stl);
        rootNode.attachChild(cube);        
       
        boxshape1 = new Box(new Vector3f(200,0,200), 1, 100, 200);
    	geom = new Geometry("Box", boxshape1);
        mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        tex_ml = assetManager.loadTexture("req/wall1.jpg");
        mat_stl.setTexture("m_ColorMap", tex_ml);
        geom.setMaterial(mat_stl);
        rootNode.attachChild(geom);
        
        Spatial chair = assetManager.loadModel("req/armchair.obj");
        chair.scale(15f,20f,30f);
        chair.setLocalTranslation(160, -98, 170);
        chair.rotate((float) -(0.5 * PI), (float) -( PI), 0);
        rootNode.attachChild(chair);
        
        chair = assetManager.loadModel("req/sofa.obj");
        chair.scale(15f,20f,30f);
        chair.setLocalTranslation(100, -98, 320);
        chair.rotate((float) -(0.5 * PI), (float) -(PI * 1.2), 0);
        rootNode.attachChild(chair);
        
        Spatial table = assetManager.loadModel("req/coffee table.obj");
        table.scale(15f,20f,15f);
        table.setLocalTranslation(0, -98, 200);
        table.rotate((float) -(0.5 * PI),0, 0);
        rootNode.attachChild(table);      
        
        Spatial lamp= assetManager.loadModel("req/lamp.obj");
        lamp.scale(15f,15f,15f);
        lamp.setLocalTranslation(150, -98, 40);
        lamp.rotate((float) -(0.5 * PI), 0, 0);
        rootNode.attachChild(lamp);        
        
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(10,-50,0).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
                      
        boxshape1 = new Box(new Vector3f(0,-101,100),500, 1, 500);
        geom = new Geometry("Box", boxshape1);
        mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        tex_ml = assetManager.loadTexture("req/grass.jpg");
        mat_stl.setTexture("m_ColorMap", tex_ml);
        geom.setMaterial(mat_stl);
        rootNode.attachChild(geom);

        rootNode.attachChild(SkyFactory.createSky(
        assetManager, "req/BrightSky.dds", false));        
        }
}