package de.tum.renderable;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * this object is a geometric object, that represents a simple geometric object
 * like a triangle or stripe
 */
public abstract class SimpleGeometricObject extends GeometricObject {
	/** size in bytes of a float value */
	private static final int size_float = Float.SIZE >> 3; 
	/** size in bytes of a short value */
	private static final int size_short = Short.SIZE >> 3; 
	/** float array for the rgba-color values */
	private float[] color;
	/** float array for the xyz-vector values */
	private float[] vertices;
	/** buffer for rendering with values for color */
	private FloatBuffer bufferC;
	/** buffer for rendering with values for vectors */
	private FloatBuffer bufferV;
	/** amount of vertices */
	private ShortBuffer bufferT;
	/** amount of vertices */
	private int amount;
	/** type of rendering */
	private int type;
	/** texture id*/
	private int texture;

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
	 * @param textures
	 *            an array of texture values
	 * @param bufferV
	 *            a buffer of vertices
	 * @param bufferC
	 *            a buffer of rgba-color values for each vertex
	 * @param texture
	 *            the texture ID
	 */
	public SimpleGeometricObject(boolean visible, int type, float[] vertices,
			float[] color, short textures[], FloatBuffer bufferV,
			FloatBuffer bufferC, int texture) {
		super(visible);
		this.type = type;
		this.texture = texture;
		setVertices(vertices, bufferV);
		setColor(color, bufferC);
		setTextures(textures);
	}

	/**
	 * method for creating a buffer with the given float or short values
	 * 
	 * @param values1
	 *            a float buffer with the values
	 * @param values2
	 *            a short buffer with the values
	 * @return the buffer
	 */
	public static final Buffer createBuffer(float[] values1, short[] values2) {
		ByteBuffer bufferByte = ByteBuffer
				.allocateDirect(values1 != null ? size_float * values1.length
						: size_short * values2.length);
		bufferByte.order(ByteOrder.nativeOrder());
		// insert values
		return values1 != null ? ((FloatBuffer) bufferByte.asFloatBuffer()
				.clear()).put(values1) : ((ShortBuffer) bufferByte
				.asShortBuffer().clear()).put(values2);
	}

	/**
	 * method for updating the color buffer to the given float values
	 * 
	 * @param color
	 *            a float array with the rgba-color values
	 * @param buffer
	 *            a float buffer with the rgba-color values
	 */
	public final void setColor(float[] color, FloatBuffer buffer) {
		if ((bufferC = buffer) == null)
		bufferC = (this.color = color) == null || color.length == 4 ?
				null : (FloatBuffer) createBuffer(color, null);
		else if (color != null)
			this.color = color;
	}

	/**
	 * method for updating the vector buffer to the given float values.
	 * Buffer will be created if null
	 * 
	 * @param vertices
	 *            a float array with the vertex values
	 * @param buffer
	 *            a float buffer with the vertex values
	 */
	public final void setVertices(float[] vertices, FloatBuffer buffer) {
		this.vertices = vertices;
		if ((bufferV = buffer) == null)
			bufferV = (FloatBuffer) createBuffer(vertices, null);
		amount = vertices.length / 3;
	}

	/**
	 * method for updating the texture buffer to the given float values
	 * 
	 * @param textures
	 *            a float buffer with the texture values
	 */
	public final void setTextures(short[] textures) {
		bufferT = textures == null ?
				null : (ShortBuffer) createBuffer(null, textures);
	}
	
	/** {@inheritDoc} */
	public final void transfer(float dx, float dy, float dz) {
		// adding the given offset
		for (int i = 0; i < vertices.length; i += 3) {
			vertices[i] += dx;
			vertices[i + 1] += dy;
			vertices[i + 2] += dz;
		}
		// updating vertices
		setVertices(vertices, null);
	}
	
	/** {@inheritDoc} */
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
			if (bufferT == null) // simple color
			  gl.glColor4f(color[0], color[1], color[2], color[3]);
			else { // textures
				bufferT.rewind();
				gl.glEnable(GL10.GL_TEXTURE_2D); // enable textures
			    gl.glBindTexture(GL10.GL_TEXTURE_2D, Textures.getId(texture)); // set current texture
				gl.glTexCoordPointer(2, GL10.GL_SHORT, 0, bufferT);
			}
		}
		// rendering this object
		gl.glDrawArrays(type, 0, amount);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
}
