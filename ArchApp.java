import java.awt.Point;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.jme3.app.Application;
import com.jme3.app.StatsView;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
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


/*This code is mostly from the default application included with JME, SimpleApp.
 * It has been modified so that various features and settings can be manipulated
 * such as turning off FPS read-outs and setting a custom camera start-point
 */
public class ArchApp extends Application
{
	/**********************GLOBAL VARIABLES**********************/
	// toggle globals
	private static final boolean shadowing = false; // determines whether or not to render shadows
	private static final boolean physics   = true;  // determines whether or not to calculate physics (not 100% implemented)
	private static final boolean overlay   = false; // displays overlay
	private static final boolean tracing   = false;  // prints tracing information as various functions are called - for debugging.
	
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
    private Edge3D.Segment floor = new Edge3D.Segment();
    private Edge3D.Segment ceil = new Edge3D.Segment();
	private Edge3D.Segment floor_plane = new Edge3D.Segment();
	private Material invis,glass;

    
    
    
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
    		if (tracing)
    		{
    			System.out.printf("onAction(%s, %b, %f) called.\n", name, value, tpf);
    			System.out.flush();
    		}
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
		if (tracing)
			System.out.println("start() called.");
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
		if (tracing)
			System.out.println("update() called.");
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
		if (tracing)
			System.out.println("initialize() called.");
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
            inputManager.addListener(actionListener, "PLAYER_Left", "PLAYER_Right", "PLAYER_Up", "PLAYER_Down");
        }
        
        setupMaterials();
        simpleInitApp();
        
        if (main.frontEnd != null)
        	tabChangedIgnoreInitComplete(main.frontEnd.getCurrentCoords());
        isInitComplete = true;
    }
    
    
    
    public void initCamera()
    {
		if (tracing)
			System.out.println("initCamera() called.");
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
		if (tracing)
			System.out.println("simpleInitApp() called.");
    	// initialise the physics components
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        // setup the other components
		flyCam.setDragToRotate(true);
		//setupScene();
		setupLighting();
        setupPlayer();
        cam.lookAt(lookvec, Vector3f.UNIT_Y);
    }

    
    
    public void simpleUpdate(float tpf)
    {
		//if (tracing)
		//	System.out.printf("simpleUpdate(%f) called.\n", tpf);
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
		if (tracing)
			System.out.println("setupMaterials() called.");
        grass = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		grass.setTexture("DiffuseMap", assetManager.loadTexture("img/3DFloor.jpg"));
        grass.setFloat("Shininess", 1000);
        
        invis = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        invis.setTexture("ColorMap", assetManager.loadTexture("img/invis.png"));
        invis.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

        wallmat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		wallmat.setTexture("DiffuseMap", assetManager.loadTexture("img/wallpapers/default.jpg"));
        wallmat.setFloat("Shininess", 1000);
        
		glass = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		glass.setTexture("ColorMap", assetManager.loadTexture("req/window_1/pane.png"));
		glass.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    }
    
    
    
    public void reloadfloor(String name)
    {
		if (tracing)
			System.out.printf("reloadfloor(%s) called.\n", name);
		synchronized(syncLockObject)
		{
	        grass = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
	        grass.setTexture("DiffuseMap", assetManager.loadTexture("img/fs"+name));
	        grass.setFloat("Shininess", 1000);
			floor.side[0].setMaterial(grass);
			Material ceilm = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			ceilm.setTexture("ColorMap", assetManager.loadTexture("img/cs"+name));
			ceilm.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			ceil.side[0].setMaterial(ceilm);
			ceil.side[0].setQueueBucket(Bucket.Transparent);
		    ((JmeCanvasContext) this.getContext()).getCanvas().requestFocus();
		}
    }
    
    //PointLight pl1, pl2;
    private void setupScene()
    {
		if (tracing)
			System.out.println("setupScene() called.");
		
    	//add the floor plane (large plane that extends beyond the floor to prevent falling off)
    	floor_plane.side[0] = new Geometry("FloorPlane", new Quad(4000,4000));
    	floor_plane.side[0].setLocalTranslation(new Vector3f(2102,-101,-902));
    	floor_plane.side[0].rotate((float) -Math.toRadians(90),(float) Math.toRadians(180),0f );
    	floor_plane.side[0].setMaterial(grass);
    	if (shadowing)
    		floor_plane.side[0].setShadowMode(ShadowMode.Receive);
    	addToPhysics(floor_plane);														// Watch out for this guy, its easy to forget he's part of the physics
    	rootNode.attachChild(floor_plane.side[0]);
    	
	    //add the floor (the box which users stand on, and is textured with the fill-colours)
	    floor.side[0] = new Geometry("Floor", new Box(new Vector3f(500, -100, -1000), 500, 0.1f, 1000));
	    floor.side[0].rotate(0f,(float) -Math.toRadians(90),0f);
	    floor.side[0].setMaterial(grass);
	    if (shadowing)
	    	floor.side[0].setShadowMode(ShadowMode.Receive);
	    rootNode.attachChild(floor.side[0]);
	    
	    ceil.side[0] = new Geometry("Ceiling", new Box(new Vector3f(-500, 0, 1000), 500, 0.1f, 1000));
	    ceil.side[0].rotate(0f,(float) -Math.toRadians(270), 0f);
	    ceil.side[0].setMaterial(invis);
		ceil.side[0].setQueueBucket(Bucket.Transparent);
	    if (shadowing)
	    	ceil.side[0].setShadowMode(ShadowMode.Receive);
	    rootNode.attachChild(ceil.side[0]);

        setupSky();
    }
    
    
    
    public void setupLighting ()
    {
		if (tracing)
			System.out.println("setupLighting() called.");
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
		if (tracing)
			System.out.println("setupSky() called.");
        DAY_MAP = SkyFactory.createSky(assetManager, "req/SkyDay.dds", false);
        NIGHT_MAP = SkyFactory.createSky(assetManager, "req/SkyNight.dds", false);
        if (day)
        	rootNode.attachChild(DAY_MAP);
        else
        	rootNode.attachChild(NIGHT_MAP);
    }
    
    
    
    private void setupPlayer ()
    {
		if (tracing)
			System.out.println("setupPlayer() called.");
        // player collision
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(15, 70, 1);
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
		if (tracing)
			System.out.println("toggleDay() called.");
    	if (!day)
    	{
    		rootNode.detachChild(NIGHT_MAP);
            rootNode.attachChild(DAY_MAP);
            sun.setDirection(DAY_ANGLE);
            sun.setColor(new ColorRGBA((float) DAY_BRIGHTNESS[0]/255, (float) DAY_BRIGHTNESS[1]/255, (float) DAY_BRIGHTNESS[2]/255, 1));
            ambient.setColor(ColorRGBA.White.mult(DAY_AMBIENCE));
            if (shadowing)
            {
            	psr.setDirection(DAY_ANGLE);
            	psr.setShadowIntensity(DAY_SHADOW_INTENSITY);
            }
            //turnOffLights();
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
            //turnOnLights();
            day = false;
    	}
		// Stops you having to click to update the 3D
		((JmeCanvasContext) this.getContext()).getCanvas().requestFocus();
    }
    
    
    
    
    
    /**********************PHYSICS FUNCTIONS**********************/
	
    private void addToPhysics (Furniture3D furn)
    {
		if (tracing)
			System.out.println("addToPhysics(Furniture3D) called.");
    	furn.physics = new RigidBodyControl(0);
    	furn.spatial.addControl(furn.physics);        
        bulletAppState.getPhysicsSpace().add(furn.physics);
    }
    
    
    
    private void removeFromPhysics (Furniture3D furn)
    {
		if (tracing)
			System.out.println("removeFromPhysics(Furniture3D) called.");
		if (furn.physics != null)
		{
			furn.spatial.removeControl(furn.physics);
    		bulletAppState.getPhysicsSpace().remove(furn.physics);
		}
    }
    
    
    
    private void updatePhysics (Furniture3D furn)
    {
		if (tracing)
			System.out.println("updatePhysics(Furniture3D) called.");
    	removeFromPhysics(furn);
    	addToPhysics(furn);
    }
    
    
    
    private void addToPhysics (Edge3D.Segment segment)
    {
		if (tracing)
			System.out.println("addToPhysics(Edge3D.Segment) called.");
		if (segment.getType() != Edge3D.Segment.Position.TOP)
		{
			segment.physics = new RigidBodyControl(0);
    		segment.side[0].addControl(segment.physics);
        
        	bulletAppState.getPhysicsSpace().add(segment.physics);
		}
    }
    
    
    
    private void removeFromPhysics (Edge3D.Segment segment)
    {
		if (tracing)
			System.out.println("removeFromPhysics(Edge3D.Segment) called.");
    	if (segment.physics != null)
    	{
        	segment.side[0].removeControl(segment.physics);
        	bulletAppState.getPhysicsSpace().remove(segment.physics);
    	}
    }
    
    
    
    // NB: updatePhysics for Edge3D is not currently needed because edges are completely
    // removed and remade when the walls are 'updated'. The respective add/remove functions
    // call the physics commands as required.
    /*private void updatePhysics (Edge3D edge)
    {
    	removeFromPhysics(edge);
    	addToPhysics(edge);
    }*/
    
    
    // TODO: I think this doesn't actually remove all from physics. To do this I think you need to manually go through every item 
    // and call removeFromPhysics (to remove all controllers from spatials as well as just the instances attached to the rootnode).
    private void removeAllFromPhysics (Coords c)
    {
		if (tracing)
			System.out.println("removeAllFromPhysics() called.");
		if (c == null)
			throw new IllegalArgumentException("null");

		// remove every segment in every edge from physics
		HashMap<Edge, Edge3D> edges = tabEdges.get(c);
		if (edges != null)
		{
			Collection<Edge3D> col_edges = edges.values();
			Iterator<Edge3D> itr_edges = col_edges.iterator();
			while (itr_edges.hasNext())
			{
				Edge3D edge = itr_edges.next();
				Iterator<Edge3D.Segment> itr_segments = edge.segments.iterator();
				while (itr_segments.hasNext())
					removeFromPhysics(itr_segments.next());
			}
		}
		
		// remove every furniture item from physics
		HashMap<Furniture, Furniture3D> furnitures = tabFurniture.get(c);
		if (furnitures != null)
		{
			Collection<Furniture3D> col_furnitures = furnitures.values();
			Iterator<Furniture3D> itr_furnitures = col_furnitures.iterator();
			while (itr_furnitures.hasNext())
				removeFromPhysics(itr_furnitures.next());
		}
		
		// remove scene from physics
    	removeFromPhysics(floor_plane);		
    }
    
    
    
    
    
	/**********************OTHER FUNCTIONS**********************/
    
	private void clearAll(Coords c)
    {
		//if (tracing)
			System.out.println("clearall() called.");
		removeAllFromPhysics(c);
		rootNode.detachAllChildren();
		setupScene();
    }
	
	
	
	
	
	/**********************EDGE3D CLASS**********************/
	

	
	private final HashMap<Coords, HashMap<Edge, Edge3D> > tabEdges
		= new HashMap<Coords, HashMap<Edge, Edge3D> >();
	

	
	
	/**********************TAB FUNCTIONS**********************/
	
	/** This should be called after the given Coords is no longer used i.e.
      *  immediately after, not immediately before, deletion. It forgets about
      *  the edges. If called before, then the entry might be recreated. */
	void tabRemoved(Coords tab)
	{
		if (tracing)
			System.out.println("tabRemoved(Coords) called.");
		HashMap<Edge, Edge3D> edges = tabEdges.remove(tab);
		if (edges != null)
			edges.clear();

		HashMap<Furniture, Furniture3D> furniture = tabFurniture.remove(tab);
		if (furniture != null)
			furniture.clear();
	}

	
	
	/** Public function to prepare edges for the given coords. If these coords haven't 	*
	 *  been seen before then new objects will be created for it.						*/
	void tabChanged(Coords newTab)
	{
		if (tracing)
			System.out.println("tabChanged(Coords) called.");
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
		//if (tracing)
			System.out.println("tabChangedIgnoreInitComplete(Coords) called.");
		synchronized(syncLockObject)
		{
			// completely remove all edges/furniture/lights/physics from the scene
			clearAll(newTab);
			
			// if the coordinate space supplied doesn't exist, exit having cleared the space.
			if (newTab == null)
			{
				System.out.println("    newTab = null");
				return;
			}

			// Recalculate the edges in the coordinate space, adding them to a new HashMap.
            HashMap<Edge, Edge3D> edges = tabEdges.get(newTab);
            if (edges == null)
            {
            	edges = makeAllEdges(newTab);
            	tabEdges.put(newTab, edges);
            }
            else
            	System.out.println("    edges != null");

			// Recalculate the furniture in the coordinate space, adding them to a new HashMap.
            HashMap<Furniture, Furniture3D> furnitures = tabFurniture.get(newTab);
            if (furnitures == null)
            {
            	furnitures = makeAllFurniture(newTab);
            	tabFurniture.put(newTab, furnitures);
            }
            else
            	System.out.println("    furnitures != null");

            // draw all the newly calculated edges + furniture
            drawAllEdges(edges);
            drawAllFurniture(furnitures);
		}
	}

	
	
	
	
	/**********************EDGE FUNCTIONS**********************/

	/** Adds the given edge. Returns if Coords c is not known yet or if e is already
      * added */
	void addEdge(Coords c, Edge e)
	{
		if (tracing)
			System.out.println("addEdge(Coords, Edge) called.");
		if (c == null || e == null)
			throw new IllegalArgumentException("null");

		synchronized(syncLockObject)
		{
			HashMap<Edge, Edge3D> edges = tabEdges.get(c);
			if (edges == null)
				return;

			Edge3D wall = edges.get(e);
			if (wall == null)
			{
				wall = makeEdge(e); // make the new wall
				drawEdge(wall);
				edges.put(e, wall);
			}
		}
	}

	
	
	/** Removes the given edge. Returns if Coords c is not known yet or if e is not
      *  known */
	void removeEdge(Coords c, Edge e)
	{
		if (tracing)
			System.out.println("removeEdge(Coords, Edge) called.");
		if (c == null || e == null)
			throw new IllegalArgumentException("null");
         
		synchronized(syncLockObject)
		{
			HashMap<Edge, Edge3D> edges = tabEdges.get(c);
			if (edges == null)
				return;

			Edge3D wall = edges.get(e);
			if (wall != null)
			{
				edges.remove(e);
				undrawEdge(wall);
			}
			else
				System.err.println("[WARNING @ArchApp] [SEVERITY: High] removeEdge called with a hashmap which has no mapping for Edge: " + e.toString());
		}
	}
	
	
	
	/** Moves the given edge. Returns if Coords c is not known yet or if e is not
      *  known */
	void updateEdge (Coords c, Edge e)
	{
		if (tracing)
			System.out.println("updateEdge(Coords, Edge) called.");
		System.out.println("Update edge " + e);
		if (c == null || e == null)
			throw new IllegalArgumentException("null");
         
		synchronized(syncLockObject)
		{
			HashMap<Edge, Edge3D> edges = tabEdges.get(c);
			if (edges == null)
				return;
			Edge3D wall = edges.get(e);
			
			if (wall != null)
			{
				// remove all segments, their physics and all attached furniture from the scene
				undrawEdge(wall);
				
				// recalculate the new segment/furniture positions
				wall.segments.clear();
				wall.attachedFurniture.clear();
				
				Edge3D newWall = makeEdge(e);
				
				Iterator<Edge3D.Segment> segment_itr = newWall.segments.iterator();
				while (segment_itr.hasNext())
					wall.segments.add(segment_itr.next());
				Iterator<Geometry> furniture_itr = newWall.attachedFurniture.iterator();
				while(furniture_itr.hasNext())
					wall.attachedFurniture.add(furniture_itr.next());
				
				drawEdge(wall);
			}
		}
	}
	
	
	
	private void undrawEdge (Edge3D wall)
	{
		Iterator<Edge3D.Segment> segment_itr = wall.segments.iterator();
		while(segment_itr.hasNext())
		{
			Edge3D.Segment segment = segment_itr.next();
			removeFromPhysics(segment);
			rootNode.detachChild(segment.side[0]);
			rootNode.detachChild(segment.side[1]);
		}
		
		Iterator<Geometry> furniture_itr = wall.attachedFurniture.iterator();
		while(furniture_itr.hasNext())
			rootNode.detachChild(furniture_itr.next());		
	}
	
	
	
	private void drawEdge (Edge3D wall)
	{
		Iterator<Edge3D.Segment> segment_itr = wall.segments.iterator();
		while(segment_itr.hasNext())
		{
			Edge3D.Segment segment = segment_itr.next();
			addToPhysics(segment);
			rootNode.attachChild(segment.side[0]);
			rootNode.attachChild(segment.side[1]);
		}
		
		Iterator<Geometry> furniture_itr = wall.attachedFurniture.iterator();
		while(furniture_itr.hasNext())
			rootNode.attachChild(furniture_itr.next());
	}

	
	
	/** Goes through all the walls in the given hashmap and adds them to the rootNode */
	private void drawAllEdges (HashMap<Edge, Edge3D> edges)
	{
		if (tracing)
			System.out.println("redrawAllEdges(HashMap<Edge, Edge3D>) called.");
		if (edges == null)
		{
			System.err.println("[WARNING @ArchApp] [SEVERITY: High] drawAllEdges called with NULL edges.");
			return;
		}
		
		Collection<Edge3D> walls = edges.values();
		Iterator<Edge3D> wall_itr = walls.iterator();
		while (wall_itr.hasNext())
			drawEdge(wall_itr.next());
	}
	
	
	//private void undrawAllEdges (HashMap<Edge, Edge3D> edges)
	private void undrawAllEdges (HashMap<Edge, Edge3D> edges)
	{
		if (tracing)
			System.out.println("redrawAllEdges(HashMap<Edge, Edge3D>) called.");
		if (edges == null)
		{
			System.err.println("[WARNING @ArchApp] [SEVERITY: High] undrawAllEdges called with NULL edges.");
			return;
		}		
		
		Collection<Edge3D> walls = edges.values();
		Iterator<Edge3D> wall_itr = walls.iterator();
		while (wall_itr.hasNext())
			undrawEdge(wall_itr.next());
	}
	

	
	/**********************WALL FUNCTIONS**********************/
	
	private HashMap<Edge, Edge3D> makeAllEdges (Coords c)
	{
		HashMap<Edge, Edge3D> edges = new HashMap<Edge, Edge3D>();
        for (Edge e : c.getEdges())
        	edges.put(e, makeEdge(e));
        return edges;
	}
	

    private Edge3D makeEdge (Edge e)
    {
		//if (tracing)
			System.out.println("makeWall(Edge) called.");
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
    	if (xx > 0 && yy > 0)
    		straight = true;
    	
    	Edge3D wall = new Edge3D();
    	if (!straight)    		
    	{
    		//draw a curved wall using the recursive procedure
    		QuadCurve2D qcurve = e.getqcurve();			
    		recurvsion(wall,qcurve,4,e.tex());
    	}
    	else
    	{    		
    		//draw a straight wall without doors/windows
            Furniture[] dws = e.getDoorWindow();  
            if (dws == null)
            	drawline(wall, x1, x2, y1, y2, 100, -100, e.tex());
            else
            {
	            //draw a straight wall with doors/windows
	            ArrayList<Furniture> dwsa = new ArrayList<Furniture>(Arrays.asList(dws));	            
	            wdoorcursion(e,wall,dwsa,x1,x2,y1,y2);
            }
    	}
    	return wall;
	}
    
    private void wdoorcursion(Edge e,Edge3D wall,ArrayList<Furniture> dwsa,int x1,int x2,int y1,int y2)
    {
		if (tracing)
			System.out.println("wdoorcursion(...) called.");                
    	//if all the windows and doors have been drawn
    	if (dwsa.size()==0)
    	{
    		drawline(wall, x1, x2, y1, y2, 100, -100, e.tex());
    		return;
    	}
	    //select the closest window or door thingumy
    	int mindist=0,mini=0,currdist=0;
    	for(int i=0;i<dwsa.size();i++){
    		currdist = (int)dwsa.get(i).getRotationCenter().distance(new Point(x1,y1));
    		if(mindist==0) {mindist=currdist; mini = i;}
    		if(currdist<mindist){mindist=currdist; mini=i;} 
	    }
    	//proceed with drawing up to that window, then recurse for the rest of the wall
    	int width=0;
	    if(dwsa.get(mini).isWindow()){width=30;}
	    if(dwsa.get(mini).isDoor())  {width=20;}
	    int doorpanel = 20;              
	    double dx1,dy1,dx2 ,dy2;
	    double o = Math.abs(y2 - y1);
	    double a = Math.abs(x2 - x1);
	    if(o==0){
	        dx1 = dwsa.get(mini).getRotationCenter().getX()-width;                              
	        dy1 = y1;
	        dx2 = dwsa.get(mini).getRotationCenter().getX()+width;
	        dy2 = y2;
	        if(x1>x2){double temp = dx1; dx1 = dx2; dx2 = temp;}
	    }else{if(a==0){
	        dx1 = x1;
	        dy1 = dwsa.get(mini).getRotationCenter().getY()-width;
	        dx2 = x2;
	        dy2 = dwsa.get(mini).getRotationCenter().getY()+width;
	        if(y1>y2){double temp = dy1; dy1 = dy2; dy2 = temp;}
	    }else{                  
	        double theta = Math.atan(o/a);            
	        double xtri =  width * (Math.cos(theta));
	        double ytri =  width * (Math.sin(theta));
	        if(x2<x1){xtri = -xtri;}
	        if(y2<y1){ytri = -ytri;}
	        dx1 = dwsa.get(mini).getRotationCenter().getX()-xtri;
	        dy1 = dwsa.get(mini).getRotationCenter().getY()-ytri;
	        dx2 = dwsa.get(mini).getRotationCenter().getX()+xtri;
	        dy2 = dwsa.get(mini).getRotationCenter().getY()+ytri;
	    }}
	    drawline(wall,(int)x1,(int)dx1,(int)y1,(int)dy1,100,-100,e.tex());
	    if(dwsa.get(mini).isDoor()){
	    	//draw top panel
	    	drawline(wall,(int)dx1,(int)dx2,(int)dy1,(int)dy2,doorpanel,-doorpanel,e.tex());	
			//add door frame
	    	Furniture3D furn = new Furniture3D(assetManager.loadModel("req/door_1/door_1.obj"), false);
	        furn.spatial.scale(4.1f, 4.1f, 4.1f);
	        furn.spatial.rotate(0f,-FastMath.HALF_PI+((FastMath.TWO_PI)-(float)e.getRotation()), 0f);    
	        furn.spatial.setLocalTranslation(new Float(dwsa.get(mini).getRotationCenter().getX()),-100f,new Float(dwsa.get(mini).getRotationCenter().getY()));
	        wall.attachedFurniture.add((Geometry)furn.spatial);
	    }
	    if(dwsa.get(mini).isWindow()){
	    	//draw the top and bottom panels
	    	drawline(wall,(int)dx1,(int)dx2,(int)dy1,(int)dy2,30,-30,e.tex());
	    	drawline(wall,(int)dx1,(int)dx2,(int)dy1,(int)dy2,30,-100,e.tex());	
	    	//add window frame
			Furniture3D furn = new Furniture3D(assetManager.loadModel("req/window_1/window_1.obj"), false);
	        furn.spatial.scale(4.3f, 4.3f, 4.3f);
	        furn.spatial.rotate(0f, -FastMath.HALF_PI+((FastMath.TWO_PI)-(float)e.getRotation()), 0f);
	        furn.spatial.setLocalTranslation(new Float(dwsa.get(mini).getRotationCenter().getX()),-70f,new Float(dwsa.get(mini).getRotationCenter().getY()));
	        wall.attachedFurniture.add((Geometry)furn.spatial);
	        //add window pane
	        Geometry pane = new Geometry("pane", new Box(new Vector3f(0,0,0),26f,18f,1f));								
			pane.setMaterial(glass);
			System.out.println((float)e.getRotation());
			pane.rotate(0,(FastMath.TWO_PI)-(float)e.getRotation(),0f);
			pane.setQueueBucket(Bucket.Transparent);
			pane.setLocalTranslation(new Float(dwsa.get(mini).getRotationCenter().getX()), -49,new Float(dwsa.get(mini).getRotationCenter().getY()));
			wall.attachedFurniture.add(pane);
	    }
	    
	    //remove the window you just added
	    dwsa.remove(mini);
	    
	    //recurse for the rest of the wall
	    wdoorcursion(e,wall,dwsa,(int)dx2,x2,(int)dy2,y2);
    }
	
    private int recurvsion (Edge3D top, QuadCurve2D curve, int level, String ppath)
    {
		if (tracing)
			System.out.println("recurvsion(...) called.");
    	if (level == 0)
    	{
    		drawline(top, (int)curve.getX1(), (int)curve.getX2(), (int)curve.getY1(), (int)curve.getY2(), 100, -100, ppath);
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
    
    private void drawline (Edge3D wall, int x1, int x2, int y1, int y2, int height, int disp, String ppath)
    {
		if (tracing)
			System.out.println("drawline(...) called.");
		
    	Edge3D.Segment.Position type;
		if (height < 100)
		{
			if (disp > -100)
				type = Edge3D.Segment.Position.TOP;
			else
				type = Edge3D.Segment.Position.BOTTOM;
		}
		else
			type = Edge3D.Segment.Position.FULL;
    	
    	//if top or bottom panel, use a different material
    	if (type == Edge3D.Segment.Position.TOP)
    		ppath = ppath.substring(0,ppath.length()-4)+"b.jpg";
    	else if (type == Edge3D.Segment.Position.BOTTOM)
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
				if(y2 > y1 && x1 > x2)
					rotation += FastMath.PI;
				if(y2 < y1 && x1 > x2)
					rotation += FastMath.PI;
			}
		}
		
		// Draw a quad between the 2 given vertices
        Material paper = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		paper.setTexture("DiffuseMap", assetManager.loadTexture(ppath));
        paper.setFloat("Shininess", 1000);
        
		//Edge3D.Segment segment = new Edge3D.Segment();
        Edge3D.Segment segment = new Edge3D.Segment(type);
		segment.side[0] = new Geometry("wallside0", new Quad(length, height));
		segment.side[1] = new Geometry ("wallside1", new Quad(length,height));
		segment.side[0].setLocalTranslation(new Vector3f(x1, disp, y1));
		segment.side[1].setLocalTranslation(new Vector3f(x2, disp, y2));
		segment.side[0].rotate(0f, rotation, 0f);
		segment.side[1].rotate(0f, rotation + FastMath.PI, 0f);
        segment.side[0].setMaterial(paper);
		segment.side[1].setMaterial(paper);
    	if (shadowing)
    	{
    		segment.side[0].setShadowMode(ShadowMode.CastAndReceive);
			segment.side[1].setShadowMode(ShadowMode.CastAndReceive);
    	}
    	
		wall.segments.add(segment);
    }
	/**********************FURNITURE3D CLASS**********************/
	

	
	private final HashMap<Coords, HashMap<Furniture, Furniture3D> > tabFurniture
		= new HashMap<Coords, HashMap<Furniture, Furniture3D> >();
	
	/**********************FURNITURE FUNCTIONS**********************/

	/** Adds the given furniture. Returns if Coords c is not known yet or if f is already
      *  added */    
	void addFurniture(Coords c, Furniture f)
	{
		if (tracing)
			System.out.println("addFurniture(Coords, Furniture) called.");
		if (c == null || f == null)
			throw new IllegalArgumentException("null");

		synchronized(syncLockObject)
		{
			HashMap<Furniture, Furniture3D> furniture = tabFurniture.get(c);
			if (furniture == null)
			{
				System.err.println("[WARNING @ArchApp] [SEVERITY: High] addFurniture called with NULL furniture.");
				return;
			}

			Furniture3D furn = furniture.get(f);
			if (furn == null)
			{
				furn = makeFurniture(f);
				drawFurniture(furn);
				furniture.put(f, furn);
			}
		}
	}

	
	
	private Furniture3D makeFurniture(Furniture f)
	{
		//if (tracing)
			System.out.println("makeFurniture(Furniture) called.");
		Furniture3D furn;
		Point center = f.getRotationCenter();
        String name = f.getObjPath();
        float rotation = (float) (f.getRotation() * 0.5);
    	
        // if object specified does not exist
        if(name == null || name.equals("none"))
        {
			System.err.println("[WARNING @ArchApp] [SEVERITY: Low] makeFurniture called with NULL furniture. Continuing to make an armchair (for no apparent reason).");
        	furn = new Furniture3D(assetManager.loadModel("req/armchair_1/armchair_1.obj"), true);
        }
        else
    		furn = new Furniture3D(assetManager.loadModel("req/" + name.substring(0, name.length() - 4) + "/" + name), f.isPhysical());
        
        // model settings
        furn.spatial.scale(5, 5, 5);
		float sinr = FastMath.sin(rotation);
		float cosr = FastMath.cos(rotation);
		Quaternion c = new Quaternion(cosr, 0, sinr, 0);
		furn.spatial.setLocalRotation(c);
		furn.spatial.rotate(0, -FastMath.HALF_PI, -FastMath.PI);       
        furn.spatial.setLocalTranslation(center.x, -100f, center.y);
        
    	if (f.isLight())
    		furn.addLight(255, 0, 0);
    	if (shadowing)
    		furn.spatial.setShadowMode(ShadowMode.CastAndReceive);

        return furn;
	}
	
	
	private HashMap<Furniture, Furniture3D> makeAllFurniture (Coords c)
	{
		HashMap<Furniture, Furniture3D> furnitures = new HashMap<Furniture, Furniture3D>();
        for (Furniture f : c.getFurniture())
        	furnitures.put(f, makeFurniture(f));
        return furnitures;
	}

	
	
	/** Removes the given furniture. Returns if Coords c is not known yet or if f is not
      *  known */
	void removeFurniture(Coords c, Furniture f)
	{
		if (tracing)
			System.out.println("removeFurniture(Coords, Furniture) called.");
		if (c == null || f == null)
			throw new IllegalArgumentException("null");
         
		synchronized(syncLockObject)
		{
			HashMap<Furniture, Furniture3D> furnitures = tabFurniture.get(c);
			if (furnitures == null)
			{
				System.err.println("[WARNING @ArchApp] [SEVERITY: High] removeFurniture called with NULL furnitures.");
				return;
			}

			Furniture3D furn = furnitures.get(f);
			if (furn != null)
			{
				furnitures.remove(f);
				undrawFurniture(furn);
			}
			else
				System.err.println("[WARNING @ArchApp] [SEVERITY: High] removeFurniture called with a hashmap which has no mapping for furniture with ID: " + f.getID());
		}
	}
	
	
    /** Goes through all the furniture in the given hashmap and adds them to the rootNode */
	private void drawAllFurniture(HashMap<Furniture, Furniture3D> furnitures)
	{
		if (tracing)
			System.out.println("drawAllFurniture(HashMap<Furniture, Furniture3D>) called.");
		if (furnitures == null)
		{
			System.err.println("[WARNING @ArchApp] [SEVERITY: High] drawAllFurniture called with NULL furnitures.");
			return;
		}
		
		Collection<Furniture3D> furniture = furnitures.values();
		Iterator<Furniture3D> furniture_itr = furniture.iterator();
		while (furniture_itr.hasNext())
			drawFurniture(furniture_itr.next());
	}
	
	
	
	private void undrawAllFurniture (HashMap<Furniture, Furniture3D> furnitures)
	{
		if (tracing)
			System.out.println("undrawAllFurniture(HashMap<Furniture, Furniture3D>) called.");
		if (furnitures == null)
		{
			System.err.println("[WARNING @ArchApp] [SEVERITY: High] undrawAllFurniture called with NULL furnitures.");
			return;
		}
		
		Collection<Furniture3D> furniture = furnitures.values();
		Iterator<Furniture3D> furniture_itr = furniture.iterator();
		while (furniture_itr.hasNext())
			undrawFurniture(furniture_itr.next());		
	}
	
	
	
	private void drawFurniture (Furniture3D f)
	{
		rootNode.attachChild(f.spatial);
		if (physics && f.isPhysical())
			addToPhysics(f);
		if (f.hasLight())
			rootNode.addLight(f.getLight());		
	}
	
	
	
	private void undrawFurniture (Furniture3D f)
	{
		rootNode.detachChild(f.spatial);
		if (physics && f.isPhysical())
			removeFromPhysics(f);
		if (f.hasLight())
			rootNode.removeLight(f.getLight());
	}
	
	
	
	/** Moves the given furniture. Returns if Coords c is not known yet or if f is not
      *  known */
	void updateFurniture(Coords c, Furniture f)
	{
		if (tracing)
			System.out.println("updateFurniture(Coords, Furniture) called.");
		if (c == null || f == null)
			throw new IllegalArgumentException("null");
         
		synchronized(syncLockObject)
		{
			HashMap<Furniture, Furniture3D> furniture = tabFurniture.get(c);
			if (furniture == null)
			{
				System.err.println("[WARNING @ArchApp] [SEVERITY: High] updateFurniture called with NULL furniture.");
				return;
			}

			Furniture3D furn = furniture.get(f);
			if (furn != null)
			{
				// recalculate the furniture's position and move it
				Point center = f.getRotationCenter();
				furn.spatial.setLocalTranslation(center.x,-100f,center.y);
		        if (furn.hasLight())
		        	furn.updateLight();
				
				// recalculate the furniture's rotation and adjust it
				float rotation = (float) (f.getRotation() * 0.5);
				float sinr = FastMath.sin(rotation);
				float cosr = FastMath.cos(rotation);
				Quaternion q = new Quaternion(cosr, 0, sinr, 0);
				furn.spatial.setLocalRotation(q);
				furn.spatial.rotate(0, -FastMath.HALF_PI, -FastMath.PI);
				
				// recalculate the furniture's physics (from position) and update it
				if (furn.physics != null)
					updatePhysics(furn);
			}
		}
	}

    
    
    
    
	/**********************OVERLAY FUNCTIONS**********************/

    public void loadFPSText()
    {
		if (tracing)
			System.out.println("loadFPSText() called.");
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        fpsText = new BitmapText(guiFont, false);
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        fpsText.setText("Mother-licking FPS:");
        guiNode.attachChild(fpsText);
    }

    
    
    public void loadStatsView()
    {
		if (tracing)
			System.out.println("loadStatsView() called.");
        statsView = new StatsView("Statistics View", assetManager, renderer.getStatistics());
        // move it up so it appears above fps text
        statsView.setLocalTranslation(0, fpsText.getLineHeight(), 0);
        guiNode.attachChild(statsView);
    }
	

	
	
	
	/**********************GET FUNCTIONS**********************/

    public MovCam getFlyByCamera()
    {
		if (tracing)
			System.out.println("getFlyByCamera() called.");
        return flyCam;
    }

    
    
    public Node getGuiNode()
    {
		if (tracing)
			System.out.println("getGuiNode() called.");
        return guiNode;
    }

    
    
    public Node getRootNode()
    {
		if (tracing)
			System.out.println("getRootNode() called.");
        return rootNode;
    }

    
    
    public boolean isShowSettings()
    {
		if (tracing)
			System.out.println("isShowSettings() called.");
        return showSettings;
    }

    
    
    public void setShowSettings(boolean showSettings)
    {
		if (tracing)
			System.out.println("setShowSettings() called.");
        this.showSettings = showSettings;
    }
}