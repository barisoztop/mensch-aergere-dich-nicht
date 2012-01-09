package de.tum;

import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class Textures {
	public static int[] textures;
	public static List<Bitmap> bitmaps = new LinkedList<Bitmap>();

	public static final int addTexture(Bitmap bitmap) {
		bitmaps.add(bitmap);
		return bitmaps.size() - 1;
	}

	// Load bitmaps into GL10
	public static final void bindTextures(GL10 gl) {
		textures = new int[bitmaps.size()];
		// Generate texture-ID array
		gl.glGenTextures(textures.length, textures, 0);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
		for (int i = 0; i < textures.length; ++i) {
			// Bind to texture ID
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
			// Build Texture from loaded bitmap for the currently-bind texture
			// ID
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmaps.get(i), 0);
			// bitmaps.get(i).recycle();
		}
	}
}
