import java.util.ArrayList;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Geometry;

public class Edge3D
{
	public static class Segment
	{
		public static enum Position {TOP, BOTTOM, FULL}

		private Position type;
		public Geometry[] side = new Geometry[2];
		public RigidBodyControl physics = null;
		
		Segment ()
		{
			type = Position.FULL;
		}
		
		Segment (Position type)
		{
			this.type = type;
		}
		
		/** Returns the position, or type, of the segment. This can be either TOP (for a top 
	     *  panel), BOTTOM (for a bottom panel) or FULL (for a full wall panel). */
	    public Position getType ()
	    {
	    	return type;
	    }
	}

	ArrayList<Segment> segments;
	ArrayList<Geometry> attachedFurniture;
	private String[] texture = new String[2];
	private static final boolean tracing = false;
	
	
	
	/** Creates a 3D edge, setting its 'parent' Edge e (the 2D edge this 3D 
	 *  edge represents). Throws an exception if no parent is given. */
	Edge3D ()
	{
		if (tracing)
			System.out.println("new Edge3D() called.");
		segments = new ArrayList<Segment>();
		attachedFurniture = new ArrayList<Geometry>();
		texture[0] = "img/wallpapers/default.jpg";
		texture[1] = "img/wallpapers/default.jpg";
	}
	
	

	/** Returns the texture applied to the given side (0 or 1) of the segment. */
	public String getTexture (int side)
	{
		if (tracing)
			System.out.printf("getTexture(%d) called.\n", side);
		if (side != 0 & side != 1)
			throw new IllegalArgumentException("side is not 0 or 1");
		return texture[side];
	}
	
	

	/** Sets the texture applied to the given side (0 or 1) of the segment. */
	public void setTexture (int side, String path)
	{
		if (tracing)
			System.out.printf("setTexture(%d, %s) called.\n", side, path);
		if (side != 0 & side != 1)
			throw new IllegalArgumentException("side is not 0 or 1");
		if (path == null)
			throw new IllegalArgumentException("null");
		texture[side] = path;
	}
}