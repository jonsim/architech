import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.jme3.app.Application;
import com.jme3.app.StatsView;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeContext.Type;
import com.jme3.system.JmeSystem;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;


/*This code is mostly from the default application included with JME, SimpleApp.
 * It has been modified so that various features and settings can be manipulated
 * such as turning off FPS read-outs and setting a custom camera start-point
 */
public class ArchApp extends Application
{
	private static final short[] DAY_BRIGHTNESS = {245, 240, 200};	// RGB colour (0-255) of the sun light
	private static final Vector3f DAY_ANGLE = new Vector3f(0.8f, -2, -0.2f);  // vector direction of the sun
	private static final float DAY_SHADOW_INTENSITY = 0.5f;  // 0 = no shadows, 1 = pitch black shadows
	private static final short[] NIGHT_BRIGHTNESS = {30, 30, 30};	// RGB colour (0-255) of the sun light
	private static final Vector3f NIGHT_ANGLE = new Vector3f(-0.2f, -0.8f, 0.6f);  // vector direction of the sun
	private static final float NIGHT_SHADOW_INTENSITY = 0.1f;  // 0 = no shadows, 1 = pitch black shadows
	private boolean day = true;  // true = day, false = night
	private Spatial DAY_MAP;  // must be initialised in SimpleInitApp() due to dependence on assetManager
	private Spatial NIGHT_MAP;  // must be initialised in SimpleInitApp() due to dependence on assetManager
	private DirectionalLight sun;

    private Node rootNode = new Node("Root Node");
    private Node guiNode = new Node("Gui Node");
    private final Object syncLockObject = new Object();

    private BitmapText fpsText;
    private BitmapFont guiFont;
    private StatsView statsView;
    
    private Material grass;
    private Texture grasst;
    private Material wallmat;
    
    private PssmShadowRenderer psr;

    private static MovCam flyCam;
    private boolean showSettings = false;

    private AppActionListener actionListener = new AppActionListener();

    private boolean isInitComplete = false;
    private Main main;

    ArchApp(Main main)
    {
    	super();
    	this.main = main;
    }
    
    private class AppActionListener implements ActionListener
    {
        public void onAction(String name, boolean value, float tpf)
        {
            if (!value)
                return;

            if (name.equals("SIMPLEAPP_Exit"))
            {
            	stop();
            }
            else if (name.equals("SIMPLEAPP_CameraPos"))
            {
            	/*if (cam != null)
            	{
	                Vector3f loc = cam.getLocation();
	                Quaternion rot = cam.getRotation();
	                System.out.println("Camera Position: ("+
	                        loc.x+", "+loc.y+", "+loc.z+")");
	                System.out.println("Camera Rotation: "+rot);
	                System.out.println("Camera Direction: "+cam.getDirection());
	            }*/
	        }
            else if (name.equals("SIMPLEAPP_Memory"))
            {
            	BufferUtils.printCurrentDirectMemory(null);
            }
        }
    }

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

    @Override
    public void initialize()
    {
        super.initialize();
        initCamera();
        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
       // loadFPSText();
       // loadStatsView();
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        if (inputManager != null)
        {
            flyCam = new MovCam(cam);
            flyCam.setMoveSpeed(100f);
            flyCam.registerWithInput(inputManager);

            if (context.getType() == Type.Display)
                inputManager.addMapping("SIMPLEAPP_Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));
            
            inputManager.addMapping("SIMPLEAPP_CameraPos", new KeyTrigger(KeyInput.KEY_C));
            inputManager.addMapping("SIMPLEAPP_Memory", new KeyTrigger(KeyInput.KEY_M));
            inputManager.addListener(actionListener, "SIMPLEAPP_Exit", "SIMPLEAPP_CameraPos", "SIMPLEAPP_Memory");
        }
        
        // call user code
        //grass = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        grass = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        //grass.setTexture("m_Alpha", assetManager.loadTexture("req/tile.png"));
        grass.setTexture("DiffuseMap", assetManager.loadTexture("req/floor.jpg"));
        grasst = assetManager.loadTexture("req/floor.jpg");
        grasst.setWrap(WrapMode.Repeat);
        //grass.setTexture("DiffuseMap", grasst);
        grass.setFloat("Shininess", 10);

        wallmat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		wallmat.setTexture("DiffuseMap", assetManager.loadTexture("req/wall1.jpg"));
        wallmat.setFloat("Shininess", 10);
        
        simpleInitApp();
        
        if (main.frontEnd != null) tabChangedIgnoreInitComplete(main.frontEnd.getCurrentCoords());
        isInitComplete = true;
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

    public void simpleInitApp()
    {
		flyCam.setDragToRotate(true);
		addbackg();
		
        // add shadow renderer
        rootNode.setShadowMode(ShadowMode.Off);
        psr = new PssmShadowRenderer(assetManager, 1024, 4);
    	psr.setShadowIntensity(DAY_SHADOW_INTENSITY);
        psr.setDirection(DAY_ANGLE);
        viewPort.addProcessor(psr);

		// add sun
        sun = new DirectionalLight();
        sun.setDirection(DAY_ANGLE);
        sun.setColor(new ColorRGBA((float) DAY_BRIGHTNESS[0]/255, (float) DAY_BRIGHTNESS[1]/255, (float) DAY_BRIGHTNESS[2]/255, 1));
        rootNode.addLight(sun);
        
        // add ambient point light
        PointLight pl = new PointLight();
        //pl.setPosition(localCentre);
        pl.setColor(ColorRGBA.White);
        pl.setRadius(200);
        rootNode.addLight(pl);
        Geometry pl1_model = new Geometry("Sphere", new Sphere(8, 8, 5));
        pl1_model.setMaterial(assetManager.loadMaterial("Common/Materials/WhiteColor.j3m"));
        //pl1_model.setLocalTranslation(localCentre);
        rootNode.attachChild(pl1_model);
                
        // create and add sky
        DAY_MAP = SkyFactory.createSky(assetManager, "req/SkyDay.dds", false);
        NIGHT_MAP = SkyFactory.createSky(assetManager, "req/SkyNight.dds", false);
	    rootNode.attachChild(DAY_MAP);
    }

    public void simpleUpdate(float tpf)
    {
    }

    public void simpleRender(RenderManager rm)
    {
    }
    
    public void toggleDay()
    {
    	if (day)
    	{
            sun.setDirection(NIGHT_ANGLE);
            psr.setDirection(NIGHT_ANGLE);
        	psr.setShadowIntensity(NIGHT_SHADOW_INTENSITY);
            sun.setColor(new ColorRGBA((float) NIGHT_BRIGHTNESS[0]/255, (float) NIGHT_BRIGHTNESS[1]/255, (float) NIGHT_BRIGHTNESS[2]/255, 1));
            rootNode.detachChild(DAY_MAP);
            rootNode.attachChild(NIGHT_MAP);
            day = false;
    	}
    	else
    	{
            sun.setDirection(DAY_ANGLE);
            psr.setDirection(DAY_ANGLE);
        	psr.setShadowIntensity(DAY_SHADOW_INTENSITY);
            sun.setColor(new ColorRGBA((float) DAY_BRIGHTNESS[0]/255, (float) DAY_BRIGHTNESS[1]/255, (float) DAY_BRIGHTNESS[2]/255, 1));    		
            rootNode.detachChild(NIGHT_MAP);
            rootNode.attachChild(DAY_MAP);
            day = true;
    	}
    }


    // constructs a wall between (x1,y1) and (x2,y2), doesn't add it to rootnode
    private WallGeometry makewall(Edge e)
    {
    	int x1 = (int) e.getV1().getX();
    	int y1 = (int) e.getV1().getY();
    	int x2 = (int) e.getV2().getX();
    	int y2 = (int) e.getV2().getY();

    	int length,leny,lenx=0;
    	float rotation=0;

		//work out the distances and angles required
		lenx = x2 - x1;
		leny = y2 - y1;
		if(lenx==0)
		{
			length=leny;
			rotation=-(float) Math.toRadians(90);
		}
		else
		{
			if(leny==0)
			{
				length=lenx;
				rotation=0;
			}
			else
			{
				length = (int) Math.sqrt((Math.pow(lenx,2) + Math.pow(leny,2)));
				length += 2;
				rotation = (float) -(Math.atan((double) (leny) / (lenx)));
				if(y2>y1 & x1>x2)
					rotation += FastMath.PI;
				if(y2<y1 & x1>x2)
					rotation += FastMath.PI;
			}
		}

		WallGeometry wallGeometry = new WallGeometry();
		// Draw a quad between the 2 given vertices
		// geometry settings
		wallGeometry.geom1 = new Geometry ("Box", new Quad(length,100));
		wallGeometry.geom1.setLocalTranslation(new Vector3f(x1, -100, y1));
		wallGeometry.geom1.rotate(0f, rotation, 0f);
		wallGeometry.geom1.setMaterial(wallmat);
    	
    	// shadow settings
    	//TangentBinormalGenerator.generate(wallGeometry.geom1.getMesh(), true);
    	wallGeometry.geom1.setShadowMode(ShadowMode.CastAndReceive);


		// Double up the quad
		// geometry settings
		wallGeometry.geom2 = new Geometry ("Box", new Quad(length,100));
		wallGeometry.geom2.setLocalTranslation(new Vector3f(x2, -100, y2));
		wallGeometry.geom2.rotate(0f, (float) (rotation + FastMath.PI), 0f);
		wallGeometry.geom2.setMaterial(wallmat);
    	
    	// shadow settings
    	//TangentBinormalGenerator.generate(wallGeometry.geom2.getMesh(), true);
    	wallGeometry.geom2.setShadowMode(ShadowMode.CastAndReceive);

    	
        return wallGeometry;
	}

    private void addbackg()
    {
		//add the grassy area
		Geometry geom = new Geometry("Box", new Quad(4000,4000));
	    //grass.setTexture("m_ColorMap", grasst);
	    geom.setMaterial(grass);
	    geom.setShadowMode(ShadowMode.Receive);
	    geom.setLocalTranslation(new Vector3f(2102,-100,-902));
	    geom.rotate((float) -Math.toRadians(90),(float) Math.toRadians(180),0f );
	    rootNode.attachChild(geom);
    }
    
	private void clearall()
    {
		rootNode.detachAllChildren();
		addbackg();
    }

	private Spatial addfurniture(Furniture f)
	{
        Spatial furn = null;
        Material furn_mat;
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
        furn.rotate(0,(float) -(FastMath.HALF_PI),0);
        furn.setLocalTranslation(center.x,-100,center.y);
        
        // lighting settings
    	furn_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        furn_mat.setFloat("Shininess", 10);
    	furn.setMaterial(furn_mat);
    	
    	// shadow settings
    	//TangentBinormalGenerator.generate(furn.getMesh(), true);
    	furn.setShadowMode(ShadowMode.CastAndReceive);

        return furn;
	}

	private class WallGeometry
	{
		public Geometry geom1;
		public Geometry geom2;
	}
	private final HashMap<Coords, HashMap<Edge, WallGeometry> > tabEdgeGeometry
		= new HashMap<Coords, HashMap<Edge, WallGeometry> >();
	private final HashMap<Coords, HashMap<Furniture, Spatial> > tabFurnitureSpatials
		= new HashMap<Coords, HashMap<Furniture, Spatial> >();

	/** Moves the two wall planes to the new position given by edge e */
	private void updatewall(WallGeometry wallGeometry, Edge e)
	{
		rootNode.detachChild(wallGeometry.geom1);
		rootNode.detachChild(wallGeometry.geom2);
	
		// move the wall, this makes a completely new one (for now)!
		WallGeometry completelyNew = makewall(e);
		wallGeometry.geom1 = completelyNew.geom1;
		wallGeometry.geom2 = completelyNew.geom2;
	
		rootNode.attachChild(wallGeometry.geom1);
		rootNode.attachChild(wallGeometry.geom2);
	}

	/** Moves the given spatial to the new position of furniture f */
	private void updatefurniture(Spatial spatial, Furniture f)
	{
		//rootNode.detachChild(spatial);

		// move the furniture to the new position!
		Point center = f.getRotationCenter();
		spatial.setLocalTranslation(center.x,-100,center.y);

		rootNode.attachChild(spatial);
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
			rootNode.attachChild(wall.geom1);
			rootNode.attachChild(wall.geom2);
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

	// edges is the set of edges associated with those coords, likewise for furniture
	// if these coords havn't been seen before both edges and furniture
	// will be null (its a brand new tab). Either both will be null or not,
	// never one or the other. If the tab has never been seen before then new
	// objects will be created for it
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

	/** Public function to prepare edges for the given coords. If these coords
	 *  havn't been seen before then new objects will be created for it. */
	void tabChanged(Coords newTab)
	{
		if (!isInitComplete) return;
		tabChangedIgnoreInitComplete(newTab);

		// Stops you having to click to update the 3D (for tab changes)
		((JmeCanvasContext) this.getContext()).getCanvas().requestFocus();
	}

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

	/** Adds the given edge. Returns if Coords c is not known yet or if e is already
      *  added */
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
               
				rootNode.attachChild(wall.geom1);
				rootNode.attachChild(wall.geom2);
			}
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
				rootNode.detachChild(wall.geom1);
				rootNode.detachChild(wall.geom2);
			}
		}
	}

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
}