package de.tum.models;

import de.tum.Helper;
import de.tum.Team;
import de.tum.TupleFloat;
import de.tum.renderable.Textures;
import de.tum.renderable.TriangleStripe;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;

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
	private static final int[] path1 = { -9, 1, -1, 1, -9, 3, -1, 3};
	private static final int[] path2 = { -3, 3, -3, 11, -1, 3, -1, 11};
	private static final int[] path3 = { -1, 9, -1, 11, 1, 9, 1, 11};
	private static final TupleFloat[] fields;
	private static final TupleFloat[] besides;
	private static final TupleFloat[] dice_fields;
	/** texture values */
	private static final short[] player_start_texture = { 0, 0, 2, 0, 0, 2, 2,
			2 };
	private static final short[] player_end_texture = { 0, 0, 4, 0, 0, 1, 4, 1 };
	private static final short[] singleSquare = { 0, 0, 1, 0, 0, 1, 1, 1 };
	/** texture IDs */
	private static final int[] textures = new int[6];
	/** float array for a gray color */
	private static float[] gray = { 0.5f, 0.5f, 0.5f, 1};
	/** an array representing the color white */
	private static final float[] circle_white = { 0.9f, 0.9f, 0.9f};
	static {
		// creating values
		float[] fields_start = { -10, 8, -8, 8, -8, 10, -10, 10 };
		float[] fields_path = { -10, 2, -8, 2, -6, 2, -4, 2, -2, 2, -2,
				4, -2, 6, -2, 8, -2, 10, 0, 10 };
		float[] fields_path_beside = { -2, 2, 0, 2, 0, 2, 0, 2, 2, -2, -2,
				0, -2, 0, -2, 0, -2, 2, 0, 2 };
		float[] fields_end = { -8, 0, -6, 0, -4, 0, -2, 0 };
		float[] fields_dice = { -9, 5, -5, 9};
		fields = createFields(fields_start, fields_path, fields_end);
		besides = createFields(fields_path_beside);
		dice_fields = createFields(fields_dice);
		calculateTextures();
	}

	/**
	 * creating a classic board
	 * 
	 * @param visible
	 *            true if visible
	 */
	public ClassicBoard(boolean visible) {
		super(true, fields, besides, dice_fields, 4);
		// adding board components
		add4Parts(basicSquare, null, singleSquare, textures[5]);
		add4Parts(player_start, null, player_start_texture, -1);
		add4Parts(player_end, null, player_end_texture, -1);
		add4Parts(path_start, null, player_start_texture, -1);
		add4Parts(path1, null, player_end_texture, textures[4]);
		add4Parts(path2, null, player_end_texture, textures[4]);
		add4Parts(path3, null, singleSquare, textures[4]);
		sgobjects.add(new TriangleStripe(visible, calculateVertices(middle, p,
				p, layer_z), null, singleSquare, null, null, textures[5]));
	}

	/** {@inheritDoc} */
	@Override
	protected void action() {
		// nothing to do
		return;
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

	// just helping method for creating the static final fields beside the path
	private static final TupleFloat[] createFields(float[] besides) {
		TupleFloat[] fields = new TupleFloat[(besides.length) * 2];
			for (int index = 0, i2 = 0; i2 < 4; ++i2) {
				for (int i3 = 0; i3 < besides.length; ++i3, ++index)
					fields[index] = new TupleFloat(p * besides[i3], p
							* besides[++i3]);
				if (i2 == 3)
					break;
				Helper.rotate90(besides, 2);
			}
		return fields;
	}

	// just helping method for adding a mesh four times with four rotations
	private final void add4Parts(int[] vertices, float[] color, short[] textures, int id) {
		sgobjects.add(new TriangleStripe(visible, calculateVertices(vertices,
				p, p, layer_z), textures == null ? color != null ? color
				: Team.RED.color : null, textures, null, null, id == -1 ? ClassicBoard.textures[0] : id));
		sgobjects.add(new TriangleStripe(visible, Helper.rotate90(
				calculateVertices(vertices, p, p, layer_z), 3),
				textures == null ? color != null ? color : Team.YELLOW.color
						: null, textures, null, null, id == -1 ? ClassicBoard.textures[1] : id));
		sgobjects.add(new TriangleStripe(visible, calculateVertices(vertices,
				-p, -p, layer_z), textures == null ? color != null ? color
				: Team.GREEN.color : null, textures, null, null, id == -1 ? ClassicBoard.textures[2] : id));
		sgobjects.add(new TriangleStripe(visible, Helper.rotate90(
				calculateVertices(vertices, -p, -p, layer_z), 3),
				textures == null ? color != null ? color : Team.BLUE.color
						: null, textures, null, null, id == -1 ? ClassicBoard.textures[3] : id));
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

	// just helping method for calculating textures
	private static void calculateTextures() {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		// drawing all textures
		for (int i = 0; i < 5; ++i) {
			int[] sizes = new int[5];
			// creating different sizes
			for (int a = 32, size = sizes.length - 1; size > -1; --size, a *= 2) {
				Bitmap bitmap = Bitmap.createBitmap(a, a, Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);
				canvas.drawRGB((int) (255 * gray[0]), (int) (255 * gray[1]),
						(int) (255 * gray[2]));
				float[] color = i != 4 ? Team.values()[i].color : circle_white;
				paint.setARGB(255, 0, 0, 0);
				canvas.drawCircle(a / 2, a / 2, a * 9 / 20, paint);
				paint.setARGB(255, (int) (255 * color[0]), (int) (255 * color[1]),
						(int) (255 * color[2]));
				canvas.drawCircle(a / 2, a / 2, a * 7 / 20, paint);
				sizes[size] = Textures.addTexture(bitmap);
			}
			// grouping the five sizes
			textures[i] = Textures.groupTextures(sizes);
		}
		// creating board gray bitmap
		Bitmap bitmap = Bitmap.createBitmap(32, 32, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawRGB((int) (255 * gray[0]), (int) (255 * gray[1]),
				(int) (255 * gray[2]));
		textures[5] = Textures.addTexture(bitmap);
	}
}
