package exp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.math.ColorRGBA;

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
		/*if (args.length < 1 || args.length != pos.length)
		{
			System.err.println("Incorrect arguments to simpleInitApp");
			return;
		}*/
		flyCam.setDragToRotate(true);

		//load the file in and draw the given walls
        try {
			load();
		} catch (IOException e) {
		} catch (Exception e) {
		}                    
        
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
    
    //load the file in and draw the vertices encoded within
	void load() throws IOException, Exception {
        File saveas = new File("testSave.atech");
        FileReader fileR = new FileReader(saveas);
        BufferedReader br = new BufferedReader(fileR);
        /* Get number of vertices */
        int numVertices = loadHeaderNum(br);
        if (numVertices < 0) {
           throw new Exception("The number of vertices is either invalid or not g"
              + "iven in the file");
        }
        /* Load vertices */
        float[][] vertices = loadVertices(br, numVertices);
        if (vertices == null) throw new Exception("Unable to load all vertices");
        /* Get Number of Edges */
        int numEdges = loadHeaderNum(br);
        if (numEdges < 0) {
           throw new Exception("The number of edges is either invalid or not give"
              + "n in the file");
        }
        /* Load Edges */
        int[][] edges = drawedges(br, numEdges,vertices);
        if (edges == null) throw new Exception("Unable to load all edges");
        /* Loading might not reach this stage */
        return;
     }
   
	//load either # vert or # edges from file
	int loadHeaderNum(BufferedReader br) throws IOException {
        String line;
        String[] split;
        int numObjects = -1;
        while ((line = br.readLine()) != null && (split = line.split("\\s")) != null) {
           if (split.length > 0) {
              try {
                 numObjects = Integer.parseInt(split[0]);
                 break;
              } catch (NumberFormatException e) {
                 // this line isn't in the form "# Vertices", keep searching
              }
           }
        }
        return numObjects;
     }       
    
	//load vertices from file into a 2d array
	float[][] loadVertices(BufferedReader br, int numVertices) throws IOException {
        if (numVertices < 0) return null;
        String line;
        String[] split;
        float[][] vertices = new float[numVertices][3];
        int vertexLoadCount = 0;

        while (vertexLoadCount < numVertices && (line = br.readLine()) != null && (split = line.split(",")) != null) {
           if (split.length == 4) {
              try {
                 int index = Integer.parseInt(split[0]);
                 if (index < 0 || index >= vertices.length) continue;
                 float x, y, z;
                 x = Float.parseFloat(split[1]);
                 y = Float.parseFloat(split[2]);
                 z = Float.parseFloat(split[3]);
                 vertices[index][0] = x;
                 vertices[index][1] = y;
                 vertices[index][2] = z;
                 vertexLoadCount++;
              } catch (NumberFormatException e) {
                 // skip this line, it is corrupt
              }
           }
        }

        if (vertexLoadCount != numVertices) return null;
        else return vertices;
     }   
   
	//draw edges by reading edges in, then looking up their end vertices
	//in the 2d Array and using these co-ordinates to draw a quad in the 3d space
	private int[][] drawedges(BufferedReader br, int numEdges, float[][] vertices) throws IOException {
	      if (numEdges < 0) return null;
	      String line;
	      String[] split;
	      int[][] edges = new int[numEdges][2];
	      int edgeLoadCount = 0;

	      while (edgeLoadCount < numEdges && (line = br.readLine()) != null && (split = line.split(",")) != null) {
	         if (split.length == 2) {
	            try {
	               int v1, v2;
	               v1 = Integer.parseInt(split[0]);
	               v2 = Integer.parseInt(split[1]);

	               System.out.println("(" + vertices[v1][0] + "," + vertices[v1][1] + ") " + "(" + vertices[v2][0] + "," + vertices[v2][1] + ") ");;
	               makewall((int) vertices[v1][0],(int) vertices[v1][1],(int) vertices[v2][0],(int) vertices[v2][1]);

	            } catch (NumberFormatException e) {
	               // skip this line, it is corrupt
	            }
	         }
	      }
	      if (edgeLoadCount != numEdges) return null;
	      else return edges;
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
    
