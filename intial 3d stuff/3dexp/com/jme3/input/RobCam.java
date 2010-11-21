/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme3.input;

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

public class RobCam implements AnalogListener, ActionListener {

    protected Camera cam;
    protected Vector3f initialUpVec;
    protected float rotationSpeed = 1f;
    protected float moveSpeed = 3f;
    protected boolean enabled = true;
    protected boolean dragToRotate = false;
    protected boolean canRotate = false;
    protected InputManager inputManager;
    
    /**
     * Creates a new RobCam to control the given Camera object.
     * @param cam
     */
    public RobCam(Camera cam){
        this.cam = cam;
        initialUpVec = cam.getUp().clone();
    }

    /**
     * Sets the up vector that should be used for the camera.
     * @param upVec
     */
    public void setUpVector(Vector3f upVec) {
       initialUpVec.set(upVec);
    }

    /**
     * Sets the move speed. The speed is given in world units per second.
     * @param moveSpeed
     */
    public void setMoveSpeed(float moveSpeed){
        this.moveSpeed = moveSpeed;
    }

    /**
     * Sets the rotation speed.
     * @param rotationSpeed
     */
    public void setRotationSpeed(float rotationSpeed){
        this.rotationSpeed = rotationSpeed;
    }

    /**
     * @param enable If false, the camera will ignore input.
     */
    public void setEnabled(boolean enable){
        if (enabled && !enable){
            if (!dragToRotate || (dragToRotate && canRotate)){
                inputManager.setCursorVisible(true);
            }
        }
        enabled = enable;
    }

    /**
     * @return If enabled
     * @see RobCam#setEnabled(boolean)
     */
    public boolean isEnabled(){
        return enabled;
    }

    /**
     * @return If drag to rotate feature is enabled.
     *
     * @see RobCam#setDragToRotate(boolean) 
     */
    public boolean isDragToRotate() {
        return dragToRotate;
    }

    /**
     * @param dragToRotate When true, the user must hold the mouse button
     * and drag over the screen to rotate the camera, and the cursor is
     * visible until dragged. Otherwise, the cursor is invisible at all times
     * and holding the mouse button is not needed to rotate the camera.
     * This feature is disabled by default.
     */
    public void setDragToRotate(boolean dragToRotate) {
        this.dragToRotate = dragToRotate;
        inputManager.setCursorVisible(dragToRotate);
    }

    /**
     * Registers the RobCam to receive input events from the provided
     * Dispatcher.
     * @param dispacher
     */
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

        // both mouse and button - rotation of cam
        inputManager.addMapping("VARCAM_Left", new MouseAxisTrigger(0, true),
                                               new KeyTrigger(KeyInput.KEY_LEFT));

        inputManager.addMapping("VARCAM_Right", new MouseAxisTrigger(0, false),
                                                new KeyTrigger(KeyInput.KEY_RIGHT));

        inputManager.addMapping("VARCAM_Up", new MouseAxisTrigger(1, false),
                                             new KeyTrigger(KeyInput.KEY_UP));

        inputManager.addMapping("VARCAM_Down", new MouseAxisTrigger(1, true),
                                               new KeyTrigger(KeyInput.KEY_DOWN));

        // mouse only - zoom in/out with wheel, and rotate drag
        inputManager.addMapping("VARCAM_ZoomIn", new MouseAxisTrigger(2, false));
        inputManager.addMapping("VARCAM_ZoomOut", new MouseAxisTrigger(2, true));
        inputManager.addMapping("VARCAM_RotateDrag", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        // keyboard only WASD for movement and WZ for rise/lower height
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
//                value = -value;
            }else{
                return;
            }
        }

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
        // derive fovY value
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
        Vector3f vel = new Vector3f(0, value * moveSpeed, 0);
        Vector3f pos = cam.getLocation().clone();
        
        if(pos.y>-97)
        {
        	pos.addLocal(vel);
        } else
        {
        	if(vel.y > 0) pos.addLocal(vel); 
        }
        cam.setLocation(pos);
    }
    
    protected void xCamera(float value){
        Vector3f vel = new Vector3f(); 
        Vector3f mov = null,des = null;
        //(value * moveSpeed,0,0);
        Vector3f pos = cam.getLocation().clone();
        cam.getDirection(vel);
        
        if(value>0){
        	des = new Vector3f(vel.x,0,vel.z); }
        else{
            des = new Vector3f(-vel.x,0,-vel.z); }
        
        if(pos.x<195){
        	mov = new Vector3f(des.x,0,0); 
        	pos.addLocal(mov);
        } else {
        	if(des.x<=0) {mov = new Vector3f(des.x,0,0); 
        	pos.addLocal(mov);}
        	else {}
        	}
        
        System.out.println(pos.z);
        if(pos.z>5){
        	mov = new Vector3f(0,0,des.z); 
        	pos.addLocal(mov);
        } else {
            System.out.println(des.z);
        	if(des.z>=0) {mov = new Vector3f(0,0,des.z); 
        	pos.addLocal(mov);}
        	else {}
        }
                
        cam.setLocation(pos);
    }

    protected void sideCamera(float value, boolean sideways){
        Vector3f vel = new Vector3f();
        Vector3f pos = cam.getLocation().clone(),des,mov=null;

        if (sideways){
            cam.getLeft(vel);
        }else{
            cam.getDirection(vel);
        }
        vel.multLocal(value * moveSpeed * 2);
        
        if(value>0){
        	des = new Vector3f(vel.x,0,vel.z); }
        else{
            des = new Vector3f(vel.x,0,vel.z); }
        
        if(pos.x<195){
        	mov = new Vector3f(des.x,0,0); 
        	pos.addLocal(mov);
        } else {
        	if(des.x<=0) {mov = new Vector3f(des.x,0,0); 
        	pos.addLocal(mov);}
        	else {}
        	}
        
        System.out.println(pos.z);
        if(pos.z>5){
        	mov = new Vector3f(0,0,des.z); 
        	pos.addLocal(mov);
        } else {
            System.out.println(des.z);
        	if(des.z>=0) {mov = new Vector3f(0,0,des.z); 
        	pos.addLocal(mov);}
        	else {}
        }

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
