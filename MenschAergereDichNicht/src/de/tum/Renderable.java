package de.tum;

import javax.microedition.khronos.opengles.GL10;

/** a renderable is an object, that can be rendered with the render-method */
public interface Renderable {
	/**
	 * method for rendering the game object
	 * 
	 * @param gl
	 *            the GL10 object for rendering the game object
	 */
	public void render(GL10 gl);
}
