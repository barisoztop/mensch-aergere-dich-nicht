package de.tum.renderable;

import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class Textures {
	/** texture IDs */
	public static int[] textures;
	/** list with bitmaps for textures */
	public static List<Bitmap> bitmaps = new LinkedList<Bitmap>();

	/**
	 * adding a texture
	 * 
	 * @param bitmap
	 *            the texture
	 * @return the texture id's index
	 */
	public static final int addTexture(Bitmap bitmap) {
		bitmaps.add(bitmap);
		return bitmaps.size() - 1;
	}

	/**
	 * load bitmaps into GL10
	 * 
	 * @param gl
	 *            the Gl10
	 */
	public static final void bindTextures(GL10 gl) {
		textures = new int[bitmaps.size()];
		// Generate texture-ID array
		gl.glGenTextures(textures.length, textures, 0);
		for (int i = 0; i < textures.length; ++i) {
			// Bind to texture ID
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);

			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
//                    GL10.GL_LINEAR);
                    GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                    GL10.GL_NEAREST);
//                    GL10.GL_LINEAR);

            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                    GL10.GL_REPEAT);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                    GL10.GL_REPEAT);

            gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                    GL10.GL_REPLACE);

			// Build Texture from loaded bitmap for the currently-bind texture
			// ID
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmaps.get(i), 0);
			// bitmaps.get(i).recycle();
		}
	}
}
