package de.tum;

/**
 * the helper defines some static methods for different actions like rotating an
 * object
 */
public class Helper {
	/**
	 * rotating a given mesh about 90 degrees around the z-axis. Notice: the
	 * original mesh is rotated and so changed
	 * 
	 * @param vertices
	 *            the mesh to rotate
	 * @param dimensions
	 *            the amount of dimensions, usually two or three
	 * @return the rotated array
	 */
	public static final float[] rotate90(float[] vertices, int dimensions) {
		for (int i = 0; i < vertices.length; i += dimensions) {
			float t = vertices[i];
			vertices[i] = vertices[i + 1];
			vertices[i + 1] = -t;
		}
		return vertices;
	}
}
