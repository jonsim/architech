import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

import com.jme3.app.*;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.system.JmeSystem;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;

/*This code is mostly from the default application included with JME, SimpleApp.
 * It has been modified so that various features and settings can be manipulated
 * such as turning off FPS read-outs and setting a custom camera start-point
 */
public class ArchApp extends Application {

    private Node rootNode = new Node("Root Node");
    private Node guiNode = new Node("Gui Node");
    Object syncLockObject = new Object();
    private Spatial chair;
    private ArrayList<Geometry> walls = new ArrayList<Geometry>();

    private float secondCounter = 0.0f;
    private BitmapText fpsText;
    private BitmapFont guiFont;
    private StatsView statsView;
    
    private Material grass;
    private Texture grasst;
    private Material wallmat;

    private static MovCam flyCam;
    private boolean showSettings = false;

    private AppActionListener actionListener = new AppActionListener();

    private class AppActionListener implements ActionListener {
        public void onAction(String name, boolean value, float tpf) {
            if (!value)
                return;

            if (name.equals("SIMPLEAPP_Exit")){
                    stop();
                }else if (name.equals("SIMPLEAPP_CameraPos")){
                    if (cam != null){
                       /* Vector3f loc = cam.getLocation();
                        Quaternion rot = cam.getRotation();
                        System.out.println("Camera Position: ("+
                                loc.x+", "+loc.y+", "+loc.z+")");
                        System.out.println("Camera Rotation: "+rot);
                        System.out.println("Camera Direction: "+cam.getDirection());*/
                    }
                }else if (name.equals("SIMPLEAPP_Memory")){
                    BufferUtils.printCurrentDirectMemory(null);
                }
        }
    }

    @Override
    public void start(){
        if (settings == null)
            setSettings(new AppSettings(true));
        if (showSettings){
            if (!JmeSystem.showSettingsDialog(settings))
                return;
        }
        super.start();
    }

    public MovCam getFlyByCamera() {
        return flyCam;
    }

    public Node getGuiNode() {
        return guiNode;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
    }

    public void loadFPSText(){
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        fpsText = new BitmapText(guiFont, false);
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        fpsText.setText("Mother-licking FPS:");
        guiNode.attachChild(fpsText);
    }

    public void loadStatsView(){
        statsView = new StatsView("Statistics View", assetManager, renderer.getStatistics());
//         move it up so it appears above fps text
        statsView.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        guiNode.attachChild(statsView);
    }
    
    public void initCamera(){
        cam = new Camera(settings.getWidth(), settings.getHeight());
        cam.setFrustumPerspective(45f, (float)cam.getWidth() / cam.getHeight(), 1f, 10000f);
        cam.setLocation(new Vector3f(357.61813f, -46.365437f, 546.6056f));
        cam.lookAt(new Vector3f(400f, -50f, 0f), Vector3f.UNIT_Y);
        renderManager = new RenderManager(renderer);
        renderManager.setTimer(timer);
        viewPort = renderManager.createMainView("Default", cam);
        viewPort.setClearEnabled(true);
        // Create a new cam for the gui
        Camera guiCam = new Camera(settings.getWidth(), settings.getHeight());
        guiViewPort = renderManager.createPostView("Gui Default", guiCam);
        guiViewPort.setClearEnabled(false);
    }

    @Override
    public void initialize(){
        super.initialize();
        initCamera();
        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
       // loadFPSText();
       // loadStatsView();
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        if (inputManager != null){
            flyCam = new MovCam(cam);
            flyCam.setMoveSpeed(100f);
            flyCam.registerWithInput(inputManager);

            if (context.getType() == Type.Display)
                inputManager.addMapping("SIMPLEAPP_Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));
            
            inputManager.addMapping("SIMPLEAPP_CameraPos", new KeyTrigger(KeyInput.KEY_C));
            inputManager.addMapping("SIMPLEAPP_Memory", new KeyTrigger(KeyInput.KEY_M));
            inputManager.addListener(actionListener, "SIMPLEAPP_Exit",
                                     "SIMPLEAPP_CameraPos", "SIMPLEAPP_Memory");
        }
        // call user code
        
	    //grass = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
	    grasst = assetManager.loadTexture("req/grass.jpg");
	    
	   grass = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
       //grass.setColor("m_Color", ColorRGBA.White);
       grass.setFloat("m_Shininess", 5f); 
        
		wallmat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
		wallmat.setTexture("m_ColorMap", assetManager.loadTexture("req/wall1.jpg"));
        
        
        simpleInitApp();
    }

    @Override
    public void update() {
		synchronized(syncLockObject) {
	        if (speed == 0 || paused)
	            return;
	        
	        super.update();
	        float tpf = timer.getTimePerFrame() * speed;
	
	        //secondCounter += timer.getTimePerFrame();
	       // int fps = (int) timer.getFrameRate();
	       // if (secondCounter >= 1.0f){
	         //   fpsText.setText("FPS: "+fps);
	         //   secondCounter = 0.0f;
	       // }
	
	        // update states
	        stateManager.update(tpf);
	
	        // simple update and root node
	        simpleUpdate(tpf);
	        rootNode.updateLogicalState(tpf);
	        guiNode.updateLogicalState(tpf);
	        rootNode.updateGeometricState();
	        guiNode.updateGeometricState();
	
	        // render states
	        stateManager.render(renderManager);
	        renderManager.render(tpf);
	        simpleRender(renderManager);
	        stateManager.postRender();
		}
    }

    public void simpleInitApp() {
		flyCam.setDragToRotate(true);
		addbackg();
	    //add a sun
	    DirectionalLight sun = new DirectionalLight();
	    sun.setDirection(new Vector3f(10,-50,0).normalizeLocal());
	    sun.setColor(ColorRGBA.White);
	    rootNode.addLight(sun);
    }

    public void simpleUpdate(float tpf){
    }

    public void simpleRender(RenderManager rm){
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
    	          length += 2;
    	          rotation = (float) -(Math.atan((double) (leny) / (lenx)));
                if(y2>y1 & x1>x2)  {rotation += Math.PI;}
                if(y2<y1 & x1>x2)  {rotation += Math.PI;}
		}}

		//Draw a quad between the two given verticies using dist
		//and angles calculate above
		wall = new Geometry ("Box", new Quad(length,100));
		wall.setMaterial(wallmat);
		wall.setLocalTranslation(new Vector3f(x1, -100, y1));
		wall.rotate(0f, rotation, 0f);
		rootNode.attachChild(wall);
		walls.add(wall);

		//Double up the quad
		wall = new Geometry ("Box", new Quad(length,100));
		wall.setMaterial(wallmat);
		wall.setLocalTranslation(new Vector3f(x2, -100, y2));
		wall.rotate(0f, (float) (rotation + Math.PI), 0f);
		rootNode.attachChild(wall);
		walls.add(wall);
    	}

    void addbackg(){
		//add the grassy area
	    Quad blah = new Quad(4000,4000);
		Geometry geom = new Geometry("Box", blah);
	    //grass.setTexture("m_ColorMap", grasst);
	    geom.setMaterial(grass);geom.setLocalTranslation(new Vector3f(2000,-100,-500));
		geom.rotate((float) -Math.toRadians(90),(float) Math.toRadians(180),0f );
	    rootNode.attachChild(geom);

	    //add the sky image
	    rootNode.attachChild(SkyFactory.createSky(
	            assetManager, "req/BrightSky.dds", false));
    }
    
	void clearall()
    {
		rootNode.detachAllChildren();
		addbackg();
    }

	void updateroot(){
		rootNode.updateGeometricState();
	}
	
	public void updateall(Edge[] edges)
	{
	          clearall();
	          addedges(edges);
	}
	
	//void removechair(){
		//if(chair!=null){rootNode.detachChild(chair);}
	//}
	
	void addchair(Point center, String name){
		//String path = "req/" + name + ".obj"
	    chair = assetManager.loadModel("req/Chair.obj");
	    chair.scale(15f, 10f, 15f);
	    chair.rotate((float) -(0.5* Math.PI),(float) -(0.5* Math.PI),0);
	    chair.setLocalTranslation(center.x+15,-100,center.y-30);
	    rootNode.attachChild(chair);
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