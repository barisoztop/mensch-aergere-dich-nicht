package de.tum;

import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

import de.tum.renderable.Renderable;

/**
 * a room manages all renderables of the game in one list and prevents other
 * threads from interacting during a rendering
 */
public class Room {
	/** list of renderable objects */
	private static final List<Renderable> renderables = new ArrayList<Renderable>();

	/**
	 * here you can add a renderable object
	 * 
	 * @param renderable
	 *            any object that implements a renderable
	 */
	public static synchronized void addRenderable(Renderable renderable) {
		renderables.add(renderable);
	}

	/**
	 * here you render the room with all its content to the GL10
	 * 
	 * @param gl
	 *            the GL10 for rendering
	 */
	public static synchronized void render(GL10 gl) {
		for (Renderable renderable : renderables)
			renderable.render(gl);
	}

	/** here you can clear the room. All objects will be deleted */
	public static synchronized void clear() {
		renderables.clear();
	}

	/**
	 * here you can verify whether the room is empty
	 * 
	 * @return returns true if the room is empty
	 */
	public static synchronized boolean isEmpty() {
		return renderables.isEmpty();
	}
}
