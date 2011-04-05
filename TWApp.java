import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jme3tools.converters.ImageToAwt;
import com.jme3.app.*;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
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
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.BufferUtils;

/*This code is mostly from the default application included with JME, SimpleApp.
 * It has been modified so that various features and settings can be manipulated
 * such as turning off FPS read-outs and setting a custom camera start-point
 */
public class TWApp extends Application implements ActionListener {

    private Node rootNode = new Node("Root Node");
    private Node guiNode = new Node("Gui Node");
    private ArrayList<Compone> furniture = new ArrayList<Compone>();
    private int currentid = 0;

    private BitmapText fpsText;
    private BitmapFont guiFont;
    private StatsView statsView;
    
    private static TWCam flyCam;
    private boolean showSettings = false;

    private AppActionListener actionListener = new AppActionListener();

    private boolean isInitComplete = false;
    public boolean isInitComplete(){return isInitComplete;}

    TWApp() {
       super();
    }
    
    private class AppActionListener implements ActionListener {
        public void onAction(String name, boolean value, float tpf) {
            if (!value)
                return;

            if (name.equals("SIMPLEAPP_Exit")){
                    stop();
                }else if (name.equals("SIMPLEAPP_CameraPos")){
                    if (cam != null){
                        /*Vector3f loc = cam.getLocation();
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
            if (!JmeSystem.showSettingsDialog(settings, true))
                return;
        }
        super.start();
    }

    public TWCam getFlyByCamera() {
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
        cam.setLocation(new Vector3f(190f, -70f,200f));
        cam.lookAt(new Vector3f(0f, -70f, 0f), Vector3f.UNIT_Y);
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
            flyCam = new TWCam(cam);
            flyCam.setMoveSpeed(100f);
            flyCam.registerWithInput(inputManager);

            if (context.getType() == Type.Display)
                inputManager.addMapping("SIMPLEAPP_Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));
            
            inputManager.addMapping("SIMPLEAPP_CameraPos", new KeyTrigger(KeyInput.KEY_C));
            inputManager.addMapping("SIMPLEAPP_Memory", new KeyTrigger(KeyInput.KEY_M));
            inputManager.addListener(actionListener, "SIMPLEAPP_Exit",
                                     "SIMPLEAPP_CameraPos", "SIMPLEAPP_Memory");
        }
        simpleInitApp();        
        isInitComplete = true;
    }

    @Override
    public void update() {
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

    public void simpleInitApp() {
		flyCam.setDragToRotate(true);
		addbackg();
		PointLight pl;
		
		pl = new PointLight();
		pl.setColor(ColorRGBA.White);
		pl.setRadius(4f);
		rootNode.addLight(pl);
		
		DirectionalLight dl = new DirectionalLight();
		dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
		dl.setColor(ColorRGBA.White);
		rootNode.addLight(dl);
    }

    public void additem(String path, String fname,int id){
    	
    	if(path!=null) {assetManager.registerLocator(path,FileLocator.class.getName());}
    	Spatial temp = assetManager.loadModel(fname);
	    temp.setLocalScale(3f);
	    temp.setLocalTranslation(108,-100,112);
	    rootNode.attachChild(temp);
	    
	    //read mtl file
	    File mat = new File(path+"/"+fname.substring(0, fname.lastIndexOf('.'))+".mtl"); 
	    StringBuffer contents = new StringBuffer();
	    BufferedReader readall = null;  
	    String text = null;
	    try {
			readall  = new BufferedReader(new FileReader(mat));
			while ((text = readall.readLine()) != null) {
				if(text.contains("newmtl")){contents.append("newmtl mat" + currentid).append(System.getProperty("line.separator"));}
				else{contents.append(text).append(System.getProperty("line.separator"));}}
			readall.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "No material file (mtl) found , aborting load","No MTL Found", 1);
			//main.remove(control);
			return;
		}
		String matfile = contents.toString();
		Compone adding = new Compone(temp, id,matfile,path,fname);	    
	    furniture.add(adding);
	    //currentid++;
	    focus();
	    return;
    }
    
    private int converttype(String type){
    	if(type.equals("Chair")) {return 1;}
    	if(type.equals("Armchair")) {return 2;}
    	if(type.equals("Sofa (2 person)")) {return 3;}
    	if(type.equals("Stool")) {return 4;}
    	if(type.equals("Bench")) {return 5;}
    	if(type.equals("Dining Table")) {return 6;}
    	if(type.equals("Desk")) {return 7;}
    	if(type.equals("Coffee Table")) {return 8;}
    	if(type.equals("Bedside Table")) {return 9;}
    	if(type.equals("Desk Lamp")) {return 10;}
    	if(type.equals("Table Lamp")) {return 11;}
    	if(type.equals("Floor Lamp")) {return 12;}
    	if(type.equals("Wall Light")) {return 13;}
    	if(type.equals("Ceiling Light")) {return 14;}
    	if(type.equals("Cupboard")) {return 15;}
    	if(type.equals("Drawers")) {return 16;}
    	if(type.equals("Wardrobe")) {return 17;}
    	if(type.equals( "Bookcase")) {return 18;}
    	if(type.equals("Wall-mounted Cupboard")) {return 19;}
    	if(type.equals("Kitchen Units")) {return 20;}
    	if(type.equals("Single Bed")) {return 21;}
    	if(type.equals("Bath (w/Shower)")) {return 22;}
    	if(type.equals("Shower")) {return 23;}    	
    	if(type.equals("Bathroom Sink")) {return 24;}
    	if(type.equals("Toilet")) {return 25;}
    	if(type.equals("Oven")) {return 26;}
    	if(type.equals("Fridge")) {return 27;}
    	if(type.equals("Freezer")) {return 28;}
    	if(type.equals("Kitchen Sink")) {return 29;}
    	if(type.equals("DishWasher")) {return 30;}
    	if(type.equals("Rug")) {return 40;}
    	if(type.equals("Double Bed")) {return 41;}
    	if(type.equals("Sofa (3 person)")) {return 42;}
    	if(type.equals("Large Plant")) {return 45;}
    	if(type.equals("Pot Plant")) {return 46;}
    	return 0;
    }
    
    public void saveitem(String name, String description, String types, ObjectBrowser obrow){
    	//String name,description;
    	int type;
    	if(furniture.size()==0){JOptionPane.showMessageDialog(null, "No objects have been added. Nothing to save","No Objects Added", 1);}
	    	else{
    		if(name.equals("")){
   			 JOptionPane.showMessageDialog(null, "Blank Name - please fill in that field","Blank Name", 1);
   			 }else{
   		     if(description.equals("")){
   					 JOptionPane.showMessageDialog(null, "Blank Description - please fill in that field","Blank Description", 1);
   			 }else{
    		if(types.equals("None")){
   			 JOptionPane.showMessageDialog(null, "No Type selected - please select one","Blank Name", 1);
   			 }else{
    		type = converttype(types);
        	URL folder = getClass().getResource("req");
        	String fpath = folder.getPath();
	        (new File(fpath+"/"+name)).mkdir();
    		try{
			FileWriter fstream = new FileWriter(fpath+"/"+name+"/"+name+".obj");
			FileWriter mstream = new FileWriter(fpath+"/"+name+"/"+name+".mtl");
			BufferedWriter out = new BufferedWriter(fstream);
			BufferedWriter outm = new BufferedWriter(mstream);
    		int vcount = 0,vncount =0,vtcount=0;
    		int pvcount = 0,pvncount =0,pvtcount=0;
    		Double maxheight=0.0, maxwidth=0.0,maxlength=0.0;
    		out.write("mtllib " + name+".mtl");
    		out.write(System.getProperty("line.separator"));
	    	for(int i = 0; i<furniture.size(); i++){	    		
	    	        //OFFSETS
	        		Double rotateangle = furniture.get(i).getrot();
	        		Double sfx = (furniture.get(i).getscalex());
	        		if(sfx>1f){sfx = 1 + (sfx-1.0)/3.0;}
	        		if(sfx<1f){sfx = 1 - (1.0-sfx)/3.0;}
	        		Double sfy = (furniture.get(i).getscaley());
	        		if(sfy>1f){sfy = 1 + (sfy-1.0)/3.0;}
	        		if(sfy<1f){sfy = 1 - (1.0-sfy)/3.0;}
	        		Double sfz = (furniture.get(i).getscalez());
	        		if(sfz>1f){sfz = 1 + (sfz-1.0)/3.0;}
	        		if(sfz<1f){sfz = 1 - (1.0-sfz)/3.0;}
	    			Loc3f disp = furniture.get(i).getdis();
	        		Double disx = Double.parseDouble(Float.toString(disp.x()));
	        		Double disy = Double.parseDouble(Float.toString(disp.y()));
	        		Double disz = Double.parseDouble(Float.toString(disp.z()));
	        		//CALCULATE DIMENSIONS
	    			BoundingBox bb = (BoundingBox) furniture.get(i).getsp().getWorldBound();
	    			Vector3f store=null;
	    			store = bb.getExtent(store);
	    			DecimalFormat tdp = new DecimalFormat("#.##");
	    			Double width =  Double.valueOf(tdp.format((store.getX()/18) + Math.abs(disp.x()/36)));
	    			Double height = Double.valueOf(tdp.format((store.getY()/18) + Math.abs(disp.z()/36)));
	    			Double length = Double.valueOf(tdp.format((store.getZ()/18) + Math.abs(disp.y()/36)));
	    			//CHECK IF THEY EXCEED PREVIOUS MAXIMA
	    			if(width>maxwidth){maxwidth = width;}
	    			if(length>maxlength){maxlength = length;}
	    			if(height>maxheight){maxheight = height;}	    			
	    			 //COPY OBJ ACROSS
                    vcount = pvcount;
                    vncount = pvncount;
                    vtcount = pvtcount;
                    String[] parts = null;
                    File inputx = new File(furniture.get(i).path());
                BufferedReader readall = null;  
            readall  = new BufferedReader(new FileReader(inputx));
            String text = null;

            while ((text = readall.readLine()) != null)
            {
                    if(text.contains("mtllib")){}else{
                    if(text.contains("usemtl")){out.write("usemtl mat" + furniture.get(i).getid());
                            out.write(System.getProperty("line.separator"));
                    } else
                            if(text.substring(0,2).equals("v ")){
                                    pvcount++;
                                    parts = text.split(" ");                                                
                                    //apply scaling factor and offset
                                    Double x = (Double.parseDouble(parts[1]) * sfx)+(disx/3.0);
                                    Double b = (Double.parseDouble(parts[2]) * sfy)+(disz/3.0);
                                    Double y = (Double.parseDouble(parts[3]) * sfz)+(disy/3.0);
                                    //calculate geometry for rotation
                                    Double hyp = Math.sqrt(Math.pow(x,2.0) + Math.pow(y,2.0));
                                    Double angle = Math.atan(x/y);
                                    angle += rotateangle;
                                    if(y<0){hyp = 0.0-hyp;}
                                    x=Math.sin(angle) * hyp;
                                    y=Math.cos(angle) * hyp;
                                    out.write(parts[0] + " " + x + " " + b +  " " + y);
                                    out.write(System.getProperty("line.separator"));
                            }
                            else{
                                    if(text.charAt(0) == 'f' && i>0){
                                    parts = text.split(" ");
                                    int x,y,z;
                                    out.write("f");
                                    for(int j =1;j<parts.length;j++){
                                            if(parts[j].contains("//")){
                                                    x = Integer.parseInt(parts[j].split("//")[0]) + vcount;
                                                    z = Integer.parseInt(parts[j].split("//")[1]) + vncount;
                                                    out.write(" " + x + "//" + z);
                                            }else{
                                                    x= Integer.parseInt(parts[j].split("/")[0]) + vcount;
                                                    y= Integer.parseInt(parts[j].split("/")[1]) + vtcount;
                                                    z= Integer.parseInt(parts[j].split("/")[2]) + vncount;
                                                    out.write(" " + x + "/" + y + "/" + z);
                                            }
                                    }
                                    out.write(System.getProperty("line.separator"));
                            }else{
                                    if(text.substring(0,2).equals("vn")){pvncount++;}
                                    if(text.substring(0,2).equals("vt")){pvtcount++;}
                                    if(!text.equals("")) {
                                            out.write(text);
                                            out.write(System.getProperty("line.separator"));}
                    }}}
	    	        }
	    	        if (readall != null) readall.close();
	    	        //append the mtl file onto the new mtl
		    		outm.write(furniture.get(i).getmat());
		    		//now copy all pictures files
		    		if(furniture.get(i).getcol()==0){
		    			if(furniture.get(i).getpic()==null){
			    		String dir = furniture.get(i).getdir();
			    		File direc = new File(dir);
			    		String[] children = direc.list();
			    		for(int l = 0 ; l<children.length;l++){
			    			if(children[l].lastIndexOf(".")!=-1){
			    			if(children[l].substring(children[l].lastIndexOf(".")).equals(".jpg") ||
			    					children[l].substring(children[l].lastIndexOf(".")).equals(".gif") ||
			    					children[l].substring(children[l].lastIndexOf(".")).equals(".png")){
			    				if(children[l].contains("ss")){}
			    				else{
			    				File file = new File(dir + "/" + children[l]);
			    				copyf(file,new File(fpath+"/"+name+"/" + file.getName()));}
			    			}}
			    		}}else{
			    			File pic = new File(furniture.get(i).getpic());
			    			copyf(pic,new File(fpath+"/"+name+"/" + pic.getName()));	
			    		}
		    		}
	    		}	    		
	    		out.close();
	    		outm.close();	  
	    		focus();
	    		//TAKE PRETTY SCREENSHOT
	    		Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();
	    		int tx = (int) 0 + 80;
	    		int ty = (int) 0 + 130;
	    		int dx = (int) (scrDim.getWidth()/3)*2;
	    		int dy = (int) (scrDim.getHeight()/3)*2;   		
	    		Robot robot = new Robot();
	    		BufferedImage bi = robot.createScreenCapture(new Rectangle(tx,ty,dx,dy));
	    		folder = getClass().getResource("img");
	        	fpath = folder.getPath();
	    		ImageIO.write(bi, "jpg", new File(fpath + "/database/" + name+".jpg"));
	    		//OUTPUT LENGTH, WIDTH, HEIGHT
	    		//main.addtodb(name,type,description,name+".jpg",name+".obj",maxwidth.floatValue(),maxlength.floatValue(),maxheight.floatValue());
	        	obrow.addObject(name,type,description,name+".jpg",name+".obj",maxwidth.floatValue(),maxlength.floatValue(),maxheight.floatValue());
	        	JOptionPane.showMessageDialog(null, "Saved Successfully!", "Success", 1);
    			}catch(Exception e){e.printStackTrace(); JOptionPane.showMessageDialog(null, "Something went wrong during the saving" +
	    				" process","OBJ Saving Error", 1);}	
   			 }}}}
    }
    
    void copyf(File sour,File tar){
    try{
	    InputStream source = new FileInputStream(sour);
	    OutputStream targ = new FileOutputStream(tar);
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = source.read(buf)) > 0) {
	        targ.write(buf, 0, len);
	    }
	    source.close();
	    targ.close();}
    catch(Exception e){}
    }

    
    public void removeitem(int id){
    	int position=0;
    	int found = -1;
    	Iterator iterator = furniture.iterator();
		while(iterator.hasNext()){
			if(((Compone) iterator.next()).getid() == id){
				found = position;
			}
			position++;
		}
		if(found!=-1){
			rootNode.detachChild(furniture.get(found).getsp());
			furniture.remove(found);
			focus();
		}
	    return;
    }
    
    public void moveitem(int id, char dir){
    	int position=0;
    	int found = -1;
    	Iterator iterator = furniture.iterator();
		while(iterator.hasNext()){
			if(((Compone) iterator.next()).getid() == id){
				found = position;
			}
			position++;
		}
		if(found!=-1){
			Spatial item = furniture.get(found).getsp();
			Compone parent = furniture.get(found);
			float curx = item.getWorldTranslation().getX();
			float cury = item.getWorldTranslation().getY();
			float curz = item.getWorldTranslation().getZ();
			float sx = item.getLocalScale().getX();
			float sy = item.getLocalScale().getY();
			float sz = item.getLocalScale().getZ();
			if(dir=='l'){item.setLocalTranslation(curx+5,cury,curz); parent.appendx(5);}
			if(dir=='r'){item.setLocalTranslation(curx-5,cury,curz); parent.appendx(-5);}
			if(dir=='f'){item.setLocalTranslation(curx,cury,curz+5); parent.appendy(5);}
			if(dir=='b'){item.setLocalTranslation(curx,cury,curz-5); parent.appendy(-5);}
			if(dir=='u'){item.setLocalTranslation(curx,cury+1,curz); parent.appendz(1);}
			if(dir=='d'){item.setLocalTranslation(curx,cury-1,curz); parent.appendz(-1);;}
			if(dir=='+'){item.setLocalScale(new Vector3f(sx+0.25f,sy+0.25f,sz+0.25f)); parent.appendscalex(0.25);parent.appendscaley(0.25);parent.appendscalez(0.25);}
			if(dir=='-'){if(!(parent.getscalex()==0.25) & !(parent.getscaley()==0.25) & !(parent.getscalez()==0.25)){
				item.setLocalScale(new Vector3f(sx-0.25f,sy-0.25f,sz-0.25f)); parent.appendscalex(-0.25);parent.appendscaley(-0.25);parent.appendscalez(-0.25);}}
			if(dir=='x'){item.setLocalScale(new Vector3f(sx+0.25f,sy,sz)); parent.appendscalex(0.25);}
			if(dir=='1'){if(!(parent.getscalex()==0.25)){item.setLocalScale(new Vector3f(sx-0.25f,sy,sz)); parent.appendscalex(-0.25);}}
			if(dir=='y'){item.setLocalScale(new Vector3f(sx,sy+0.25f,sz)); parent.appendscaley(0.25);}
			if(dir=='2'){if(!(parent.getscaley()==0.25)){item.setLocalScale(new Vector3f(sx,sy-0.25f,sz)); parent.appendscaley(-0.25);}}
			if(dir=='z'){item.setLocalScale(new Vector3f(sx,sy,sz+0.25f)); parent.appendscalez(0.25);}
			if(dir=='3'){if(!(parent.getscalez()==0.25)){item.setLocalScale(new Vector3f(sx,sy,sz-0.25f)); parent.appendscalez(-0.25);}}
			if(dir=='>'){item.rotate(0f,(float) (Math.PI*0.125),0f);parent.appendrot(Math.PI*0.125);}
			if(dir=='<'){item.rotate(0f,-((float) (Math.PI*0.125)),0f);parent.appendrot(-Math.PI*0.125);}
			focus();
		}
	    return;
    }
    
    public void paintitem(int id,int swit,String path, String name, Float tex, float red, float green,float blue){
    	Material mat =null;
    	int position=0;
    	int found = -1;
    	Iterator iterator = furniture.iterator();
		while(iterator.hasNext()){
			if(((Compone) iterator.next()).getid() == id){
				found = position;
			}
			position++;
		}
		if(found!=-1){			
	    	if(swit==0){
	            //if painting
	        	mat= new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
	    	    mat.setFloat("m_Shininess", 10f);
	            mat.setColor("m_Diffuse", new ColorRGBA(red,green,blue,(float) 1.0));
	            mat.setBoolean("m_UseMaterialColors",true);
	            furniture.get(found).colour(red,green,blue);
	            }else{
	            if(tex != 0f){if(furniture.get(found).getpic()==null) {return;}}
	        	//if texturing
	    	    mat = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
	    	    assetManager.registerLocator(path,FileLocator.class.getName());	    
	    	    mat.setTexture("m_Alpha", assetManager.loadTexture("req/tile.png"));
	    	    Texture grass = assetManager.loadTexture(name);
	    	    grass.setWrap(WrapMode.Repeat);
	    	    mat.setTexture("m_Tex1", grass);
	    	    furniture.get(found).appendtile(tex);
	    	    furniture.get(found).texture(path,name);
	    	    if(furniture.get(found).gettile()==0) {furniture.get(found).appendtile(1);}
	    	    mat.setFloat("m_Tex1Scale", furniture.get(found).gettile());
	    	    }
			furniture.get(found).getsp().setMaterial(mat);
			focus();
		}   
    }
    
    public void focus() {
    	((JmeCanvasContext) this.getContext()).getCanvas().requestFocus();
    	return;
    }

    public void simpleUpdate(float tpf){
    }

    public void simpleRender(RenderManager rm){
    }

    void addbackg(){
    	TerrainQuad terrain;
		Material mat_terrain;
		Texture grass;
		
	    mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
	    mat_terrain.setTexture("m_Alpha", assetManager.loadTexture("req/tile.png"));
	    grass = assetManager.loadTexture("req/floor.jpg");
	    grass.setWrap(WrapMode.Repeat);
	    mat_terrain.setTexture("m_Tex1", grass);
	    mat_terrain.setFloat("m_Tex1Scale", 10f);
	    
	    Quad blah = new Quad(200,200);
		Geometry geom = new Geometry("Box", blah);
		geom.setMaterial(mat_terrain);
		geom.setLocalTranslation(new Vector3f(200,-100,0));
		geom.rotate((float) -Math.toRadians(90),(float) Math.toRadians(180),0f );
	    rootNode.attachChild(geom);
	    
	    mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
	    mat_terrain.setTexture("m_Alpha", assetManager.loadTexture("req/tile.png"));
	    grass = assetManager.loadTexture("req/floor.jpg");
	    grass.setWrap(WrapMode.Repeat);
	    mat_terrain.setTexture("m_Tex1", grass);
	    mat_terrain.setFloat("m_Tex1Scale", 5f);
	    
	    blah = new Quad(200,200);
		geom = new Geometry("Box", blah);
		geom.setMaterial(mat_terrain);
		geom.setLocalTranslation(new Vector3f(200,0,200));
		geom.rotate((float) -Math.toRadians(270),(float) Math.toRadians(180),0f );
	    rootNode.attachChild(geom);
	    
	    mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
	    mat_terrain.setTexture("m_Alpha", assetManager.loadTexture("req/tile.png"));
	    grass = assetManager.loadTexture("req/floor.jpg");
	    grass.setWrap(WrapMode.Repeat);
	    mat_terrain.setTexture("m_Tex1", grass);
	    mat_terrain.setFloat("m_Tex1Scale",8f);
	    
	    Geometry wall = new Geometry ("Box", new Quad(200,100));
		wall.setMaterial(mat_terrain);
		wall.setLocalTranslation(new Vector3f(0, -100, 0));
		wall.rotate(0f, (float) (Math.toRadians(180) + Math.PI), 0f);
		rootNode.attachChild(wall);
		
		wall = new Geometry ("Box", new Quad(200,100));
		wall.setMaterial(mat_terrain);
		wall.setLocalTranslation(new Vector3f(200, -100, 0));
		wall.rotate(0f, (float) (Math.toRadians(90) + Math.PI), 0f);
		rootNode.attachChild(wall);
		
		wall = new Geometry ("Box", new Quad(200,100));
		wall.setMaterial(mat_terrain);
		wall.setLocalTranslation(new Vector3f(200, -100, 200));
		wall.rotate(0f, (float) (Math.toRadians(360) + Math.PI), 0f);
		rootNode.attachChild(wall);
		
		wall = new Geometry ("Box", new Quad(200,100));
		wall.setMaterial(mat_terrain);
		wall.setLocalTranslation(new Vector3f(0, -100, 200));
		wall.rotate(0f, (float) (Math.toRadians(270) + Math.PI), 0f);
		rootNode.attachChild(wall);	
		/*
		assetManager.registerLocator("C:/Users/Robin/Documents/University/SPE/google cvs/beta resources backup/req main/small_bookcase/",FileLocator.class.getName());
	    furniture[0] = assetManager.loadModel("req/merge.obj");
	    furniture[0].scale(10f, 10f, 10f);
	    furniture[0].setLocalTranslation(108,-100,112);
	    rootNode.attachChild(furniture[0]);
	    
	    furniture[1] = assetManager.loadModel("req/bench.obj");
	    furniture[1].scale(5f, 5f, 5f);
	    furniture[1].setLocalTranslation(108,-100,112);
	    //rootNode.attachChild(furniture[1]);
	    
	    furniture[2] = assetManager.loadModel("req/shower.obj");
	    furniture[2].scale(5f, 5f, 5f);
	    furniture[2].setLocalTranslation(108,-100,112);
	    //rootNode.attachChild(furniture[2]);
	    rootNode.attachChild(geom);*/
    	}

	@Override
	public void onAction(String arg0, boolean arg1, float arg2) {
		// TODO Auto-generated method stub
		
	}  
}
