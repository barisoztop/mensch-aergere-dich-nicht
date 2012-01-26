package de.tum.renderable;

import javax.microedition.khronos.opengles.GL10;

/**
 * a triangle stripe is a simple geometric object that consists of triangles.
 * The triangles are sorted in a row, so that always two triangles have one edge
 * in common
 */
public class TriangleFan extends SimpleGeometricObject {
	
	/**
	 * you have to initialize this fan with its vertices and its color
	 * 
	 * @param visible
	 *            true if this object is visible
	 * @param vertices
	 *            an array of vertices sorted in the stripe order
	 * @param color
	 *            an array of rgba-color values for each vertex
	 * @param textures
	 *            an array of texture values
	 * @param texture
	 *            the texture ID
	 */
	public TriangleFan(boolean visible, float[] vertices, float[] color, short[] textures, int texture) {
		super(visible, GL10.GL_TRIANGLE_FAN, vertices, color, textures, texture);
	}
}
