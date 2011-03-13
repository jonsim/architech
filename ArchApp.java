import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;

import com.jme3.app.Application;
import com.jme3.app.StatsView;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeContext.Type;
import com.jme3.system.JmeSystem;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;


/*This code is mostly from the default application included with JME, SimpleApp.
 * It has been modified so that various features and settings can be manipulated
 * such as turning off FPS read-outs and setting a custom camera start-point
 */
public class ArchApp extends Application
{
	/**********************GLOBAL VARIABLES**********************/
	// toggle globals
	private static final boolean shadowing = false;  // determines whether or not to render shadows
	private static final boolean physics   = true;  // determines whether or not to calculate physics (not 100% implemented)
	private static final boolean overlay   = false;  // displays overlay
	
	// lighting globals
	private static final short[]  DAY_BRIGHTNESS = {245, 240, 200};	// RGB colour (0-255) of the sun light
	private static final float    DAY_AMBIENCE = 0.7f;	// amount of ambient light in the scene (0 is none)
	private static final Vector3f DAY_ANGLE = new Vector3f(0.8f, -2, -0.2f);  // vector direction of the sun
	private static final float    DAY_SHADOW_INTENSITY = 0.5f;  // 0 = no shadows, 1 = pitch black shadows
	private static Spatial        DAY_MAP = null;  // must be initialised in SimpleInitApp() due to dependence on assetManager
	private static final short[]  NIGHT_BRIGHTNESS = {30, 30, 30};
	private static final float    NIGHT_AMBIENCE = 0.8f;
	private static final Vector3f NIGHT_ANGLE = new Vector3f(-0.2f, -0.8f, 0.6f);
	private static final float    NIGHT_SHADOW_INTENSITY = 0;
	private static Spatial        NIGHT_MAP = null;
	private static Vector3f lookvec = new Vector3f(-540f, -50f, 360f);
	private static Vector3f startvec = new Vector3f(590, -15, 80);
	private boolean day = true;  // true = day, false = night
	private DirectionalLight sun;
	private AmbientLight ambient;
    private PssmShadowRenderer psr;

	// physics globals
    private BulletAppState bulletAppState;
	private CharacterControl player;
	private Vector3f walkDirection = new Vector3f();
	private boolean left = false, right = false, up = false, down = false;

	// node globals
    private Node rootNode = new Node("Root Node");
    private Node guiNode = new Node("Gui Node");
    private final Object syncLockObject = new Object();

    // overlay globals
    private BitmapText fpsText;
    private BitmapFont guiFont;
    private StatsView statsView;
    
    // material globals
    private Material grass;
    private Material wallmat;
    
    // camera globals
    private static MovCam flyCam;
    private boolean showSettings = false;

    // miscellaneous globals
    private AppActionListener actionListener = new AppActionListener();
    private boolean isInitComplete = false;
    private Main main;

    
    
    
	/**********************MAIN FUNCTION**********************/
    
    ArchApp(Main main)
    {
    	super();
    	this.main = main;
    }
    
    
    
    
	/**********************ACTION LISTNER**********************/

    private class AppActionListener implements ActionListener
    {
        public void onAction(String name, boolean value, float tpf)
        {
            if (name.equals("PLAYER_Left"))
            	if (value)
            		left = true;
            	else
            		left = false;
            else if (name.equals("PLAYER_Right"))
            	if (value)
            		right = true;
            	else
            		right = false;
            else if (name.equals("PLAYER_Up"))
            	if (value)
            		up = true;
            	else
            		up = false;
            else if (name.equals("PLAYER_Down"))
            	if (value)
            		down = true;
            	else
            		down = false;
            else if (name.equals("PLAYER_Jump"))
            	player.jump();
            else if (name.equals("SIMPLEAPP_Exit"))
            	stop();
            else if (name.equals("SIMPLEAPP_ToggleDay"))
            	if (value)
            		toggleDay();
            else if (name.equals("SIMPLEAPP_CameraPos"))
            	if (cam != null)
            	{
	                Vector3f loc = cam.getLocation();
	                Quaternion rot = cam.getRotation();
	                System.out.println("Camera Position: ("+loc.x+", "+loc.y+", "+loc.z+")");
	                System.out.println("Camera Rotation: "+rot);
	                System.out.println("Camera Direction: "+cam.getDirection());
	            }
            else if (name.equals("SIMPLEAPP_Memory"))
            	BufferUtils.printCurrentDirectMemory(null);
        }
    }
    
    
    
    
	/**********************INITIALISATION FUNCTIONS**********************/

    @Override
    public void start()
    {
        if (settings == null)
            setSettings(new AppSettings(true));
        if (showSettings)
            if (!JmeSystem.showSettingsDialog(settings, true))
                return;
        super.start();
    }

    
    
    @Override
    public void update()
    {
		synchronized(syncLockObject)
		{
	        if (speed == 0 || paused)
	            return;
	        
	        super.update();
	        float tpf = timer.getTimePerFrame() * speed;	

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
    
    
    
    @Override
    public void initialize()
    {
        super.initialize();
        if (!physics)
        	initCamera();
        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        if (overlay)
        {
            loadFPSText();
            loadStatsView();        	
        }
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        if (inputManager != null)
        {
            flyCam = new MovCam(cam);
        	flyCam.setMoveSpeed(20);
            flyCam.registerWithInput(inputManager);

            if (context.getType() == Type.Display)
                inputManager.addMapping("SIMPLEAPP_Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));
            
            inputManager.addMapping("SIMPLEAPP_CameraPos", new KeyTrigger(KeyInput.KEY_C));
            inputManager.addMapping("SIMPLEAPP_Memory", new KeyTrigger(KeyInput.KEY_M));
            inputManager.addMapping("SIMPLEAPP_ToggleDay", new KeyTrigger(KeyInput.KEY_N));
            inputManager.addListener(actionListener, "SIMPLEAPP_Exit", "SIMPLEAPP_ToggleDay", "SIMPLEAPP_CameraPos", "SIMPLEAPP_Memory");
            
            inputManager.addMapping("PLAYER_Left",  new KeyTrigger(KeyInput.KEY_A));
            inputManager.addMapping("PLAYER_Right", new KeyTrigger(KeyInput.KEY_D));
            inputManager.addMapping("PLAYER_Up",    new KeyTrigger(KeyInput.KEY_W));
            inputManager.addMapping("PLAYER_Down",  new KeyTrigger(KeyInput.KEY_S));
            inputManager.addMapping("PLAYER_Jump",  new KeyTrigger(KeyInput.KEY_SPACE));
            inputManager.addListener(actionListener, "PLAYER_Left", "PLAYER_Right", "PLAYER_Up", "PLAYER_Down", "PLAYER_Jump");
        }
        
        // call user code
        setupMaterials();
        simpleInitApp();
        
        if (main.frontEnd != null)
        	tabChangedIgnoreInitComplete(main.frontEnd.getCurrentCoords());
        isInitComplete = true;
    }
    
    
    
    public void initCamera()
    {
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

    
    
    public void simpleInitApp()
    {
    	// initialise the physics components
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        // setup the other components
		flyCam.setDragToRotate(true);
		setupScene();
		setupLighting();
        setupPlayer();
        cam.lookAt(lookvec, Vector3f.UNIT_Y);
    }

    
    
    public void simpleUpdate(float tpf)
    {
        Vector3f camDir = cam.getDirection().clone().multLocal(2);
        Vector3f camLeft = cam.getLeft().clone().multLocal(2);
        walkDirection.set(0, 0, 0);
        if (left)
        	walkDirection.addLocal(camLeft);
        if (right)
        	walkDirection.addLocal(camLeft.negate());
        if (up)
        	walkDirection.addLocal(camDir);
        if (down)
        	walkDirection.addLocal(camDir.negate());
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }

    
    
    public void simpleRender(RenderManager rm)
    {
    }
    
    
    
    
    
	/**********************SETUP FUNCTIONS**********************/

    private void setupMaterials ()
    {
        grass = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        grass.setTexture("DiffuseMap", assetManager.loadTexture("req/floor.jpg"));
        /*grass.setBoolean("UseMaterialColors", true);
        grass.setColor("Diffuse",  ColorRGBA.White);
        grass.setColor("Specular",  ColorRGBA.Red);
        grass.setColor("Ambient",  ColorRGBA.Blue);*/
        grass.setFloat("Shininess", 10);

        wallmat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		wallmat.setTexture("DiffuseMap", assetManager.loadTexture("req/wall1.jpg"));
        wallmat.setFloat("Shininess", 1000);    	
    }
    
    

    PointLight pl1, pl2;
    private void setupScene()
    {	    
	    //add the floor
	    Geometry geom = new Geometry("Box", new Quad(4000,4000));
	    geom.setMaterial(grass);
	    geom.setShadowMode(ShadowMode.Receive);
	    geom.setLocalTranslation(new Vector3f(2102,-100,-902));
	    geom.rotate((float) -Math.toRadians(90),(float) Math.toRadians(180),0f );
	    addToPhysics(geom);
	    rootNode.attachChild(geom);	    
	    
	    Box cei = new Box( new Vector3f(420,0,150), 180,0.1f,90);
        Geometry top = new Geometry("Box", cei);
        top.setMaterial(grass);
	    rootNode.attachChild(top);
	    cei = new Box( new Vector3f(330,0,300), 90,0.1f,60);
        top = new Geometry("Box", cei);
        top.setMaterial(grass);
	    rootNode.attachChild(top);

	    // add lightbulbs
	    Spatial light1 = assetManager.loadModel("req/lightbulb/lightbulb.obj");
		light1.scale(4, 4, 4);
		light1.rotate(-FastMath.PI,0,0);
		light1.setLocalTranslation(270, -30, 80);
		rootNode.attachChild(light1);
		
	    Spatial light2 = assetManager.loadModel("req/lightbulb/lightbulb.obj");
		light2.scale(4, 4, 4);
		light2.rotate(-FastMath.PI,0,0);
		light2.setLocalTranslation(380, -25, 290);
		rootNode.attachChild(light2);

        pl1 = new PointLight();
        pl1.setColor(new ColorRGBA(2, 2, 1.5f, 0));
        pl1.setRadius(250);
        pl1.setPosition(new Vector3f(270, -30, 80));
        
        pl2 = new PointLight();
        pl2.setColor(new ColorRGBA(2, 2, 1.5f, 0));
        pl2.setRadius(250);
        pl2.setPosition(new Vector3f(380, -25, 290));

        setupSky();
    }
    
    
    
    public void setupLighting ()
    {
    	// add shadow renderer
    	if (shadowing)
    	{
	        rootNode.setShadowMode(ShadowMode.Off);
	        psr = new PssmShadowRenderer(assetManager, 1024, 4);
	        viewPort.addProcessor(psr);
        	psr.setDirection(DAY_ANGLE);
        	psr.setShadowIntensity(DAY_SHADOW_INTENSITY);
    	}

		// add directional lighting (sun)
        sun = new DirectionalLight();
        sun.setDirection(DAY_ANGLE);
        sun.setColor(new ColorRGBA((float) DAY_BRIGHTNESS[0]/255, (float) DAY_BRIGHTNESS[1]/255, (float) DAY_BRIGHTNESS[2]/255, 1));
        rootNode.addLight(sun); 
        
        // add ambient lighting
        ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(DAY_AMBIENCE));
        rootNode.addLight(ambient);
    }
    
    
    
    public void setupSky()
    {
        DAY_MAP = SkyFactory.createSky(assetManager, "req/SkyDay.dds", false);
        NIGHT_MAP = SkyFactory.createSky(assetManager, "req/SkyNight.dds", false);
        rootNode.attachChild(DAY_MAP);
    }
    
    
    
    private void setupPlayer ()
    {
        //flyCam.setMoveSpeed(200);
     
        // player collision
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(20, 70, 1);
        player = new CharacterControl(capsuleShape, 0.01f);
        player.setJumpSpeed(20);
        player.setFallSpeed(160);
        player.setGravity(400);
        player.setPhysicsLocation(startvec);
        
        bulletAppState.getPhysicsSpace().add(player);    	
    }
    
    
    
    
    
	/**********************DAY/NIGHT/LIGHTING FUNCTIONS**********************/

    public void toggleDay()
    {
    	if (!day)
    	{
            rootNode.attachChild(DAY_MAP);
            sun.setDirection(DAY_ANGLE);
            sun.setColor(new ColorRGBA((float) DAY_BRIGHTNESS[0]/255, (float) DAY_BRIGHTNESS[1]/255, (float) DAY_BRIGHTNESS[2]/255, 1));
            ambient.setColor(ColorRGBA.White.mult(DAY_AMBIENCE));
            if (shadowing)
            {
            	psr.setDirection(DAY_ANGLE);
            	psr.setShadowIntensity(DAY_SHADOW_INTENSITY);
            }
            turnOffLights();
            day = true;
    	}
    	else
    	{
        	rootNode.detachChild(DAY_MAP);
            rootNode.attachChild(NIGHT_MAP);
            sun.setDirection(NIGHT_ANGLE);
            sun.setColor(new ColorRGBA((float) NIGHT_BRIGHTNESS[0]/255, (float) NIGHT_BRIGHTNESS[1]/255, (float) NIGHT_BRIGHTNESS[2]/255, 1));
            ambient.setColor(ColorRGBA.White.mult(NIGHT_AMBIENCE));
            if (shadowing)
            {
	            psr.setDirection(NIGHT_ANGLE);
	        	psr.setShadowIntensity(NIGHT_SHADOW_INTENSITY);
            }
            turnOnLights();
            day = false;
    	}
		// Stops you having to click to update the 3D
		((JmeCanvasContext) this.getContext()).getCanvas().requestFocus();
    }
    
    
    
    private void turnOnLights()
    {
    	rootNode.addLight(pl1);
    	rootNode.addLight(pl2);
    }
    
    
    
    private void turnOffLights()
    {
    	rootNode.removeLight(pl1);
    	rootNode.removeLight(pl2);
    }
    
    
    
    
    
    /**********************PHYSICS FUNCTIONS**********************/

    private void addToPhysics (Spatial s)
    {
    	RigidBodyControl bla;
    	CollisionShape sShape = CollisionShapeFactory.createMeshShape(s);
        bla = new RigidBodyControl(sShape, 0);
        s.addControl(bla);
        
        bulletAppState.getPhysicsSpace().add(bla);
    }
    
    
    
    private void addToPhysics (Geometry g)
    {
    	RigidBodyControl bla;
    	MeshCollisionShape gShape = CollisionShapeFactory.createSingleMeshShape(g);
        bla = new RigidBodyControl(gShape, 0);
        g.addControl(bla);
        
        bulletAppState.getPhysicsSpace().add(bla);
    }
    
    
    
    
    
	/**********************OTHER FUNCTIONS**********************/
    
	private void clearall()
    {
		rootNode.detachAllChildren();
		setupScene();
    }
	
	
	
	
	
	/**********************WALL GEOMETRY FUNCTIONS**********************/

	private class WallGeometry
	{
		ArrayList<Geometry> geom = new ArrayList<Geometry>();
	}
	private final HashMap<Coords, HashMap<Edge, WallGeometry> > tabEdgeGeometry
		= new HashMap<Coords, HashMap<Edge, WallGeometry> >();
	private final HashMap<Coords, HashMap<Furniture, Spatial> > tabFurnitureSpatials
		= new HashMap<Coords, HashMap<Furniture, Spatial> >();

	
	
	
	
	/**********************TAB FUNCTIONS**********************/
	
	/** This should be called after the given Coords is no longer used i.e.
      *  immediately after, not immediately before deletion. It forgets about
      *  the edges. If called before, then the entry might be recreated */
	void tabRemoved(Coords tab)
	{
		HashMap<Edge, WallGeometry> edges = tabEdgeGeometry.remove(tab);
		if (edges != null)
			edges.clear();

		HashMap<Furniture, Spatial> furniture = tabFurnitureSpatials.remove(tab);
		if (furniture != null)
			furniture.clear();
	}

	
	
	/** Public function to prepare edges for the given coords. If these coords haven't 	*
	 *  been seen before then new objects will be created for it.						*/
	void tabChanged(Coords newTab)
	{
		if (!isInitComplete) return;
		tabChangedIgnoreInitComplete(newTab);

		// Stops you having to click to update the 3D (for tab changes)
		((JmeCanvasContext) this.getContext()).getCanvas().requestFocus();
	}

	
	
	/** Edges is the set of edges associated with those coords, likewise for furniture	*
	 *  if these coords havn't been seen before both edges and furniture				*
	 *  will be null (its a brand new tab). Either both will be null or not,			*
	 *  never one or the other. If the tab has never been seen before then new			*
	 *  objects will be created for it													*/
	private void tabChangedIgnoreInitComplete(Coords newTab)
	{
		synchronized(syncLockObject)
		{
			// no tab selected
			if (newTab == null)
			{
				clearall();
				return;
            }

            // Do edges, if this tab already exists, this will not be null and it
            // will get redrawn from below, not recreated!
            HashMap<Edge, WallGeometry> edges = tabEdgeGeometry.get(newTab);
            if (edges == null)
            {
            	// make a brand new edge container for the new tab and add all
            	// the edges from the given coords
            	edges = new HashMap<Edge, WallGeometry>();
            	for (Edge e : newTab.getEdges())
            		edges.put(e, makewall(e));

            	tabEdgeGeometry.put(newTab, edges);
            }

            // Do furniture, if this tab already exists, this will not be null and it
            // will get redrawn from below, not recreated!
            HashMap<Furniture, Spatial> furniture = tabFurnitureSpatials.get(newTab);
            if (furniture == null)
            {
            	// make a brand new furniture container for the new tab and add all
            	// the furniture from the given coords
            	furniture = new HashMap<Furniture, Spatial>();
            	for (Furniture f : newTab.getFurniture())
            		furniture.put(f, addfurniture(f));

            	tabFurnitureSpatials.put(newTab, furniture);
            }

            //redraw everything for this tab as the tab has changed
            clearall();
            redrawAllEdges(edges);
            redrawAllFurniture(furniture);
		}
	}

	
	
	
	
	/**********************EDGE FUNCTIONS**********************/

	/** Adds the given edge. Returns if Coords c is not known yet or if e is already
      * added */
	void addEdge(Coords c, Edge e)
	{
		if (c == null || e == null)
			throw new IllegalArgumentException("null");

		synchronized(syncLockObject)
		{
			HashMap<Edge, WallGeometry> edges = tabEdgeGeometry.get(c);
			if (edges == null)
				return;

			WallGeometry wall = edges.get(e);
			if (wall == null)
			{
				wall = makewall(e); // make the new wall
				edges.put(e, wall);               
			
				Iterator itr = wall.geom.iterator();
				while(itr.hasNext())
					rootNode.attachChild((Geometry) itr.next());
			}
		}
	}

	
	
	/** Removes the given edge. Returns if Coords c is not known yet or if e is not
      *  known */
	void removeEdge(Coords c, Edge e)
	{
		if (c == null || e == null)
			throw new IllegalArgumentException("null");
         
		synchronized(syncLockObject)
		{
			HashMap<Edge, WallGeometry> edges = tabEdgeGeometry.get(c);
			if (edges == null)
				return;

			WallGeometry wall = edges.get(e);
			if (wall != null)
			{
				edges.remove(e);
				Iterator itr = wall.geom.iterator();
				while(itr.hasNext())
					rootNode.detachChild((Geometry) itr.next());
			}
		}
	}

	
	
	/** Goes through all the walls in the given hashmap and adds them to the rootNode */
	private void redrawAllEdges(HashMap<Edge, WallGeometry> edges)
	{
		Collection<WallGeometry> walls = edges.values();
		Iterator<WallGeometry> iterator = walls.iterator();
		WallGeometry wall;
		while (iterator.hasNext())
		{
			wall = iterator.next();
			Iterator itr = wall.geom.iterator();
			while(itr.hasNext())
				rootNode.attachChild((Geometry) itr.next());		
		}
	}

   
	
	
	
	/** Moves the given edge. Returns if Coords c is not known yet or if e is not
      *  known */
	void updateEdgeChanged(Coords c, Edge e)
	{
		if (c == null || e == null)
			throw new IllegalArgumentException("null");
         
		synchronized(syncLockObject)
		{
			HashMap<Edge, WallGeometry> edges = tabEdgeGeometry.get(c);
			if (edges == null)
				return;

			WallGeometry wall = edges.get(e);
			if (wall != null)
				updatewall(wall, e);
		}
	} // constructs a wall between (x1,y1) and (x2,y2), doesn't add it to rootnode
	
	
	

	
	/**********************WALL FUNCTIONS**********************/

    private WallGeometry makewall(Edge e)
    {   
    	int x1 = (int) e.getV1().getX();
    	int y1 = (int) e.getV1().getY();
    	int x2 = (int) e.getV2().getX();
    	int y2 = (int) e.getV2().getY();
    	float ctrlx = (int) e.getCtrlX();
    	float ctrly = (int) e.getCtrlY();
    	int straight = 0;
    	int xx = 0, yy = 0;
    	
    	if (ctrlx     == ((x1 + x2) / 2))
    		xx++;
    	if (ctrlx+1.0 == ((x1 + x2) / 2))
    		xx++;
    	if (ctrlx-1.0 == ((x1 + x2) / 2))
    		xx++;
    	if (ctrly     == ((y1 + y2) / 2))
    		yy++;
    	if (ctrly+1.0 == ((y1 + y2) / 2))
    		yy++;
    	if (ctrly-1.0 == ((y1 + y2) / 2))
    		yy++;
    	if (xx > 0 & yy > 0)
    		straight=1;
    	WallGeometry wallGeometry = new WallGeometry();
    	if (straight == 0)
    	{
    		//CURVED LINE
    		QuadCurve2D qcurve = e.getqcurve();			
    		recurvsion(wallGeometry,qcurve,4);
    	}
    	else
    	{	    
    		/*//STRAIGHT LINE
    		//Furniture[] dws = e.getDoorWindow(); 	
    		//if(dws!=null){
    		int doorwidth = 20;
    		int doorpanel = 20;
			//if(x1==180 & y1==0 & x2==300 & y2==0){
	    		//Furniture[] dws = new Furniture[1];
	    			    		
	    		int cx=0,cy=0;
	    		if(x2>x1){cx = x1 + Math.abs((x2 -x1)/2);}
	    		if(x2==x1) {cx = x2;}
	    		if(x1>x2){cx = x1 - Math.abs((x2 -x1)/2);}
	    		if(y2>y1){cy = y1 + Math.abs((y2 -y1)/2);}
	    		if(y2==y1){cy=y2;}
	    		if(y1>y2){cy = y1 - Math.abs((y2 -y1)/2);}
	    		
	    		/*dws[0] = new Furniture(new FurnitureSQLData(2, 2f, 2f, 2,"bing"),new Point(cx,cy),null);
	    		double dx1,dy1,dx2 ,dy2;
	    		double o = Math.abs(y2 - y1);
	    		double a = Math.abs(x2 - x1);
	    		if(o==0){
	    			dx1 = dws[0].getRotationCenter().getX()-doorwidth;	    			
	    			dy1 = y1;
	    			dx2 = dws[0].getRotationCenter().getX()+doorwidth;
	    			dy2 = y2;
	    			if(x1>x2){double temp = dx1; dx1 = dx2; dx2 = temp;}
	    		}else{if(a==0){
	    			dx1 = x1;
	    			dy1 = dws[0].getRotationCenter().getY()-doorwidth;
	    			dx2 = x2;
	    			dy2 = dws[0].getRotationCenter().getY()+doorwidth;
	    			if(y1>y2){double temp = dy1; dy1 = dy2; dy2 = temp;}
	    		}else{  		
	    		System.out.println(o/a);
	    		double theta = Math.atan(o/a);
	    		
	    		double xtri =  doorwidth * (Math.cos(theta));
	    		double ytri =  doorwidth * (Math.sin(theta));
	    		if(x2<x1){xtri = -xtri;}
	    		if(y2<y1){ytri = -ytri;}
	    		dx1 = dws[0].getRotationCenter().getX()-xtri;
	    		dy1 = dws[0].getRotationCenter().getY()-ytri;
	    		dx2 = dws[0].getRotationCenter().getX()+xtri;
	    		dy2 = dws[0].getRotationCenter().getY()+ytri;
	    		
	    		double dx1,dy1,dx2 ,dy2;
	    		double o = Math.abs(y2 - y1);
	    		double a = Math.abs(x2 - x1);
	    		if(o==0){
	    			dx1 = cx-doorwidth;	    			
	    			dy1 = y1;
	    			dx2 = cx+doorwidth;
	    			dy2 = y2;
	    			if(x1>x2){double temp = dx1; dx1 = dx2; dx2 = temp;}
	    		}else{if(a==0){
	    			dx1 = x1;
	    			dy1 = cy-doorwidth;
	    			dx2 = x2;
	    			dy2 = cy+doorwidth;
	    			if(y1>y2){double temp = dy1; dy1 = dy2; dy2 = temp;}
	    		}else{  		
	    		double theta = Math.atan(o/a);
	    		
	    		double xtri =  doorwidth * (Math.cos(theta));
	    		double ytri =  doorwidth * (Math.sin(theta));
	    		if(x2<x1){xtri = -xtri;}
	    		if(y2<y1){ytri = -ytri;}
	    		dx1 = cx-xtri;
	    		dy1 = cy-ytri;
	    		dx2 = cx+xtri;
	    		dy2 = cy+ytri;

	    		}}
	    		drawline(wallGeometry,(int)x1,(int)dx1,(int)y1,(int)dy1,100,-100,1);
	    		drawline(wallGeometry,(int)dx2,(int)x2,(int)dy2,(int)y2,100,-100,1);
	    		drawline(wallGeometry,(int)dx1,(int)dx2,(int)dy1,(int)dy2,doorpanel,-doorpanel,0);*/
    		
	    		
			//}
    		//}
			//else{			

    		drawline(wallGeometry,x1,x2,y1,y2,100,-100,1); 
			//}
    	}

    	return wallGeometry;
	}
    
	/** Moves the two wall planes to the new position given by edge e */
	private void updatewall(WallGeometry wallGeometry, Edge e)
	{
		Iterator itr = wallGeometry.geom.iterator();
		while(itr.hasNext())
			rootNode.detachChild((Geometry) itr.next());		
	
		// move the wall, this makes a completely new one (for now)!
		WallGeometry completelyNew = makewall(e);
		wallGeometry.geom.clear();	
		itr = completelyNew.geom.iterator();
		while(itr.hasNext())
			wallGeometry.geom.add((Geometry) itr.next());
	
		itr = wallGeometry.geom.iterator();
		while(itr.hasNext())
			rootNode.attachChild((Geometry) itr.next());
	}
    
    private int recurvsion(WallGeometry top, QuadCurve2D curve,int level)
    {
    	if (level == 0)
    	{
    		drawline(top, (int)curve.getX1(), (int)curve.getX2(), (int)curve.getY1(), (int)curve.getY2(),100,-100,1);
    		return -1;
    	}
    	else
    	{
    		int nlevel = level - 1;
    		QuadCurve2D left =  new QuadCurve2D.Float();
    		QuadCurve2D right = new QuadCurve2D.Float();
    		curve.subdivide(left, right);
    		recurvsion(top, left, nlevel);
    		recurvsion(top, right, nlevel);
    		return 0;
    	}
    }
    
    private void drawline(WallGeometry wallGeometry, int x1, int x2, int y1, int y2,int height,int disp,int coll)
    {
    	int length, leny, lenx = 0;
    	float rotation = 0;
		//work out the distances and angles required
		lenx = x2 - x1;
		leny = y2 - y1;
		if (lenx == 0)
		{
			length = leny;
			rotation =- (float) FastMath.HALF_PI;
		}
		else
		{
			if(leny == 0)
			{
				length = lenx;
				rotation = 0;
			}
			else
			{
				length = (int) Math.sqrt((Math.pow(lenx,2) + Math.pow(leny,2)));
				length += 2;
				rotation = (float) -(Math.atan((double) (leny) / (lenx)));
				if(y2 > y1 & x1 > x2)
					rotation += FastMath.PI;
				if(y2 < y1 & x1 > x2)
					rotation += FastMath.PI;
			}
		}
		
		// Draw a quad between the 2 given vertices
		Geometry adding = new Geometry ("Box", new Quad(length, height));
		adding.setLocalTranslation(new Vector3f(x1, disp, y1));
		adding.rotate(0f, rotation, 0f);
		adding.setMaterial(wallmat);
    	if (shadowing)
    		adding.setShadowMode(ShadowMode.CastAndReceive);
    	if (physics & coll == 1)
    		addToPhysics(adding);
		wallGeometry.geom.add(adding);

		// Double up the quad
		adding = new Geometry ("Box", new Quad(length,height));
		adding.setLocalTranslation(new Vector3f(x2, disp, y2));
		adding.rotate(0f, (float) (rotation + FastMath.PI), 0f);
		adding.setMaterial(wallmat);
		if (shadowing)
			adding.setShadowMode(ShadowMode.CastAndReceive);
		if (physics & coll == 1)
			addToPhysics(adding);
    	wallGeometry.geom.add(adding);
    	return; 
    }
	

	
	
	
	/**********************FURNITURE FUNCTIONS**********************/

	/** Adds the given furniture. Returns if Coords c is not known yet or if f is already
      *  added */
	void addFurniture(Coords c, Furniture f)
	{
		if (c == null || f == null)
			throw new IllegalArgumentException("null");

		synchronized(syncLockObject)
		{
			HashMap<Furniture, Spatial> furniture = tabFurnitureSpatials.get(c);
			if (furniture == null)
				return;

			Spatial spatial = furniture.get(f);
			if (spatial == null)
			{
				spatial = addfurniture(f);
				furniture.put(f, spatial);
				rootNode.attachChild(spatial);
			}
		}
	}

	
	
	private Spatial addfurniture(Furniture f)
	{
        Spatial furn = null;
        //Material furn_mat;
		Point center = f.getRotationCenter();
        String name = f.getObjPath();
        
        // if object specified does not exist
        if(name == null || name.equals("none"))
        	furn = assetManager.loadModel("req/armchair/armchair.obj");
        else
        {
        	String path = "req/" + name.substring(0,name.length()-4) +"/" +name;
            furn = assetManager.loadModel(path);
        }
        
        // model settings
        furn.scale(5, 5, 5);
        furn.rotate(0f, - FastMath.HALF_PI,0f);        
        furn.setLocalTranslation(center.x,-100f,center.y);
    	if (shadowing)
    		furn.setShadowMode(ShadowMode.CastAndReceive);
    	if (physics)
    		addToPhysics(furn);

        return furn;
	}

	
	
	/** Removes the given furniture. Returns if Coords c is not known yet or if f is not
      *  known */
	void removeFurniture(Coords c, Furniture f)
	{
		if (c == null || f == null)
			throw new IllegalArgumentException("null");
         
		synchronized(syncLockObject)
		{
			HashMap<Furniture, Spatial> furniture = tabFurnitureSpatials.get(c);
			if (furniture == null)
				return;

			Spatial spatial = furniture.get(f);
			if (spatial != null)
			{
				furniture.remove(f);
				rootNode.detachChild(spatial);
			}
		}
	}

	
	
    /** Goes through all the furniture in the given hashmap and adds them to the rootNode */
	private void redrawAllFurniture(HashMap<Furniture, Spatial> furniture)
	{
		Collection<Spatial> spatials = furniture.values();
		Iterator<Spatial> iterator = spatials.iterator();
		while (iterator.hasNext())
		{
			rootNode.attachChild(iterator.next());
		}
	}	
	
	/** Moves the given spatial to the new position of furniture f */
	private void updatefurniture(Spatial spatial, Furniture f)
	{
		// move the furniture to the new position!
		Point center = f.getRotationCenter();
		spatial.setLocalTranslation(center.x,-100f,center.y);
		//take care of its rotation
		double rotation = f.getRotation();
		Quaternion q = spatial.getLocalRotation();
		rotation *= 0.5;
		float sina = FastMath.sin(((float)rotation));
		float x = (float)(0.0 * sina);
		float y = (float)(1.0 * sina);
		float z = (float)(0.0 * sina);
		float w = (float) FastMath.cos((float)rotation);
		Quaternion c = new Quaternion(w,x,y,z);
		spatial.setLocalRotation(c);
		spatial.rotate(0f,-FastMath.HALF_PI,-FastMath.PI);
	}
	
	
	/** Moves the given furniture. Returns if Coords c is not known yet or if f is not
      *  known */
	void updateFurnitureChanged(Coords c, Furniture f)
	{
		if (c == null || f == null)
			throw new IllegalArgumentException("null");
         
		synchronized(syncLockObject)
		{
			HashMap<Furniture, Spatial> furniture = tabFurnitureSpatials.get(c);
			if (furniture == null)
				return;

			Spatial spatial = furniture.get(f);
			if (spatial != null)
				updatefurniture(spatial, f);
		}
	}

    
    
    
    
	/**********************OVERLAY FUNCTIONS**********************/

    public void loadFPSText()
    {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        fpsText = new BitmapText(guiFont, false);
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        fpsText.setText("Mother-licking FPS:");
        guiNode.attachChild(fpsText);
    }

    
    
    public void loadStatsView()
    {
        statsView = new StatsView("Statistics View", assetManager, renderer.getStatistics());
        // move it up so it appears above fps text
        statsView.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        guiNode.attachChild(statsView);
    }
	

	
	
	
	/**********************GET FUNCTIONS**********************/

    public MovCam getFlyByCamera()
    {
        return flyCam;
    }

    
    
    public Node getGuiNode()
    {
        return guiNode;
    }

    
    
    public Node getRootNode()
    {
        return rootNode;
    }

    
    
    public boolean isShowSettings()
    {
        return showSettings;
    }

    
    
    public void setShowSettings(boolean showSettings)
    {
        this.showSettings = showSettings;
    }
}