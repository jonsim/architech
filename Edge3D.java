import java.util.ArrayList;
import java.util.Iterator;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

public class Edge3D
{
	/** A single segment making up a wall. A wall with have 1 or more of these. */
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
	    
	    public void flipSegment ()
	    {
	    	Quaternion side0_rotation = side[0].getLocalRotation();
	    	Quaternion side1_rotation = side[1].getLocalRotation();
	    	Vector3f side0_translation = side[0].getLocalTranslation();
	    	Vector3f side1_translation = side[1].getLocalTranslation();
	    	side[0].setLocalRotation(side1_rotation);
	    	side[1].setLocalRotation(side0_rotation);
	    	side[0].setLocalTranslation(side1_translation);
	    	side[1].setLocalTranslation(side0_translation);
	    }
	}

	
	
	ArrayList<Segment> segments;
	ArrayList<Geometry> attachedFurniture;
	private String[] texture = new String[2];
	private static final boolean tracing = false;
	private Edge edge2D;
	private int ID;
	
	
	
	/** Creates a 3D edge, setting its 'parent' Edge e (the 2D edge this 3D 
	 *  edge represents). Throws an exception if no parent is given. */
	Edge3D (Edge parent, int ID)
	{
		if (tracing)
			System.out.println("new Edge3D() called.");
		this.ID = ID;
		this.edge2D = parent;
		segments = new ArrayList<Segment>();
		attachedFurniture = new ArrayList<Geometry>();
		texture[0] = "img/wallpapers/default.jpg";
		texture[1] = "img/wallpapers/default.jpg";
	}
	
	
	
	/**  */
	public void flipEdge ()
	{
		Iterator<Segment> segment_itr = segments.iterator();
		while (segment_itr.hasNext())
			segment_itr.next().flipSegment();
	}
	
	
	
	public Edge get2DEdge ()
	{
		return edge2D;
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
	
	
	
	public int getID ()
	{
		return ID;
	}
	
	
	
	public String getIDString ()
	{
		return ID + "";
	}
}