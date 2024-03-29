package de.tum.models;

import javax.microedition.khronos.opengles.GL10;

import de.tum.Helper;
import de.tum.Team;
import de.tum.TupleFloat;
import de.tum.renderable.TriangleStripe;

/**
 * a simple peg is a peg that is represented just by a cuboid. Like all pegs it
 * can move on the board on given paths
 */
public class SimplePeg extends Peg {
	/** the height of this peg */
	private static final int height = 3;
	/** the vertex mesh */
	private static final int[] vertices = { 0, 1, 0, 1, 0, 0, 0, 1, height, 1,
			0, height, 0, -1, height, 1, 0, 0, 0, -1, 0, 0, 1, 0, -1, 0, 0, 0,
			1, height, -1, 0, height, 0, -1, height, -1, 0, 0, 0, -1, 0 };
	/** the color mesh */
	private static final short[] colors = { 0, 1, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1,
			1, 0 };
	/** an array representing the color black */
	private static final float[] black = { 0, 0, 0, 1 };
	/** an array representing the color white */
	private static final float[] white = { 1, 1, 1, 1 };
	/** an array representing the final color mesh */
	private final float[] color;
	/** an array representing the final color mesh for selection 11*/
	private final float[] color_selection;

	/**
	 * creating a new simple peg
	 * 
	 * @param visible
	 *            true if visible
	 * @param team
	 *            the team of this peg
	 * @param pos_start
	 *            the start position for this peg
	 */
	public SimplePeg(boolean visible, Team team, int pos_start) {
		super(visible, team, pos_start);
		// creating the real mesh
		sgobjects.add(new TriangleStripe(true, convert(vertices, team),
				color = createColor(new float[][] { team.color, black }), null,
				null, null, 0));
		color_selection = createColor(new float[][] { team.color, white }); 
		TupleFloat position = Board.getPosition(this, pos_start, true);
		// moving this peg to its start position
		transfer(position.x, position.y, layer_z + bottom);
	}

	/** {@inheritDoc} */
	@Override
	public void setSelection(boolean selected) {
		setColor(selected ? color_selection : color);
	}

	// just for creating the real vertex mesh
	private static float[] convert(int[] array, Team team) {
		float[] array2 = new float[array.length];
		for (int i = 0; i < array.length; ++i)
			array2[i] = p * array[i];
		if (team.id % 2 == 0)
			Helper.rotate90(array2, 3);
		return array2;
	}

	// just or creating the real color mesh
	private static float[] createColor(float[][] set) {
		float[] colors = new float[56];
		for (int i1 = 0; i1 < colors.length; i1 += 4)
			for (int i2 = 0; i2 < 4; ++i2)
				colors[i1 + i2] = set[SimplePeg.colors[i1 / 4]][i2];
		return colors;
	}

	/** {@inheritDoc} */
	@Override
	protected final void rotate(GL10 gl) {
		return;
	}
}
