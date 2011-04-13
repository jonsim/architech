import java.awt.Color;
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
import com.jme3.material.RenderState.BlendMode;
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
	private static final float    DAY_AMBIENCE = 2.0f;	// amount of ambient light in the scene (0 is none)
	private static final Vector3f DAY_ANGLE = new Vector3f(0.8f, -2, -0.2f);  // vector direction of the sun
	private static final float    DAY_SHADOW_INTENSITY = 0.5f;  // 0 = no shadows, 1 = pitch black shadows
	private static Spatial        DAY_MAP = null;  // must be initialised in SimpleInitApp() due to dependence on assetManager
	private static final short[]  NIGHT_BRIGHTNESS = {30, 30, 30};
	private static final float    NIGHT_AMBIENCE = 0.8f;
	private static final Vector3f NIGHT_ANGLE = new Vector3f(-0.2f, -0.8f, 0.6f);
	private static final float    NIGHT_SHADOW_INTENSITY = 0;
	private static Spatial        NIGHT_MAP = null;
	private static Vector3f lookvec = new Vector3f(-540f, -50f, 360f);
	private static final Vector3f START_VEC = new Vector3f(590, -15, 80);
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
    private Edge3D floor = new Edge3D();
    private Edge3D ceil = new Edge3D();
	private Edge3D floor_plane = new Edge3D();
	private Material white,glass;

    
    
    
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
		synchronized(syncLockObject)
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
    }

    
    
    public void simpleRender(RenderManager rm)
    {
    }
    
	/**********************SETUP FUNCTIONS**********************/

    private void setupMaterials ()
    {
        grass = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		grass.setTexture("DiffuseMap", assetManager.loadTexture("img/3DFloor.jpg"));
        grass.setFloat("Shininess", 1000);
        
        white = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        white.setTexture("DiffuseMap", assetManager.loadTexture("img/3DFloor.jpg"));
        white.setFloat("Shininess", 1000);

        wallmat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		wallmat.setTexture("DiffuseMap", assetManager.loadTexture("img/wallpapers/default.jpg"));
        wallmat.setFloat("Shininess", 1000);
        
		glass = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		glass.setTexture("ColorMap", assetManager.loadTexture("req/window_1/pane.png"));
		glass.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    }
    
    
    
    public void reloadfloor(String name)
    {
		synchronized(syncLockObject)
		{
	        white = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
	        white.setTexture("DiffuseMap", assetManager.loadTexture("img/fs"+name));
	        white.setFloat("Shininess", 1000);
			floor.geometry.setMaterial(white);
			Material ceilm = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			ceilm.setTexture("ColorMap", assetManager.loadTexture("img/cs"+name));
			ceilm.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			ceil.geometry.setMaterial(ceilm);
			ceil.geometry.setQueueBucket(Bucket.Transparent);
		    ((JmeCanvasContext) this.getContext()).getCanvas().requestFocus();
		}
    }
    
    PointLight pl1, pl2;
    private void setupScene()
    {    	
    	//add the floor plane (large plane that extends beyond the floor to prevent falling off)
    	floor_plane.geometry = new Geometry("FloorPlane", new Quad(4000,4000));
    	floor_plane.geometry.setLocalTranslation(new Vector3f(2102,-101,-902));
    	floor_plane.geometry.rotate((float) -Math.toRadians(90),(float) Math.toRadians(180),0f );
    	floor_plane.geometry.setMaterial(grass);
    	if (shadowing)
    		floor_plane.geometry.setShadowMode(ShadowMode.Receive);
    	addToPhysics(floor_plane);
    	rootNode.attachChild(floor_plane.geometry);
    	
	    //add the floor (the box which users stand on, and is textured with the fill-colours)
	    floor.geometry = new Geometry("Floor", new Box(new Vector3f(500, -100, -1000), 500, 0.1f, 1000));
	    floor.geometry.rotate(0f,(float) -Math.toRadians(90),0f);
	    floor.geometry.setMaterial(white);
	    if (shadowing)
	    	floor.geometry.setShadowMode(ShadowMode.Receive);
	    rootNode.attachChild(floor.geometry);
	    
	    ceil.geometry = new Geometry("Floor", new Box(new Vector3f(-500, 0, 1000), 500, 0.1f, 1000));
	    ceil.geometry.rotate(0f,(float) -Math.toRadians(270),0f);
	    ceil.geometry.setMaterial(grass);
	    if (shadowing)
	    	ceil.geometry.setShadowMode(ShadowMode.Receive);
	    //addToPhysics(floor);
	    rootNode.attachChild(ceil.geometry);
	    
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
        sun = new DirectionalLight();
        ambient = new AmbientLight();
    	
    	// add shadow renderer
    	if (shadowing)
    	{
	        rootNode.setShadowMode(ShadowMode.Off);
	        psr = new PssmShadowRenderer(assetManager, 1024, 4);
	        viewPort.addProcessor(psr);
	        if (day)
	        {
	        	psr.setDirection(DAY_ANGLE);
	        	psr.setShadowIntensity(DAY_SHADOW_INTENSITY);
	        }
	        else
	        {
	        	psr.setDirection(NIGHT_ANGLE);
	        	psr.setShadowIntensity(NIGHT_SHADOW_INTENSITY);	        	
	        }
    	}

    	// add directional and ambient lighting to the scene
    	if (day)
    	{
    		sun.setDirection(DAY_ANGLE);
    		sun.setColor(new ColorRGBA((float) DAY_BRIGHTNESS[0]/255, (float) DAY_BRIGHTNESS[1]/255, (float) DAY_BRIGHTNESS[2]/255, 1));
    		ambient.setColor(ColorRGBA.White.mult(DAY_AMBIENCE));
    	}
    	else
    	{
    		sun.setDirection(NIGHT_ANGLE);
    		sun.setColor(new ColorRGBA((float) NIGHT_BRIGHTNESS[0]/255, (float) NIGHT_BRIGHTNESS[1]/255, (float) NIGHT_BRIGHTNESS[2]/255, 1));
    		ambient.setColor(ColorRGBA.White.mult(NIGHT_AMBIENCE));
    	}
		rootNode.addLight(sun);
    	rootNode.addLight(ambient);
    }
    
    
    
    public void setupSky()
    {
        DAY_MAP = SkyFactory.createSky(assetManager, "req/SkyDay.dds", false);
        NIGHT_MAP = SkyFactory.createSky(assetManager, "req/SkyNight.dds", false);
        if (day)
        	rootNode.attachChild(DAY_MAP);
        else
        	rootNode.attachChild(NIGHT_MAP);
    }
    
    
    
    private void setupPlayer ()
    {
        // player collision
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(20, 70, 1);
        player = new CharacterControl(capsuleShape, 0.01f);
        player.setJumpSpeed(20);
        player.setFallSpeed(160);
        player.setGravity(400);
        player.setPhysicsLocation(START_VEC);
        
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
	
    private void addToPhysics (Furniture3D furn)
    {
    	furn.physics = new RigidBodyControl(0);
    	furn.spatial.addControl(furn.physics);        
        bulletAppState.getPhysicsSpace().add(furn.physics);
    }
    
    
    
    private void removeFromPhysics (Furniture3D furn)
    {
    	bulletAppState.getPhysicsSpace().remove(furn.physics);
    }
    
    
    
    private void updatePhysics (Furniture3D furn)
    {
    	removeFromPhysics(furn);
    	addToPhysics(furn);
    }
    
    
    
    private void addToPhysics (Edge3D edge)
    {
    	//edge.collision = CollisionShapeFactory.createSingleMeshShape(edge.geometry);
    	//edge.physics = new RigidBodyControl(edge.collision, 0);
    	edge.physics = new RigidBodyControl(0);
    	edge.geometry.addControl(edge.physics);
        
        bulletAppState.getPhysicsSpace().add(edge.physics);
    }
    
    
    
    private void removeFromPhysics (Edge3D edge)
    {
    	edge.geometry.removeControl(edge.physics);
    	bulletAppState.getPhysicsSpace().remove(edge.physics);
    }
    
    
    
    // NB: updatePhysics for Edge3D is not currently needed because edges are completely
    // removed and remade when the walls are 'updated'. The respective add/remove functions
    // call the physics commands as required.
    /*private void updatePhysics (Edge3D edge)
    {
    	removeFromPhysics(edge);
    	addToPhysics(edge);
    }*/
    
    
    
    private void removeAllFromPhysics ()
    {
    	bulletAppState.getPhysicsSpace().removeAll(rootNode);
    }
    
    
    
    
    
	/**********************OTHER FUNCTIONS**********************/
    
	private void clearall()
    {
		removeAllFromPhysics();
		rootNode.detachAllChildren();
		setupScene();
    }
	
	
	
	
	
	/**********************EDGE3D CLASS**********************/
	
	private class Edge3D
	{
		Geometry           geometry;
		//MeshCollisionShape collision;
		RigidBodyControl   physics = null;
	}

	private class WallGeometry
	{
		ArrayList<Edge3D> geom    = new ArrayList<Edge3D>();
		ArrayList<Geometry> dw    = new ArrayList<Geometry>();
	}
	
	private final HashMap<Coords, HashMap<Edge, WallGeometry> > tabEdgeGeometry
		= new HashMap<Coords, HashMap<Edge, WallGeometry> >();
	

	
	
	/**********************TAB FUNCTIONS**********************/
	
	/** This should be called after the given Coords is no longer used i.e.
      *  immediately after, not immediately before deletion. It forgets about
      *  the edges. If called before, then the entry might be recreated */
	void tabRemoved(Coords tab)
	{
		HashMap<Edge, WallGeometry> edges = tabEdgeGeometry.remove(tab);
		if (edges != null)
			edges.clear();

		HashMap<Furniture, Furniture3D> furniture = tabFurnitureSpatials.remove(tab);
		if (furniture != null)
			furniture.clear();
	}

	
	
	/** Public function to prepare edges for the given coords. If these coords haven't 	*
	 *  been seen before then new objects will be created for it.						*/
	void tabChanged(Coords newTab)
	{
		if (!isInitComplete)
			return;
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
            		edges.put(e, makeWall(e));

            	tabEdgeGeometry.put(newTab, edges);
            }

            // Do furniture, if this tab already exists, this will not be null and it
            // will get redrawn from below, not recreated!
            HashMap<Furniture, Furniture3D> furniture = tabFurnitureSpatials.get(newTab);
            if (furniture == null)
            {
            	// make a brand new furniture container for the new tab and add all
            	// the furniture from the given coords
            	furniture = new HashMap<Furniture, Furniture3D>();
            	for (Furniture f : newTab.getFurniture())
            		furniture.put(f, makeFurniture(f));

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
				wall = makeWall(e); // make the new wall
				edges.put(e, wall);               
			
				Iterator<Edge3D> itr = wall.geom.iterator();
				while(itr.hasNext())
					rootNode.attachChild(itr.next().geometry);
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
				Iterator<Edge3D> itr = wall.geom.iterator();
				while(itr.hasNext())
				{
					Edge3D edge = itr.next();
					removeFromPhysics(edge);
					rootNode.detachChild(edge.geometry);
				}
				Iterator<Geometry> dwitr = wall.dw.iterator();
				while(dwitr.hasNext()){
					rootNode.detachChild(dwitr.next());
				}
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
			Iterator<Edge3D> itr = wall.geom.iterator();
			while(itr.hasNext())
			{
				Edge3D edge = itr.next();
				addToPhysics(edge);
				rootNode.attachChild(edge.geometry);
			}
			Iterator<Geometry> dwitr = wall.dw.iterator();
			while(dwitr.hasNext()){
				rootNode.attachChild(dwitr.next());
			}
		}
	}
	
	/** Moves the given edge. Returns if Coords c is not known yet or if e is not
      *  known */
	void updateEdgeChanged (Coords c, Edge e)
	{
		System.out.println("Update edge " + e);
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

    private WallGeometry makeWall(Edge e)
    {
    	int x1 = (int) e.getV1().getX();
    	int y1 = (int) e.getV1().getY();
    	int x2 = (int) e.getV2().getX();
    	int y2 = (int) e.getV2().getY();
    	float ctrlx = (int) e.getCtrlX();
    	float ctrly = (int) e.getCtrlY();
    	boolean straight = false;
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
    		straight = true;
    	
    	WallGeometry wallGeometry = new WallGeometry();
    	if (!straight)    		
    	{
    		//draw a curved wall using the recursive procedure
    		QuadCurve2D qcurve = e.getqcurve();			
    		recurvsion(wallGeometry,qcurve,4,e.tex());
    	}
    	else
    	{    		
    		//draw a straight wall without doors/windows
            Furniture[] dws = e.getDoorWindow();  
            if(dws==null){
            	drawline(wallGeometry, x1, x2, y1, y2, 100, -100, true,true,e.tex());
            }else{
            //draw a straight wall with doors/windows
            int width=0;
            if(dws[0].isWindow()){width=30;}
            if(dws[0].isDoor())  {width=20;}
            int doorpanel = 20;
            int cx=0,cy=0;
            //perform lots of trig and calculations to work out
            //how to partition the wall up to fit the windows/doors in
            if(x2>x1){cx = x1 + Math.abs((x2 -x1)/2);}
            if(x2==x1) {cx = x2;}
            if(x1>x2){cx = x1 - Math.abs((x2 -x1)/2);}
            if(y2>y1){cy = y1 + Math.abs((y2 -y1)/2);}
            if(y2==y1){cy=y2;}
            if(y1>y2){cy = y1 - Math.abs((y2 -y1)/2);}                  
            double dx1,dy1,dx2 ,dy2;
            double o = Math.abs(y2 - y1);
            double a = Math.abs(x2 - x1);
            if(o==0){
                dx1 = dws[0].getRotationCenter().getX()-width;                              
                dy1 = y1;
                dx2 = dws[0].getRotationCenter().getX()+width;
                dy2 = y2;
                if(x1>x2){double temp = dx1; dx1 = dx2; dx2 = temp;}
            }else{if(a==0){
                dx1 = x1;
                dy1 = dws[0].getRotationCenter().getY()-width;
                dx2 = x2;
                dy2 = dws[0].getRotationCenter().getY()+width;
                if(y1>y2){double temp = dy1; dy1 = dy2; dy2 = temp;}
            }else{                  
	            double theta = Math.atan(o/a);            
	            double xtri =  width * (Math.cos(theta));
	            double ytri =  width * (Math.sin(theta));
	            if(x2<x1){xtri = -xtri;}
	            if(y2<y1){ytri = -ytri;}
	            dx1 = dws[0].getRotationCenter().getX()-xtri;
	            dy1 = dws[0].getRotationCenter().getY()-ytri;
	            dx2 = dws[0].getRotationCenter().getX()+xtri;
	            dy2 = dws[0].getRotationCenter().getY()+ytri;
            }}
            drawline(wallGeometry,(int)x1,(int)dx1,(int)y1,(int)dy1,100,-100,true,true,e.tex());
            drawline(wallGeometry,(int)dx2,(int)x2,(int)dy2,(int)y2,100,-100,true,true,e.tex());
            if(dws[0].isDoor()){
            	drawline(wallGeometry,(int)dx1,(int)dx2,(int)dy1,(int)dy2,doorpanel,-doorpanel,false,true,e.tex());	
        		//add door frame
            	Furniture3D furn = new Furniture3D("req/door_1/door_1.obj");
		        furn.spatial.scale(4.1f, 4.1f, 4.1f);
		        furn.spatial.rotate(0f,-FastMath.HALF_PI+((FastMath.TWO_PI)-(float)e.getRotation()), 0f);    
		        furn.spatial.setLocalTranslation(new Float(dws[0].getRotationCenter().getX()),-100f,new Float(dws[0].getRotationCenter().getY()));
		        rootNode.attachChild(furn.spatial);
            }
            if(dws[0].isWindow()){
            	drawline(wallGeometry,(int)dx1,(int)dx2,(int)dy1,(int)dy2,30,-30,false,true,e.tex());
            	drawline(wallGeometry,(int)dx1,(int)dx2,(int)dy1,(int)dy2,30,-100,true,false,e.tex());	
            	//add window frame
        		Furniture3D furn = new Furniture3D("req/window_1/window_1.obj");
		        furn.spatial.scale(4.3f, 4.3f, 4.3f);
		        furn.spatial.rotate(0f, -FastMath.HALF_PI+((FastMath.TWO_PI)-(float)e.getRotation()), 0f);
		        furn.spatial.setLocalTranslation(new Float(dws[0].getRotationCenter().getX()),-70f,new Float(dws[0].getRotationCenter().getY()));
		        wallGeometry.dw.add((Geometry)furn.spatial);
		        //add window pane
		        Geometry pane = new Geometry("Box", new Box(new Vector3f(0,0,0),26f,18f,1f));								
				pane.setMaterial(glass);
				System.out.println((float)e.getRotation());
				pane.rotate(0,(FastMath.TWO_PI)-(float)e.getRotation(),0f);
				pane.setQueueBucket(Bucket.Transparent);
				pane.setLocalTranslation(new Float(dws[0].getRotationCenter().getX()), -49,new Float(dws[0].getRotationCenter().getY()));
				wallGeometry.dw.add(pane);			
            }}}
    	return wallGeometry;
	}
    
    
    
	/** Moves the two wall planes to the new position given by edge e */
	private void updatewall(WallGeometry wallGeometry, Edge e)
	{
		System.out.println("updatewall called");
		// remove all edges in the WallGeometry from the scene
		Iterator<Edge3D> itr = wallGeometry.geom.iterator();
		while(itr.hasNext())
		{
			Edge3D edge = itr.next();
			if(edge.physics!=null)
				removeFromPhysics(edge);
			rootNode.detachChild(edge.geometry);
		}
		//remove all windows and doors in the WallGeometry
		Iterator<Geometry> dwitr = wallGeometry.dw.iterator();
		while(dwitr.hasNext()){
			rootNode.detachChild(dwitr.next());
		}
		
		// makes new edges in the new locations
		WallGeometry completelyNew = makeWall(e);
		wallGeometry.geom.clear();
		itr = completelyNew.geom.iterator();		
		System.out.println("dw has "+completelyNew.dw.size());
		//add the walls to the arraylist and draw them in the scene
		while (itr.hasNext()){
			wallGeometry.geom.add(itr.next());
			rootNode.attachChild(wallGeometry.geom.get(wallGeometry.geom.size()-1).geometry);}
		
		//add any doors and windows to the arraylist and draw them in the scene
	    dwitr = completelyNew.dw.iterator();
		while(dwitr.hasNext()){
			wallGeometry.dw.add(dwitr.next());
			rootNode.attachChild(wallGeometry.dw.get(wallGeometry.dw.size()-1));
		}
	

	}
	
    private int recurvsion (WallGeometry top, QuadCurve2D curve, int level, String ppath)
    {
    	if (level == 0)
    	{
    		drawline(top, (int)curve.getX1(), (int)curve.getX2(), (int)curve.getY1(), (int)curve.getY2(), 100, -100, true,true,ppath);
    		return -1;
    	}
    	else
    	{
    		int nlevel = level - 1;
    		QuadCurve2D left =  new QuadCurve2D.Float();
    		QuadCurve2D right = new QuadCurve2D.Float();
    		curve.subdivide(left, right);
    		recurvsion(top, left, nlevel,ppath);
    		recurvsion(top, right, nlevel,ppath);
    		return 0;
    	}
    }
    
    private void drawline (WallGeometry wallGeometry, int x1, int x2, int y1, int y2, int height, int disp, boolean top,boolean bot,String ppath)
    {
    	//if top or bottom panel, use a different material
    	if(top==false)
    		ppath = ppath.substring(0,ppath.length()-4)+"b.jpg";
    	if(bot==false)
    		ppath = ppath.substring(0,ppath.length()-4)+"a.jpg";
    	
    	int length, leny, lenx = 0;
    	float rotation = 0;
		//work out the distances and angles required
		lenx = x2 - x1;
		leny = y2 - y1;
		if (lenx == 0)
		{
			length = leny;
			rotation =- FastMath.HALF_PI;
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
		Edge3D quad1 = new Edge3D();
		quad1.geometry = new Geometry("Box", new Quad(length, height));
		quad1.geometry.setLocalTranslation(new Vector3f(x1, disp, y1));
		quad1.geometry.rotate(0f, rotation, 0f);
        Material paper = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		paper.setTexture("DiffuseMap", assetManager.loadTexture(ppath));
        paper.setFloat("Shininess", 1000);
		quad1.geometry.setMaterial(paper);
    	if (shadowing)
    		quad1.geometry.setShadowMode(ShadowMode.CastAndReceive);
    	if (physics & top & bot){
    		addToPhysics(quad1);}
		wallGeometry.geom.add(quad1);

		// Double up the quad
		Edge3D quad2 = new Edge3D();
		quad2.geometry = new Geometry ("Box", new Quad(length,height));
		quad2.geometry.setLocalTranslation(new Vector3f(x2, disp, y2));
		quad2.geometry.rotate(0f, rotation + FastMath.PI, 0f);
		quad2.geometry.setMaterial(paper);
		if (shadowing)
			quad2.geometry.setShadowMode(ShadowMode.CastAndReceive);
		if (physics & top & bot){
			addToPhysics(quad2);}
    	wallGeometry.geom.add(quad2);
    	return; 
    }
	/**********************FURNITURE3D CLASS**********************/
	
	private class Furniture3D
	{
		Spatial          spatial;
		RigidBodyControl physics;
		Spatial			 glass;
		PointLight pl;
		
		Furniture3D (String path)
		{
			this.spatial = assetManager.loadModel(path);
			glass = null;
			pl = null;
		}
		Furniture3D (String path,int x)
		{
			this.spatial = assetManager.loadModel(path);
	        pl = new PointLight();
	        pl.setColor(new ColorRGBA(2, 2, 1.5f, 0));
	        pl.setRadius(250f);
		}
		
		boolean islight(){
			if (pl!=null){return true;}
			else{return false;}
		}
	}
	
	private final HashMap<Coords, HashMap<Furniture, Furniture3D> > tabFurnitureSpatials
		= new HashMap<Coords, HashMap<Furniture, Furniture3D> >();
	
	/**********************FURNITURE FUNCTIONS**********************/

	/** Adds the given furniture. Returns if Coords c is not known yet or if f is already
      *  added */    
	void addFurniture(Coords c, Furniture f)
	{
		if (c == null || f == null)
			throw new IllegalArgumentException("null");

		synchronized(syncLockObject)
		{
			HashMap<Furniture, Furniture3D> furniture = tabFurnitureSpatials.get(c);
			if (furniture == null)
				return;

			Furniture3D furn = furniture.get(f);
			if (furn == null)
			{
				furn = makeFurniture(f);
				furniture.put(f, furn);				
				rootNode.attachChild(furn.spatial);
				if(furn.islight()){
					rootNode.addLight(furn.pl);
				}
			}
		}
	}

	
	
	private Furniture3D makeFurniture(Furniture f)
	{
		Furniture3D furn;
		Point center = f.getRotationCenter();
        String name = f.getObjPath();
    	
        // if object specified does not exist
        if(name == null || name.equals("none"))
        	furn = new Furniture3D("req/armchair_1/armchair_1.obj");
        else{
        	if(f.isLight()){
        		furn = new Furniture3D("req/" + name.substring(0, name.length() - 4) + "/" + name,1);
        	}else{
        		furn = new Furniture3D("req/" + name.substring(0, name.length() - 4) + "/" + name);
        	}
        }
        // model settings
        furn.spatial.scale(5, 5, 5);
        furn.spatial.rotate(0f, -FastMath.HALF_PI, 0f);        
        furn.spatial.setLocalTranslation(center.x,-100f,center.y);
        if(furn.islight()){
        	furn.pl.setPosition(new Vector3f(center.x,-40f,center.y));
        }
    	if (shadowing)
    		furn.spatial.setShadowMode(ShadowMode.CastAndReceive);
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
			HashMap<Furniture, Furniture3D> furniture = tabFurnitureSpatials.get(c);
			if (furniture == null)
				return;

			Furniture3D furn = furniture.get(f);
			if (furn != null)
			{
				furniture.remove(f);
				if (physics)
					removeFromPhysics(furn);
				rootNode.detachChild(furn.spatial);
				if(furn.islight()){
					rootNode.removeLight(furn.pl);
				}
			}
		}
	}

	
	/* Goes through all the walls in the given hashmap and adds them to the rootNode 
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
	}*/
	
	
    /** Goes through all the furniture in the given hashmap and adds them to the rootNode */
	private void redrawAllFurniture(HashMap<Furniture, Furniture3D> furnitures)
	{
		Collection<Furniture3D> furniture = furnitures.values();
		Iterator<Furniture3D> iterator = furniture.iterator();
		Furniture3D furn;
		while (iterator.hasNext())
		{
			furn = iterator.next();
			addToPhysics(furn);
			rootNode.attachChild(furn.spatial);
			if(furn.islight()){
				rootNode.addLight(furn.pl);
			}
		}
	}
	
	/** Moves the given spatial to the new position of furniture f */
	private void updatefurniture(Furniture3D furn, Furniture f)
	{
		// recalculate the furniture's position and move it
		Point center = f.getRotationCenter();
		furn.spatial.setLocalTranslation(center.x,-100f,center.y);
        if(furn.islight()){
        	furn.pl.setPosition(new Vector3f(center.x,-40f,center.y));
        }
		
		// recalculate the furniture's rotation and adjust it
		float rotation = (float) (f.getRotation() * 0.5);
		float sinr = FastMath.sin(rotation);
		float cosr = FastMath.cos(rotation);
		/*float x = sinr * 0.0f;
		float y = sinr * 1.0f;
		float z = sinr * 0.0f;*/
		//Quaternion c = new Quaternion(cosr, x, y, z);
		Quaternion c = new Quaternion(cosr, 0, sinr, 0);
		furn.spatial.setLocalRotation(c);
		furn.spatial.rotate(0, -FastMath.HALF_PI, -FastMath.PI);
		
		// recalculate the furniture's physics (from position) and update it
		updatePhysics(furn);
	}
	
	
	
	/** Moves the given furniture. Returns if Coords c is not known yet or if f is not
      *  known */
	void updateFurnitureChanged(Coords c, Furniture f)
	{
		if (c == null || f == null)
			throw new IllegalArgumentException("null");
         
		synchronized(syncLockObject)
		{
			HashMap<Furniture, Furniture3D> furniture = tabFurnitureSpatials.get(c);
			if (furniture == null)
				return;

			Furniture3D furn = furniture.get(f);
			if (furn != null)
				updatefurniture(furn, f);
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