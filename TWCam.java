import com.jme3.input.InputManager;
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

public class TWCam implements AnalogListener, ActionListener {

    protected Camera cam;
    protected Vector3f initialUpVec;
    protected float rotationSpeed = 5f;
    protected float moveSpeed = 1f;
    protected boolean enabled = true;
    protected boolean dragToRotate = false;
    protected boolean canRotate = false;
    protected InputManager inputManager;
    protected boolean mousein = false;
    
/*This code is mostly from the default camera included with JME, FlyCam.
 * It has been modified so that there are movement constraints
 * and also so that the movement is more natural (wasd, travel where the mouse is looking)
 */
    
    public TWCam(Camera cam){
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
            "VARCAM_StrafeLeft",
            "VARCAM_StrafeRight",
            "VARCAM_ZoomIn",
            "VARCAM_ZoomOut",
            "VARCAM_RotateDrag"
        };
        // both mouse and button

        inputManager.addMapping("VARCAM_RotateDrag", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("VARCAM_StrafeLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("VARCAM_StrafeRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("VARCAM_ZoomIn", new MouseAxisTrigger(2, false));
        inputManager.addMapping("VARCAM_ZoomOut", new MouseAxisTrigger(2, true));

        inputManager.addListener(this, mappings);
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
        vel = new Vector3f(vel.x,0,vel.z);
        pos.addLocal(vel);
        cam.setLocation(pos);
    }

    public void onAnalog(String name, float value, float tpf) {
        if (!enabled)
            return;
        //Vector3f loc = cam.getLocation();

        if (name.equals("VARCAM_StrafeLeft") && mousein){
            rotateCamera(-value, initialUpVec);
            sideCamera(value*3, true);
        }else if (name.equals("VARCAM_StrafeRight") && mousein){
        	rotateCamera(value, initialUpVec);
            sideCamera(-value*3, true);
	    }else if (name.equals("VARCAM_ZoomIn")){
	        zoomCamera(value);
	    }else if (name.equals("VARCAM_ZoomOut")){
	        zoomCamera(-value);
	    }
        else{}
        
    }

    public void onAction(String name, boolean value, float tpf) {
        if (!enabled)
            return;
        if (name.equals("VARCAM_RotateDrag") && dragToRotate){
            canRotate = value;
            mousein = !mousein;
            inputManager.setCursorVisible(!value);
        }
    }

}
