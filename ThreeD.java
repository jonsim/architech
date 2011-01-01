import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.math.ColorRGBA;

public class ThreeD extends ArchApp {

/* This file specifies how JME should compose the test scene, using
 * OBJ files and textures we have created */
	
    public static void main(String[] args){
    	ThreeD app = new ThreeD();
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
    
    // constructs a wall between (x1,y1) and (x2,y2)
    void makewall (int x1, int y1, int x2, int y2)
    {
    	int length,leny,lenx=0;
    	float rotation=0;
        Geometry wall = new Geometry();
        
		//work out the distances and angles required
		lenx = x2 - x1;
		leny = y2 - y1;
		if(lenx==0){length=leny; rotation=-(float) Math.toRadians(90);} else{
		if(leny==0){length=lenx; rotation=0;} else{
    	length = (int) Math.sqrt((Math.pow(lenx,2) + Math.pow(leny,2)));
    	rotation = (float) -(Math.atan((double) (leny) / (lenx)));
    	if(y2>y1 & x1>x2)  {rotation += Math.PI;}
    	if(y2<y1 & x1>x2)  {rotation += Math.PI;}
		}}
		
		//Draw a quad between the two given verticies using dist
		//and angles calculate above
		wall = new Geometry ("Box", new Quad(length,200));
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
		mat.setTexture("m_ColorMap", assetManager.loadTexture("req/wall1.jpg"));
		wall.setMaterial(mat);
		wall.setLocalTranslation(new Vector3f(x1, -100, y1));
		wall.rotate(0f, rotation, 0f);
		rootNode.attachChild(wall);	
		
		//Double up the quad
		wall = new Geometry ("Box", new Quad(length,200));
		wall.setMaterial(mat);
		wall.setLocalTranslation(new Vector3f(x2, -100, y2));
		wall.rotate(0f, (float) (rotation + Math.PI), 0f);
		rootNode.attachChild(wall);
    	}    	        
    
    @Override
	public void simpleInitApp()
	{
		flyCam.setDragToRotate(true);
		addbackg();			
}
    
    void addbackg(){
		//add the grassy area
	    Quad blah = new Quad(2000,2000);
		Geometry geom = new Geometry("Box", blah);
	    Material mat_stl = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
	    Texture tex_ml = assetManager.loadTexture("req/grass.jpg");
	    mat_stl.setTexture("m_ColorMap", tex_ml);
	    geom.setMaterial(mat_stl);
	    geom.setLocalTranslation(new Vector3f(2000,-100,-500));
		geom.rotate((float) -Math.toRadians(90),(float) Math.toRadians(180),0f );		
	    rootNode.attachChild(geom);
	    
	    //add the sky image
	    rootNode.attachChild(SkyFactory.createSky(
	            assetManager, "req/BrightSky.dds", false));
	    
	    //add a sun
	    DirectionalLight sun = new DirectionalLight();
	    sun.setDirection(new Vector3f(10,-50,0).normalizeLocal());
	    sun.setColor(ColorRGBA.White);
	    rootNode.addLight(sun);	
    }
    
	void clearall()
    {
		rootNode.detachAllChildren();
		addbackg();
    }
	
	void updateroot(){
		rootNode.updateGeometricState();
	}
	
	void addedges(Edge[] edges){
		int i,x1,x2,y1,y2;
		for(i=0;i<edges.length;i++){
			x1 = (int) edges[i].getV1().getX();
			x2 = (int) edges[i].getV2().getX();
			y1 = (int) edges[i].getV1().getY();
			y2 = (int) edges[i].getV2().getY();
			makewall(x1,y1,x2,y2);
		}
	}	
	/*
    // NB: a float[] model_rotation is required
    void addModels (String[] model_name, int[][] model_position, Spatial[] model)
    {
	    float[] scales = new float[3];
	    Material mat;	    
	    model = new Spatial[args.length];
	    
        // load models (as per arguments given), position and scale accordingly.
		for (int i = 0; i < args.length; i++)
		{
			scales = dbGetScale(model_name[i]);
			
			model[i] = assetManager.loadModel(dbGetModel(model_name[i]));
			model[i].scale(scales[0], scales[1], scales[2]);
        	model[i].setLocalTranslation(model_position[i][0], model_position[i][1], model_position[i][2]);
        	model[i].rotate((float) -(0.5 * PI), (float) -(PI), 0);
        	
        	mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
			mat.setColor("m_Color", ColorRGBA.LightGray);
			model[i].setMaterial(mat);
        	
        	rootNode.attachChild(model[i]);
		} 	
    }*/
    /* list of requirements from 2D GUI:
           - array of vertices representing the points making up the room
                 FORM: V[n][2]  :  V[i][0] = xi, V[i][1] = yi, etc.
                 INTERNAL FORM: wall[n]  :  wall[i], etc. (as a Quad)
                 
           - array of strings (or other database identifiers) defining the pieces
             of furniture needed in the room allowing their details to be looked
             up from the database
                 FORM: F[n]  :  F[i] = "furniture1", etc.
                 INTERNAL FORM: model[n]  :  model[i], etc. (as a Spatial)
                 
           - array of vertices representing the points furniture (whose id
             corresponds the the identicle position in the array of strings)
                 FORM: P[n][2]  :  V[i][0] = xi, V[i][1] = yi, etc. (top left)
                 
           - array of floats representing relevent rotations where -PI < rot <= PI
             depends on implementation decision wrt. rotation (snapping etc)
                 FORM: R[n]  :  R[i] = -PI/2, etc. (where wall[i].rotation = R[i])
                 
           - array of vertices representing the location of doors
           
           - array of vertices representing the location of windows
           
           - array of strings representing the texture of the strip of wall at the
             corresponding position in the wall array.
                 FORM: T[n]  :  T[i], etc. (where wall[i].texture = T[i])
    */    
}   
