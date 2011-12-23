package de.tum;

/**
 * TupleFloat is just a pair of floats for representing a 2D-coordinate
 */
public class TupleFloat {
	/** the x-coordinate */
	public final float x;
	/** the y-coordinate */
	public final float y;

	/**
	 * this object has to be initialized with two floats that cannot be changed
	 * later
	 * 
	 * @param x
	 *            the x-coordinate
	 * @param y
	 *            the y-coordinate
	 */
	public TupleFloat(float x, float y) {
		this.x = x;
		this.y = y;
	}
}
