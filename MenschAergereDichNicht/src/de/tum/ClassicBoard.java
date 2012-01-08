package de.tum;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;

/**
 * a classic board is a game board with a classic layout and design. It
 * represents a game object and can be rendered to the GUI
 */
public class ClassicBoard extends Board {
	/** coordinates for the path and the start and end position */
	private static final int[] basicSquare = { -3, 11, -7, 11, -3, 3, -7, 7,
			-12, 3, -11, 7, -12, 12, -11, 11, 3, 12, 3, 11 };
	private static final int[] player_start = { -11, 11, -7, 11, -11, 7, -7, 7 };
	private static final int[] player_end = { -9, 1, -1, 1, -9, -1, -1, -1 };
	private static final int[] middle = { -1, 1, 1, 1, -1, -1, 1, -1 };
	private static final int[] path_start = { -11, 1, -11, 3, -9, 1, -9, 3 };
	private static final int[] path = { -9, 1, -9, 3, -1, 1, -3, 3, -1, 9, -3,
			11, 1, 9, 1, 11 };
	private static final TupleFloat[] fields;
	static {
		final float[] fields_start = { -10, 10, -8, 10, -10, 8, -8, 8 };
		final float[] fields_path = { -10, 2, -8, 2, -6, 2, -4, 2, -2, 2, -2,
				4, -2, 6, -2, 8, -2, 10, 0, 10 };
		final float[] fields_end = { -8, 0, -6, 0, -4, 0, -2, 0 };
		fields = createFields(fields_start, fields_path, fields_end);
	}
	/** float array for a gray color */
	private float[] gray = { 0.5f, 0.5f, 0.5f, 1 };
	/** float array for color for the four first field on the path */
	private float[] path_start_color = { 0.9f, 0.9f, 0.9f, 1 };
	/** float array for color for the path */
	private float[] path_color = { 0.8f, 0.8f, 0.8f, 1 };

	/**
	 * creating a classic board
	 * 
	 * @param visible
	 *            true if visible
	 * @param players
	 *            the amount of players that really play
	 */
	public ClassicBoard(boolean visible, int players) {
		super(true, fields, 4, players);
		// adding board components
		add4Parts(basicSquare, gray);
		add4Parts(player_start, null);
		add4Parts(player_end, null);
		add4Parts(path_start, path_start_color);
		add4Parts(path, path_color);
		sgobjects.add(new TriangleStripe(visible, calculateVertices(middle, p,
				p, layer_z), gray));
	}

	// just helping method for creating the static final fields
	private static final TupleFloat[] createFields(float[] start, float[] path,
			float[] end) {
		TupleFloat[] fields = new TupleFloat[(start.length + path.length + end.length) * 2];
		for (int index = 0, i1 = 0; i1 < 3; ++i1) {
			for (int i2 = 0; i2 < 4; ++i2) {
				for (int i3 = 0; i3 < start.length; ++i3, ++index)
					fields[index] = new TupleFloat(p * start[i3], p
							* start[++i3]);
				if (i2 == 3)
					break;
				Helper.rotate90(start, 2);
			}
			start = i1 == 0 ? path : end;
		}
		return fields;
	}

	// just helping method for adding a mesh four times with four rotations
	private final void add4Parts(int[] vertices, float[] color) {
		sgobjects.add(new TriangleStripe(visible, calculateVertices(vertices,
				p, p, layer_z), color != null ? color : Team.RED.color));
		sgobjects.add(new TriangleStripe(visible, Helper.rotate90(
				calculateVertices(vertices, p, p, layer_z), 3),
				color != null ? color : Team.YELLOW.color));
		sgobjects.add(new TriangleStripe(visible, calculateVertices(vertices,
				-p, -p, layer_z), color != null ? color : Team.GREEN.color));
		sgobjects.add(new TriangleStripe(visible, Helper.rotate90(
				calculateVertices(vertices, -p, -p, layer_z), 3),
				color != null ? color : Team.BLUE.color));
	}

	// just helping method for calculating mesh
	private static float[] calculateVertices(int[] vertices2D, float fx,
			float fy, float dz) {
		float[] vertices = new float[vertices2D.length / 2 * 3];
		for (int i1 = 0, i2 = 0; i1 < vertices2D.length; i1 += 2, i2 += 3) {
			vertices[i2] = fx * vertices2D[i1];
			vertices[i2 + 1] = fy * vertices2D[i1 + 1];
			vertices[i2 + 2] = dz;
		}
		return vertices;
	}
	
	// just helping method for calculating the textures
	private static void calculateTextures() {
		Bitmap bitmap = Bitmap.createBitmap(40, 40, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawARGB(255, 100, 255, 50);
		int i = Float.SIZE;
	}
}
