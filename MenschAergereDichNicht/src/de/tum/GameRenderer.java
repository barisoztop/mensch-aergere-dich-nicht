package de.tum;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.tum.renderable.Textures;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

/**
 * with the game renderer the game is rendered, meaning that the individual
 * frames are drawn. Therefore the method onDrawFrame is called for each frame
 */
public class GameRenderer implements Renderer {
	/** the room containing all renderable objects */
	private Room room;

	// just for testing
	// ############################### needs
	// some change
	private static final float r = 2;
	private float degree;
	Context context;
	
	/**
	 * creating the game renderer. Usual only one renderer is created
	 * 
	 * @param room
	 *            the room containing the renderable objects
	 */
	public GameRenderer(Room room, Context context) {
		this.room = room;this.context = context;
	}

	/**
	 * called when the surface is created. Currently not used
	 * 
	 * @param gl
	 *            the GL10 for rendering
	 * @param config
	 *            the configuration for the GL10
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Textures.bindTextures(gl);
	}

	/**
	 * rendering the current frame
	 * 
	 * @param gl
	 *            the GL10 for rendering
	 */
	public void onDrawFrame(GL10 gl) {
		// just for testing
		// ############################### needs
		// some change
		degree += 0.01;
		gl.glClearColor(0, 0, 0, 0);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		GLU.gluPerspective(gl, 67, (float) MenschAergereDichNichtActivity.width
				/ MenschAergereDichNichtActivity.height, 0.01f, 20);
		GLU.gluLookAt(gl, (float) (r * Math.sin(degree)),
				(float) (r * Math.cos(degree)),
				MenschAergereDichNichtActivity.hz, 0, 0,
				MenschAergereDichNichtActivity.hz / 2, 0, 0, 1);
		room.render(gl);
	}

	/**
	 * called if the surface changed. This happens e.g. when a player rotates
	 * the phone
	 * 
	 * @param gl
	 *            the GL10 for rendering
	 * @param width
	 *            the width of the surface
	 * @param height
	 *            the height of the surface
	 */
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// testing -// ############################### needs
		// some change
		MenschAergereDichNichtActivity.width = width;
		MenschAergereDichNichtActivity.height = height;
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		GLU.gluPerspective(gl, 67, (float) MenschAergereDichNichtActivity.width
				/ MenschAergereDichNichtActivity.height, 0.01f, 20);
		GLU.gluLookAt(gl, r, r, r, 0, 0, 0, 0, 0, 1);
		gl.glDisable(GL10.GL_DITHER);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_COLOR_MATERIAL);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
}
