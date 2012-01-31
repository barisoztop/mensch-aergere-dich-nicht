package de.tum.renderable;

import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class Textures {
	/** current zoom [0; 1[ */
	private static float zoom;
	/** texture IDs */
	private static int[][] textures;
	// just for initializing
	/** list with bitmaps for textures */
	private static final List<Bitmap> bitmaps = new LinkedList<Bitmap>();
	/** list with texture groups */
	private static final List<int[]> groups = new LinkedList<int[]>();

	/**
	 * adding a texture
	 * 
	 * @param bitmap
	 *            the texture
	 * @return the texture id's index
	 */
	public static final synchronized int addTexture(Bitmap bitmap) {
		bitmaps.add(bitmap);
		groups.add(new int[] {groups.size()});
		return groups.size() - 1;
	}
	
	/**
	 * grouping textures so that different textures can be used for zooming
	 * 
	 * @param textures
	 *            the textures for one group
	 * @return the group's index (texture id)
	 */
	public static final synchronized int groupTextures(int[] textures) {
		int[] array = new int[textures.length];
		for (int i = 0; i < textures.length; ++i)
			array[i] = textures[i];
		groups.add(array);
		return groups.size() - 1;
	}

	/**
	 * getting the actual texture id
	 * 
	 * @param index
	 *            the texture's or group's index
	 * @return the texture id
	 */
	public static final synchronized int getId(int index) {
		return textures[index][(int) (textures[index].length * zoom)];
	}
	
	/**
	 * setting the current zoom [0; 1[
	 * 
	 * @param zoom
	 *            the current zoom
	 */
	public static final synchronized void setZoom(float zoom) {
		Textures.zoom = zoom;
	}
	
	/**
	 * load bitmaps into GL10
	 * 
	 * @param gl
	 *            the Gl10
	 */
	public static final synchronized void bindTextures(GL10 gl) {
		int tmp[] = new int[bitmaps.size()];
		// Generate texture-ID array
		gl.glGenTextures(tmp.length, tmp, 0);
		for (int i = 0; i < tmp.length; ++i) {
			// Bind to texture ID
			gl.glBindTexture(GL10.GL_TEXTURE_2D, tmp[i]);

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
		//setting up all textures (also groups)
		textures = new int[groups.size()][];
		int texture = 0; int dif = 0;
		for (int[] group : groups) {
			int array[] = new int[group.length];
			for (int i = 0; i < array.length; ++i)
				array[i] = tmp[group[i] - dif];
			textures[texture++] = array;
			if (array.length != 1)
				++dif;
		}
	}
}
