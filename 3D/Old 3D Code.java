
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
    
    /* ceiling
    boxshape1 = new Box(new Vector3f(0,100, 200),200,1,200);
    geom = new Geometry("My Textured Box", boxshape1);
	Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
    mat.setColor("m_Color", ColorRGBA.LightGray);
    geom.setMaterial(mat);
    rootNode.attachChild(geom);*/
    
    
    /* floor (carpet)
    boxshape1 = new Box(new Vector3f(0,-100, 200),200,1,200);
    geom = new Geometry("My Textured Box", boxshape1);
    mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
    tex_ml = assetManager.loadTexture("req/carp.jpg");
    mat_stl.setTexture("m_ColorMap", tex_ml);
    geom.setMaterial(mat_stl);
    rootNode.attachChild(geom);*/
    
    /* door
    boxshape1 = new Box(new Vector3f(90,-30,1),30,70,1);
    Geometry cube = new Geometry("My Textured Box", boxshape1);
    mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
    tex_ml = assetManager.loadTexture("req/door.jpg");
    mat_stl.setTexture("m_ColorMap", tex_ml);
    cube.setMaterial(mat_stl);
    rootNode.attachChild(cube);*/

    // args[0] = "armchair1"
    // args[1] = "sofa1"
    // args[2] = "table1"
    // args[3] = "lamp1"
    //
    //             x     y     z
    // +--------+-----+-----+-----+
    // |        | [0] | [1] | [2] |
    // +--------+-----+-----+-----+
    // | pos[0] | 160 | -98 | 170 |
    // | pos[1] | 100 | -98 | 320 |
    // | pos[2] |   0 | -98 | 200 |
    // | pos[3] | 150 | -98 |  40 |
    // +--------+-----+-----+-----+     

	// add sun (as headlight)
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(10,-50,0).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);

	// add skybox
    rootNode.attachChild(SkyFactory.createSky(assetManager, "req/BrightSky.dds", false));        
    }
    
	String dbGetModel (String name)
	{
		if (name.equals("armchair1"));
			return "req/armchair.obj";
		/*if (name.equals("sofa1"));
			return "req/sofa.obj";
		if (name.equals("table1"));
			return "req/coffee table.obj";
		if (name.equals("lamp1"));
			return "req/lamp.obj";*/
	}
	
	float[] dbGetScale (String name)
	{
		float scale;
		float[] result = new float[3];
		if (name.equals("armchair1"));
			scale = 30f;
		if (name.equals("sofa1"));
			scale = 30f;
		if (name.equals("table1"));
			scale = 20f;
		if (name.equals("lamp1"));
			scale = 15f;
			
		result[0] = scale;
		result[1] = scale;
		result[2] = scale;
		return result;
	}
}