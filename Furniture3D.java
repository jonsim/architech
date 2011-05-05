import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

	public class Furniture3D
	{
		Spatial          spatial;
		RigidBodyControl physics = null;
		private PointLight light = null;
		private boolean lightToggleable = true;  // whether or not this light toggles (i.e. if it is on all the time (false) or only at night (true))
		private boolean lightState = false; // true = on, false = off
		private ColorRGBA lightColor;
		private boolean isP;
		
		
		
		/** Sets the Furniture3D model to that stored at the path provided. */
		Furniture3D (Spatial spatial, boolean isP)
		{
			if (spatial == null)
				throw new IllegalArgumentException("null");
			this.spatial = spatial;
			this.isP = isP;
		}
		
		
		
		/** Returns the height of the spatial object. */
		public float getHeight ()
		{
			float centre = spatial.getWorldBound().getCenter().y;
			float base = -100;
			return (centre - base) * 2;
		}
		
		
		
		/** Returns true if the object is physical, false if it is not. */
		public boolean isPhysical ()
		{
			return isP;
		}
		
		
		
		/** Sets whether the light attached to this furniture object can be toggled (i.e. if it is on all the 
		 *  time (false) or only at night (true)). This method does NOT check whether the furniture has an 
		 *  attached light. */
		public void setLightToggleable (boolean t)
		{
			lightToggleable = t;
		}

		
		
		/** Adds a point light to the current furniture with colour defined by the integer array values. This 
		 *  should contain an RGB colour in that order (with values between 0 and 255). Adds a light of this 
		 *  colour and of 250px radius. This should be the default if you do not know the radius to use.  */
		public void addLight (int[] v)
		{
			if (v.length != 4)
				throw new IllegalArgumentException("addLight called with a non-4-element-array.");
			addLight(v[0], v[1], v[2], 250, v[3]);
		}
		
		
		
		/** Adds a point light to the current furniture with colour RGB (with values between 0 and 255) and an 
		 *  (optional) intensity I.  */
		public void addLight (int R, int G, int B, int I, int lightMode)
		{
			if (R < 0 || R > 255 || G < 0 || G > 255 || B < 0 || B > 255)
				throw new IllegalArgumentException("addLight called with a non-RGB value.");
			if (I < 0)
				throw new IllegalArgumentException("addLight called with a negative intensity.");
			if (lightMode < 0 || lightMode > 1)
				throw new IllegalArgumentException("addLight called with an illegal lightMode.");
			if (light != null)
			{
				System.err.println("[WARNING @Furniture3D] [SEVERITY: Medium] addLight called, but there is already a light.");
				return;
			}

			light = new PointLight();
			lightColor = new ColorRGBA((float) R/255, (float) G/255, (float) B/255, 1);
			light.setColor(lightColor);
			light.setRadius(I);
			Vector3f centre = spatial.getLocalTranslation();
			centre = centre.add(0, getHeight(), 0);
			light.setPosition(centre);
			lightState = true;
			if (lightMode == 1)
				setLightToggleable(false);
		}
		
		
		
		/** Updates the attached light's position to the centre of the spatial. */
		public void updateLight ()
		{
			if (light == null)
			{
				System.err.println("[WARNING @Furniture3D] [SEVERITY: Low] updateLight called, but there is no light.");
				return;
			}
			if (lightState)
			{
				Vector3f centre = spatial.getLocalTranslation();
				centre = centre.add(0, getHeight(), 0);
				light.setPosition(centre);
			}
		}
		
		
		
		/** Turns on the object's attached light (if it exists). */
		public void turnOnLight ()
		{
			if (light == null)
			{
				System.err.println("[WARNING @Furniture3D] [SEVERITY: Low] turnOnLight called on an object which has no light.");
				return;				
			}
			if (lightToggleable)
			{
				Vector3f centre = spatial.getLocalTranslation();
				centre = centre.add(0, getHeight(), 0);
				light.setPosition(centre);
				light.setColor(lightColor);
				lightState = true;
			}
		}

		
		
		/** Turns off the object's attached light (if it exists). */
		public void turnOffLight ()
		{
			if (light == null)
			{
				System.err.println("[WARNING @Furniture3D] [SEVERITY: Low] turnOffLight called on an object which has no light.");
				return;				
			}
			if (lightToggleable)
			{
				light.setColor(ColorRGBA.Black);
				lightState = false;
			}
		}
		
		
		
		/** Returns true is the object has a light and it is turned on. Returns false otherwise. A return value of 
		 *  false does NOT imply the object doesn't have a light; use hasLight() for this. */
		public boolean getLightState ()
		{
			return lightState;
		}

		
		
		/** Returns true is the object has an attached light, or false if it does not. */
		public boolean hasLight ()
		{
			if (light == null)
				return false;
			return true;
		}
		
		
		
		/** Returns the light object attached to the furniture. Will throw an exception if called when there is 
		 *  no light. Use hasLight() to determine if the furniture owns a light. */
		public PointLight getLight ()
		{
			if (light == null)
				throw new IllegalArgumentException("Object has no light.");
			return light;
		}
	}