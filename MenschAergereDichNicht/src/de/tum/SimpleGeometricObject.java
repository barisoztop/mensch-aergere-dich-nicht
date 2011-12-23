package de.tum;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * this object is a geometric object, that represents a simple geometric object
 * like a triangle or stripe
 */
public abstract class SimpleGeometricObject extends GeometricObject {
	/** float array for the rgba-color values */
	private float[] color;
	/** float array for the xyz-vector values */
	private float[] vertices;
	/** buffer for rendering with values for color */
	private FloatBuffer bufferC;
	/** buffer for rendering with values for vectors */
	private FloatBuffer bufferV;
	/** amount of vertices */
	private int amount;
	/** type of rendering */
	private int type;

	/**
	 * initializing this object with its coordinates and its color
	 * 
	 * @param visible
	 *            true if this object is visible
	 * @param type
	 *            the type of vertices, e.g. a triangle stripe or a fan
	 * @param vertices
	 *            an array of vertices
	 * @param color
	 *            an array of rgba-color values for each vertex
	 */
	public SimpleGeometricObject(boolean visible, int type, float[] vertices,
			float[] color) {
		super(visible);
		this.type = type;
		setVertices(vertices);
		setColor(color);
	}

	/**
	 * method for updating the color buffer to the given float values
	 * 
	 * @param color
	 *            a float buffer with the rgba-color values
	 */
	public final void setColor(float[] color) {
		this.color = color;
		if (color.length == 4) {
			bufferC = null;
			return;
		}
		// verifying whether the buffer is null or a greater one is needed
		if (bufferC == null || bufferC.capacity() < 4 * color.length) {
			ByteBuffer buffer = ByteBuffer.allocateDirect(4 * color.length);
			buffer.order(ByteOrder.nativeOrder());
			bufferC = buffer.asFloatBuffer();
		}
		bufferC.clear();
		bufferC.put(color);
	}

	/**
	 * method for updating the vector buffer to the given float values
	 * 
	 * @param vertices
	 *            a float buffer with the vertex values
	 */
	public final void setVertices(float[] vertices) {
		this.vertices = vertices;
		// verifying whether the buffer is null or a greater one is needed
		if (bufferV == null || bufferV.capacity() < 4 * vertices.length) {
			ByteBuffer buffer = ByteBuffer.allocateDirect(4 * vertices.length);
			buffer.order(ByteOrder.nativeOrder());
			bufferV = buffer.asFloatBuffer();
		}
		bufferV.clear();
		bufferV.put(vertices);
		amount = vertices.length / 3;
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
	public final void transfer(float dx, float dy, float dz) {
		// adding the given offset
		for (int i = 0; i < vertices.length; i += 3) {
			vertices[i] += dx;
			vertices[i + 1] += dy;
			vertices[i + 2] += dz;
		}
		// updating vertices
		setVertices(vertices);
	}

	/**
	 * method for rendering this geometric object
	 * 
	 * @param gl
	 *            the GL10 object for rendering this geometric object
	 */
	public final void render(GL10 gl) {
		// checking whether current object is visible
		if (!visible)
			return;

		bufferV.rewind();
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bufferV);
		if (bufferC != null) {
			// enabling a color array
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			// setting the position to 0
			bufferC.rewind();
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, bufferC);
		} else {
			// disabling a color array
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			gl.glColor4f(color[0], color[1], color[2], color[3]);
		}
		// rendering this object
		gl.glDrawArrays(type, 0, amount);
	}
}
