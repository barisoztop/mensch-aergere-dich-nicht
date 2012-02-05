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
	public static final float p = 0.08f;

	/** the amount of frames a peg needs to reach the next field */
	public static int frames = 20;
	/** the final bottom layer has his z-coordinate at 0x00 */
	protected static final float layer_z = 0x00;

	/** array for the simple geometric objects */
	protected ArrayList<SimpleGeometricObject> sgobjects;

	/** the absolute location of this object */
	protected float x;
	protected float y;
	protected float z;

	/** true if GL is changed before rendering */
	protected boolean rotated;

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
		x += dx;
		y += dy;
		z += dz;
	}
	
	/**
	 * setting color for all parts of this game object
	 * 
	 * @param color
	 *            the new colors
	 */
	protected final void setColor(float color[]) {
		for (SimpleGeometricObject object : sgobjects)
			object.setColor(color, null);
	}
	
	/** calculates moves and updates properties for this game object */
	protected abstract void action();

	/**
	 * rotates the matrix for this game object. Called if rotated = true
	 * 
	 * @param gl
	 *            the Gl10 to rotate11
	 */
	protected abstract void rotate(GL10 gl);

	/** {@inheritDoc}
	 * Calls the action method */
	public final void render(GL10 gl) {
		action();
		if (!visible || sgobjects == null)
			return;
		// push matrix to save it on the stack
		gl.glPushMatrix();
		// setting game object position
		gl.glTranslatef(x, y, z);
		if (rotated) // rotate object
			rotate(gl);
		// render game object
		for (SimpleGeometricObject object : sgobjects)
			object.render(gl);
		gl.glPopMatrix();
	}
}
