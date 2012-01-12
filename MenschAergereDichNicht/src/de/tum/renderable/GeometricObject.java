package de.tum.renderable;

/**
 * a geometric object is a renderable, that can might be visible and can be
 * transferred
 */
public abstract class GeometricObject implements Renderable {
	/** specifies whether the object is visible or not */
	protected boolean visible;

	/**
	 * specify whether this object will be rendered
	 * 
	 * @param visible
	 *            true if visible
	 */
	public GeometricObject(boolean visible) {
		this.visible = visible;
	}

	/**
	 * method for checking the visibility
	 * 
	 * @return true if visible
	 */
	public final boolean isVisible() {
		return visible;
	}

	/**
	 * method for setting the visibility
	 * 
	 * @param visible
	 *            true if visible
	 */
	public final void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * method for transferring the vectors by adding the given offset to each
	 * coordinate
	 * 
	 * @param dx
	 *            the offset in x-direction
	 * @param dy
	 *            the offset in y-direction
	 * @param dz
	 *            the offset in z-direction
	 */
	public abstract void transfer(float dx, float dy, float dz);
}
