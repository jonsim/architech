import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

	public class Furniture3D
	{
		Spatial          spatial;
		RigidBodyControl physics = null;
		Geometry lightMarker = null;
		private PointLight light = null;
		private boolean    isP;
		
		/** Sets the Furniture3D model to that stored at the path provided. */
		Furniture3D (Spatial spatial, boolean isP)
		{
			if (spatial == null)
				throw new IllegalArgumentException("null");
			this.spatial = spatial;
			this.isP = isP;
		}
		
		public float getHeight ()
		{
			float centre = spatial.getWorldBound().getCenter().y;
			float base = -100;
			System.out.println("getHeight() = " + (centre-base)*2);
			return (centre - base) * 2;
		}
		
		public boolean isPhysical ()
		{
			return isP;
		}

		/** Adds a point light to the current furniture with colour RGB (with values between 0 and 255) and 100% 
		 *  intensity. This should be the default if you do not know the intensity to use.  */
		public void addLight (int R, int G, int B)
		{
			addLight(R, G, B, 250);
		}
		
		/** Adds a point light to the current furniture with colour RGB (with values between 0 and 255) and an 
		 *  (optional) intensity I.  */
		public void addLight (int R, int G, int B, int I)
		{
			if (R < 0 || R > 255 || G < 0 || G > 255 || B < 0 || B > 255)
				throw new IllegalArgumentException("addLight called with a non-RGB value.");
			if (I < 0)
				throw new IllegalArgumentException("addLight called with a negative intensity.");
			if (light != null)
			{
				System.err.println("[WARNING @ArchApp] [SEVERITY: Medium] addLight called, but there is already a light.");
				return;
			}

			light = new PointLight();
			light.setColor(new ColorRGBA((float) R/255, (float) G/255, (float) B/255, 1));
			//light.setColor(new ColorRGBA(1, 0, 0, 1));
			light.setRadius(I);
			Vector3f centre = spatial.getLocalTranslation();
			centre = centre.add(0, getHeight(), 0);
			light.setPosition(centre);
/*
			lightMarker = new Geometry("lightmarker", new Box(Vector3f.ZERO, 1, 1, 1));
	        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	        mat.setColor("Color", ColorRGBA.Blue);
	        lightMarker.setMaterial(wallmat);
			Vector3f centre = spatial.getLocalTranslation();
			//centre = centre.add(0, 60, 0);
			//lightMarker.setMaterial(wallmat);
			lightMarker.setLocalTranslation(centre);
			rootNode.attachChild(lightMarker);
			System.out.printf("adding marker at (%d,%d,%d)\n", (int) centre.getX(), (int) centre.getY(), (int) centre.getZ());*/
			
		}
		
		/** Updates the attached light's position to the centre of the spatial. */
		public void updateLight ()
		{
			if (light == null)
			{
				System.err.println("[WARNING @ArchApp] [SEVERITY: Low] updateLight called, but there is no light.");
				return;
			}
			//light.setPosition(spatial.getLocalTranslation());
			lightMarker.setLocalTranslation(spatial.getLocalTranslation());
		}
		
		/** Turns on the object's attached light (if it exists). */
		public void turnOnLight ()
		{
			if (light == null)
			{
				System.err.println("[WARNING @ArchApp] [SEVERITY: Low] turnOnLight called on an object which has no light.");
				return;				
			}
			
			light.getColor().a = 1;
		}

		/** Turns off the object's attached light (if it exists). */
		public void turnOffLight ()
		{
			if (light == null)
			{
				System.err.println("[WARNING @ArchApp] [SEVERITY: Low] turnOffLight called on an object which has no light.");
				return;				
			}

			light.getColor().a = 0;
		}

		/** Returns true is the object has an attached light, or false if it does not. */
		boolean hasLight ()
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