package de.tum.renderable;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;


/**
 * a game object is a geometric object, that represents a typical object of the
 * game like a peg or the board and consists of one or more simple geometric
 * objects
 */
public abstract class GameObject extends GeometricObject {
	/** p is a factor for scaling a game object */
	protected static final float p = 0.16f;

	/** the final bottom layer has his z-coordinate at 0x00 */
	protected static final float layer_z = 0x00;

	/** array for the simple geometric objects */
	protected ArrayList<SimpleGeometricObject> sgobjects;

	/** the absolute location of this object */
	protected float x;
	protected float y;
	protected float z;

	/**
	 * specify whether this object will be rendered
	 * 
	 * @param visible
	 *            true if visible
	 */
	public GameObject(boolean visible) {
		super(visible);
		sgobjects = new ArrayList<SimpleGeometricObject>();
	}

	/** {@inheritDoc} */
	public final void transfer(float dx, float dy, float dz) {
		if (sgobjects != null)
			for (SimpleGeometricObject object : sgobjects)
				object.transfer(dx, dy, dz);
		x += dx;
		y += dy;
		z += dz;
	}
	
	/** calculates moves and updates properties for this game object */
	protected abstract void action();

	/** {@inheritDoc}
	 * Calls the action method */
	public final void render(GL10 gl) {
		action();
		if (visible && sgobjects != null)
			for (SimpleGeometricObject object : sgobjects)
				object.render(gl);
	}
}
