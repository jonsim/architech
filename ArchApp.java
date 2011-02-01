import java.awt.Point;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import com.jme3.app.*;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
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
public class ArchApp extends Application {

    private Node rootNode = new Node("Root Node");
    private Node guiNode = new Node("Gui Node");
    private final Object syncLockObject = new Object();

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

    private boolean isInitComplete = false;
    private Main main;

    ArchApp(Main main) {
       super();
       this.main = main;
    }
    
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
        grass = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        grass.setTexture("m_Alpha", assetManager.loadTexture("req/tile.png"));
        grasst = assetManager.loadTexture("req/floor.jpg");
        grasst.setWrap(WrapMode.Repeat);
        grass.setTexture("m_Tex1", grasst);
        grass.setFloat("m_Tex1Scale", 66.6f);
        
		wallmat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
		wallmat.setTexture("m_ColorMap", assetManager.loadTexture("req/wall1.jpg"));
        
        
        simpleInitApp();
        
        if (main.frontEnd != null) tabChangedIgnoreInitComplete(main.frontEnd.getCurrentCoords());
        isInitComplete = true;
    }

    @Override
    public void update() {
		synchronized(syncLockObject) {
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

    public void simpleInitApp() {
		flyCam.setDragToRotate(true);
		addbackg();
            PointLight pl;

            //add a sun
            pl = new PointLight();
            pl.setColor(ColorRGBA.White);
            pl.setRadius(4f);
            rootNode.addLight(pl);

            DirectionalLight dl = new DirectionalLight();
            dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
            dl.setColor(ColorRGBA.White);
            rootNode.addLight(dl);
    }

    public void simpleUpdate(float tpf){
    }

    public void simpleRender(RenderManager rm){
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
		if(lenx==0){length=leny; rotation=-(float) Math.toRadians(90);} else{
		if(leny==0){length=lenx; rotation=0;} else{
    	          length = (int) Math.sqrt((Math.pow(lenx,2) + Math.pow(leny,2)));
    	          length += 2;
    	          rotation = (float) -(Math.atan((double) (leny) / (lenx)));
                if(y2>y1 & x1>x2)  {rotation += Math.PI;}
                if(y2<y1 & x1>x2)  {rotation += Math.PI;}
		}}

		WallGeometry wallGeometry = new WallGeometry();
		Geometry wall;

		//Draw a quad between the two given verticies using dist
		//and angles calculate above
		wall = new Geometry ("Box", new Quad(length,100));
		wall.setMaterial(wallmat);
		wall.setLocalTranslation(new Vector3f(x1, -100, y1));
		wall.rotate(0f, rotation, 0f);

		wallGeometry.geom1 = wall;

		//Double up the quad
		wall = new Geometry ("Box", new Quad(length,100));
		wall.setMaterial(wallmat);
		wall.setLocalTranslation(new Vector3f(x2, -100, y2));
		wall.rotate(0f, (float) (rotation + Math.PI), 0f);

            wallGeometry.geom2 = wall;

            return wallGeometry;
    	}

    private void addbackg(){
		//add the grassy area
	    Quad blah = new Quad(4000,4000);
		Geometry geom = new Geometry("Box", blah);
	    //grass.setTexture("m_ColorMap", grasst);
	    geom.setMaterial(grass);
	    geom.setLocalTranslation(new Vector3f(2102,-100,-902));
	    geom.rotate((float) -Math.toRadians(90),(float) Math.toRadians(180),0f );
	    rootNode.attachChild(geom);

	    //add the sky image
	    rootNode.attachChild(SkyFactory.createSky(
	            assetManager, "req/BrightSky.dds", false));
    }
    
	private void clearall()
    {
		rootNode.detachAllChildren();
		addbackg();
    }

	private Spatial addfurniture(Furniture f) {
         Point center = f.getRotationCenter();
         String name = f.getObjPath();
         Spatial furn = null;
         if(name == null || name.equals("none")) furn = assetManager.loadModel("req/armchair/armchair.obj");
         else {
            String path = "req/" + name.substring(0,name.length()-4) +"/" +name;
            furn = assetManager.loadModel(path);
         }
         furn.scale(5, 5, 5);
         //chair.rotate((float) -(0.5* Math.PI),(float) -(0.5* Math.PI),0);
         furn.rotate(0,(float) -(0.5* Math.PI),0);
         furn.setLocalTranslation(center.x,-100,center.y);

         return furn;
	}

      private class WallGeometry {
         public Geometry geom1;
         public Geometry geom2;
      }
      private final HashMap<Coords, HashMap<Edge, WallGeometry> > tabEdgeGeometry
         = new HashMap<Coords, HashMap<Edge, WallGeometry> >();
      private final HashMap<Coords, HashMap<Furniture, Spatial> > tabFurnitureSpatials
         = new HashMap<Coords, HashMap<Furniture, Spatial> >();

      /** Moves the two wall planes to the new position given by edge e */
      private void updatewall(WallGeometry wallGeometry, Edge e) {
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
      private void updatefurniture(Spatial spatial, Furniture f) {
         rootNode.detachChild(spatial);

         // move the furniture to the new position!
         Point center = f.getRotationCenter();
         spatial.setLocalTranslation(center.x,-100,center.y);

         rootNode.attachChild(spatial);
      }

      /** Goes through all the walls in the given hashmap and adds them to the rootNode */
      private void redrawAllEdges(HashMap<Edge, WallGeometry> edges) {
         Collection<WallGeometry> walls = edges.values();
         Iterator<WallGeometry> iterator = walls.iterator();
         WallGeometry wall;
         while (iterator.hasNext()) {
            wall = iterator.next();
            rootNode.attachChild(wall.geom1);
            rootNode.attachChild(wall.geom2);
         }
      }

      /** Goes through all the furniture in the given hashmap and adds them to the rootNode */
      private void redrawAllFurniture(HashMap<Furniture, Spatial> furniture) {
         Collection<Spatial> spatials = furniture.values();
         Iterator<Spatial> iterator = spatials.iterator();
         while (iterator.hasNext()) {
            rootNode.attachChild(iterator.next());
         }
      }

      // edges is the set of edges associated with those coords, likewise for furniture
      // if these coords havn't been seen before both edges and furniture
      // will be null (its a brand new tab). Either both will be null or not,
      // never one or the other. If the tab has never been seen before then new
      // objects will be created for it
      private void tabChangedIgnoreInitComplete(Coords newTab) {
         synchronized(syncLockObject) {
            if (newTab == null) {
               // no tab selected
               clearall();
               return;
            }

            // Do edges, if this tab already exists, this will not be null and it
            // will get redrawn from below, not recreated!
            HashMap<Edge, WallGeometry> edges = tabEdgeGeometry.get(newTab);
            if (edges == null) {
               // make a brand new edge container for the new tab and add all
               // the edges from the given coords
               edges = new HashMap<Edge, WallGeometry>();
               for (Edge e : newTab.getEdges()) {
                  edges.put(e, makewall(e));
               }

               tabEdgeGeometry.put(newTab, edges);
            }

            // Do furniture, if this tab already exists, this will not be null and it
            // will get redrawn from below, not recreated!
            HashMap<Furniture, Spatial> furniture = tabFurnitureSpatials.get(newTab);
            if (furniture == null) {
               // make a brand new furniture container for the new tab and add all
               // the furniture from the given coords
               furniture = new HashMap<Furniture, Spatial>();
               for (Furniture f : newTab.getFurniture()) {
                  furniture.put(f, addfurniture(f));
               }

               tabFurnitureSpatials.put(newTab, furniture);
            }

            //redraw everything for this tab as the tab has changed
            clearall();
            redrawAllEdges(edges);
            redrawAllFurniture(furniture);
         }
      }

      public void tabChanged(Coords newTab) {
         if (!isInitComplete) return;
         tabChangedIgnoreInitComplete(newTab);

         // Stops you having to click to update the 3D (for tab changes)
         ((JmeCanvasContext) this.getContext()).getCanvas().requestFocus();
      }

      /** Adds the given edge. Returns if Coords c is not known yet or if e is already
       *  added */
      void addEdge(Coords c, Edge e) {
         if (c == null || e == null) throw new IllegalArgumentException("null");

         synchronized(syncLockObject) {
            HashMap<Edge, WallGeometry> edges = tabEdgeGeometry.get(c);
            if (edges == null) return;

            WallGeometry wall = edges.get(e);
            if (wall == null) {
               wall = makewall(e); // make the new wall
               edges.put(e, wall);
               
               rootNode.attachChild(wall.geom1);
               rootNode.attachChild(wall.geom2);
            }
         }
      }

      /** Moves the given edge. Returns if Coords c is not known yet or if e is not
       *  known */
      void updateEdgeChanged(Coords c, Edge e) {
         if (c == null || e == null) throw new IllegalArgumentException("null");
         
         synchronized(syncLockObject) {
            HashMap<Edge, WallGeometry> edges = tabEdgeGeometry.get(c);
            if (edges == null) return;

            WallGeometry wall = edges.get(e);
            if (wall != null) updatewall(wall, e);
         }
      }

      /** Removes the given edge. Returns if Coords c is not known yet or if e is not
       *  known */
      void removeEdge(Coords c, Edge e) {
         if (c == null || e == null) throw new IllegalArgumentException("null");
         
         synchronized(syncLockObject) {
            HashMap<Edge, WallGeometry> edges = tabEdgeGeometry.get(c);
            if (edges == null) return;

            WallGeometry wall = edges.get(e);
            if (wall != null) {
               edges.remove(e);
               rootNode.detachChild(wall.geom1);
               rootNode.detachChild(wall.geom2);
            }
         }
      }

      /** Adds the given furniture. Returns if Coords c is not known yet or if f is already
       *  added */
      void addFurniture(Coords c, Furniture f) {
         if (c == null || f == null) throw new IllegalArgumentException("null");

         synchronized(syncLockObject) {
            HashMap<Furniture, Spatial> furniture = tabFurnitureSpatials.get(c);
            if (furniture == null) return;

            Spatial spatial = furniture.get(f);
            if (spatial == null) {
               spatial = addfurniture(f);
               furniture.put(f, spatial);
               rootNode.attachChild(spatial);
            }
         }
      }

      /** Moves the given furniture. Returns if Coords c is not known yet or if f is not
       *  known */
      void updateFurnitureChanged(Coords c, Furniture f) {
         if (c == null || f == null) throw new IllegalArgumentException("null");
         
         synchronized(syncLockObject) {
            HashMap<Furniture, Spatial> furniture = tabFurnitureSpatials.get(c);
            if (furniture == null) return;

            Spatial spatial = furniture.get(f);
            if (spatial != null) updatefurniture(spatial, f);
         }
      }

      /** Removes the given furniture. Returns if Coords c is not known yet or if f is not
       *  known */
      void removeFurniture(Coords c, Furniture f) {
         if (c == null || f == null) throw new IllegalArgumentException("null");
         
         synchronized(syncLockObject) {
            HashMap<Furniture, Spatial> furniture = tabFurnitureSpatials.get(c);
            if (furniture == null) return;

            Spatial spatial = furniture.get(f);
            if (spatial != null) {
               furniture.remove(f);
               rootNode.detachChild(spatial);
            }
         }
      }
}