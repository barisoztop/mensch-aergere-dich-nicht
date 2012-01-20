package de.tum;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.tum.renderable.Textures;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;

/**
 * with the game renderer the game is rendered, meaning that the individual
 * frames are drawn. Therefore the method onDrawFrame is called for each frame
 */
public class GameRenderer implements Renderer {
	/** the room containing all renderable objects */
	private static final float degree_90 = (float) Math.PI / 2 - 0.001f;
	private static final float distance_min = 0.001f;
	private static final float distance_max = 70;
	private static float width;
	private static float height;
	private static float radius;
	private static float degree_horizontal;
	private static float degree_vertical;
	private static float center_x;
	private static float center_y;
	private static float center_z;
	private static float up_z;
	
	/**
	 * creating the game renderer. Usual only one renderer is created
	 * 
	 * @param room
	 *            the room containing the renderable objects
	 */
	public GameRenderer() {
		radius = 2;
		tranfer(0, degree_90 * 70);
	}
	
	public static final void tranfer(float dx, float dy) {
		dx /= 140;
		dy /= 140;
		// calculating vectors	
		degree_horizontal += dx;
		degree_vertical += dy;
		if (degree_vertical < 0)
			degree_vertical = 0;
		else if (degree_vertical > degree_90)
			degree_vertical = degree_90;
		center_x = (float) (radius * Math.sin(degree_horizontal));
		center_y = (float) (radius * Math.cos(degree_horizontal));
		center_z = (float) (radius * Math.sin(degree_vertical));
		up_z = (float) Math.cos(degree_vertical);
		center_x *= up_z;
		center_y *= up_z;
	}

	public static final void zoom(float zoom) {
		Log.d("zoom", "" + zoom);
		zoom *= radius;
		if (zoom > 20)
			zoom = 20;
		if (zoom < distance_min * 2)
			zoom = distance_min * 2;
		center_x *= zoom / radius;
		center_y *= zoom / radius;
		center_z *= zoom / radius;
		radius = zoom;
		Log.d("radius", "" + radius);
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
		gl.glClearColor(0, 0, 0, 0);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glEnable(GL10.GL_DITHER);
		gl.glEnable(GL10.GL_BLEND);
		GLU.gluPerspective(gl, 67, width / height, distance_min, distance_max);
		GLU.gluLookAt(gl, center_x, center_y, center_z, 0, 0, 0, 0, 0, 1);
		Room.render(gl);
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
		GameRenderer.width = width;
		GameRenderer.height = height;
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
//		gl.glDisable(GL10.GL_DITHER);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_COLOR_MATERIAL);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
}
