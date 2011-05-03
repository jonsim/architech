import com.jme3.input.InputManager;
import com.jme3.input.JoyInput;
import com.jme3.input.Joystick;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class MovCam implements AnalogListener, ActionListener {

    protected Camera cam;
    protected Vector3f initialUpVec;
    protected float rotationSpeed = 5f;
    protected float moveSpeed = 4f;
    protected boolean enabled = true;
    protected boolean dragToRotate = false;
    protected boolean canRotate = false;
    protected InputManager inputManager;
    
/*This code is mostly from the default camera included with JME, FlyCam.
 * It has been modified so that there are movement constraints
 * and also so that the movement is more natural (wasd, travel where the mouse is looking)
 */
    
    public MovCam(Camera cam){
        this.cam = cam;
        initialUpVec = cam.getUp().clone();
    }

    public void setUpVector(Vector3f upVec) {
       initialUpVec.set(upVec);
    }

    public void setMoveSpeed(float moveSpeed){
        this.moveSpeed = moveSpeed;
    }

    public void setRotationSpeed(float rotationSpeed){
        this.rotationSpeed = rotationSpeed;
    }

    public void setEnabled(boolean enable){
        if (enabled && !enable){
            if (!dragToRotate || (dragToRotate && canRotate)){
                inputManager.setCursorVisible(true);
            }
        }
        enabled = enable;
    }

    public boolean isEnabled(){
        return enabled;
    }
    
    public boolean isDragToRotate() {
        return dragToRotate;
    }

    public void setDragToRotate(boolean dragToRotate) {
        this.dragToRotate = dragToRotate;
        inputManager.setCursorVisible(dragToRotate);
    }

    public void registerWithInput(InputManager inputManager){
        this.inputManager = inputManager;
        String[] mappings = new String[]{
            "VARCAM_Left",
            "VARCAM_Right",
            "VARCAM_Up",
            "VARCAM_Down",
            "VARCAM_StrafeLeft",
            "VARCAM_StrafeRight",
            "VARCAM_Forward",
            "VARCAM_Backward",
            "VARCAM_ZoomIn",
            "VARCAM_ZoomOut",
            "VARCAM_RotateDrag",
            "VARCAM_Rise",
            "VARCAM_Lower"
        };
        // both mouse and button
        inputManager.addMapping("VARCAM_Left", new MouseAxisTrigger(0, true),
                                               new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("VARCAM_Right", new MouseAxisTrigger(0, false),
                                                new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("VARCAM_Up", new MouseAxisTrigger(1, false),
                                             new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("VARCAM_Down", new MouseAxisTrigger(1, true),
                                                new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("VARCAM_ZoomIn", new MouseAxisTrigger(2, false));
        inputManager.addMapping("VARCAM_ZoomOut", new MouseAxisTrigger(2, true));
        inputManager.addMapping("VARCAM_RotateDrag", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("VARCAM_StrafeLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("VARCAM_StrafeRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("VARCAM_Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("VARCAM_Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("VARCAM_Rise", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("VARCAM_Lower", new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addListener(this, mappings);
        inputManager.setCursorVisible(dragToRotate);
        Joystick[] joysticks = inputManager.getJoysticks();
        if (joysticks != null && joysticks.length > 0){
            Joystick joystick = joysticks[0];
            joystick.assignAxis("VARCAM_StrafeRight", "VARCAM_StrafeLeft", JoyInput.AXIS_POV_X);
            joystick.assignAxis("VARCAM_Forward", "VARCAM_Backward", JoyInput.AXIS_POV_Y);
            joystick.assignAxis("VARCAM_Right", "VARCAM_Left", joystick.getXAxisIndex());
            joystick.assignAxis("VARCAM_Down", "VARCAM_Up", joystick.getYAxisIndex());
        }
    }

    protected void rotateCamera(float value, Vector3f axis){
        if (dragToRotate){
            if (canRotate){
            }else{return;}        }
        Matrix3f mat = new Matrix3f();
        mat.fromAngleNormalAxis(rotationSpeed * value, axis);
        Vector3f up = cam.getUp();
        Vector3f left = cam.getLeft();
        Vector3f dir = cam.getDirection();
        mat.mult(up, up);
        mat.mult(left, left);
        mat.mult(dir, dir);
        Quaternion q = new Quaternion();
        q.fromAxes(left, up, dir);
        q.normalize();
        cam.setAxes(q);
    }

    protected void zoomCamera(float value){
    	value = value * 10;
        float h = cam.getFrustumTop();
        float w = cam.getFrustumRight();
        float aspect = w / h;
        float near = cam.getFrustumNear();
        float fovY = FastMath.atan(h / near)
                  / (FastMath.DEG_TO_RAD * .5f);
        fovY += value * 0.1f;
        h = FastMath.tan( fovY * FastMath.DEG_TO_RAD * .5f) * near;
        w = h * aspect;
        cam.setFrustumTop(h);
        cam.setFrustumBottom(-h);
        cam.setFrustumLeft(-w);
        cam.setFrustumRight(w);
    }

    protected void riseCamera(float value){
        Vector3f vel = new Vector3f(0, value * moveSpeed * 2, 0);
        Vector3f pos = cam.getLocation().clone();
        /*//Injected Code to get movement bounds
        if(pos.y>-97 && pos.y<97)
        {
        	pos.addLocal(vel);
        } else
        {
        	if(vel.y > 0 && pos.y<0) pos.addLocal(vel); 
        	if(vel.y < 0 && pos.y>0) pos.addLocal(vel); 
        }
        //end*/
        vel = new Vector3f(0,vel.y,0);
        pos.addLocal(vel);
        cam.setLocation(pos);       
    }
    
    protected void xCamera(float value){
        Vector3f vel = new Vector3f(); 
        //Vector3f mov = null,des = null;
        Vector3f pos = cam.getLocation().clone();
        cam.getDirection(vel);
        if(value>=0) {vel = new Vector3f(vel.x ,0,vel.z);}
        else {vel = new Vector3f(-vel.x,0,-vel.z);}
        //Injected Code to get movement bounds
        /*if(value>0){
        	des = new Vector3f(vel.x,0,vel.z); }
        else{
            des = new Vector3f(-vel.x,0,-vel.z); }
        
        if(pos.x<195 && pos.x>-340){
        	mov = new Vector3f(des.x,0,0); 
        	pos.addLocal(mov);
        } else {
        	if(des.x<=0 && pos.x>195) {mov = new Vector3f(des.x,0,0);pos.addLocal(mov);}
        	if(des.x>=0 && pos.x<-340) {mov = new Vector3f(des.x,0,0);pos.addLocal(mov);}
        	else {}
        	}
        
        if(pos.z>5 && pos.z<387){
        	mov = new Vector3f(0,0,des.z); 
        	pos.addLocal(mov);
        } else {
        	if(des.z>=0 && pos.z<387) {mov = new Vector3f(0,0,des.z);pos.addLocal(mov);}
        	if(des.z<=0 && pos.z>387) {mov = new Vector3f(0,0,des.z);pos.addLocal(mov);}        	
        }
        //end*/
        pos.addLocal(vel);
        cam.setLocation(pos);
        //System.out.println(pos.x+ " "+pos.y+ " "+pos.z);
    }

    protected void sideCamera(float value, boolean sideways){
        Vector3f vel = new Vector3f();
        Vector3f pos = cam.getLocation().clone();
        //Vector3f des,mov=null;

        if (sideways){
            cam.getLeft(vel);
        }else{
            cam.getDirection(vel);
        }
        vel.multLocal(value * moveSpeed * 2);
        
        //Injected Code to get movement bounds
        /*
        if(value>0){
        	des = new Vector3f(vel.x,0,vel.z); }
        else{
            des = new Vector3f(vel.x,0,vel.z); }
               
        if(pos.x<195 && pos.x>-340){
        	mov = new Vector3f(des.x,0,0); 
        	pos.addLocal(mov);
        } else {
        	if(des.x<=0 && pos.x>195) {mov = new Vector3f(des.x,0,0);pos.addLocal(mov);}
        	if(des.x>=0 && pos.x<-340) {mov = new Vector3f(des.x,0,0);pos.addLocal(mov);}
        	else {}
        	}      
       
        if(pos.z>5 && pos.z<387){
        	mov = new Vector3f(0,0,des.z); 
        	pos.addLocal(mov);
        } else {
        	if(des.z>=0 && pos.z<387) {mov = new Vector3f(0,0,des.z);pos.addLocal(mov);}
        	if(des.z<=0 && pos.z>387) {mov = new Vector3f(0,0,des.z);pos.addLocal(mov);}        	
        }
        */
        //end
        vel = new Vector3f(vel.x,0,vel.z);
        pos.addLocal(vel);
        cam.setLocation(pos);
    }

    public void onAnalog(String name, float value, float tpf) {
        if (!enabled)
            return;

        if (name.equals("VARCAM_Left")){
            rotateCamera(value, initialUpVec);
        }else if (name.equals("VARCAM_Right")){
            rotateCamera(-value, initialUpVec);
        }else if (name.equals("VARCAM_Up")){
            rotateCamera(-value, cam.getLeft());
        }else if (name.equals("VARCAM_Down")){
            rotateCamera(value, cam.getLeft());
        }else if (name.equals("VARCAM_Forward")){
            xCamera(value);
        }else if (name.equals("VARCAM_Backward")){
            xCamera(-value);
        }else if (name.equals("VARCAM_StrafeLeft")){
            sideCamera(value, true);
        }else if (name.equals("VARCAM_StrafeRight")){
            sideCamera(-value, true);
        }else if (name.equals("VARCAM_Rise")){
            riseCamera(value);
        }else if (name.equals("VARCAM_Lower")){
            riseCamera(-value);
        }else if (name.equals("VARCAM_ZoomIn")){
            zoomCamera(value);
        }else if (name.equals("VARCAM_ZoomOut")){
            zoomCamera(-value);
        }
    }

    public void onAction(String name, boolean value, float tpf) {
        if (!enabled)
            return;
        if (name.equals("VARCAM_RotateDrag") && dragToRotate){
            canRotate = value;
            inputManager.setCursorVisible(!value);
        }
    }

}
