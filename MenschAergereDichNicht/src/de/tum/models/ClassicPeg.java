package de.tum.models;

import de.tum.Team;
import de.tum.TupleFloat;
import de.tum.renderable.SimpleGeometricObject;
import de.tum.renderable.TriangleFan;
import de.tum.renderable.TriangleStripe;

/**
 * a classic peg is a peg that represents a classic peg model. Like all pegs it
 * can move on the board on given paths
 */
public class ClassicPeg extends Peg {
	/** the height of this peg */
	private static final float height = 3;
	/** the radius of this peg */
	private static final float radius = 0.8f;
	/** the amount of horizontal splits */
	private static final int split_horizontal = 20;
	/** the amount of vertical splits */
	private static final int split_vertical = 13;
	/** an array representing the color black */
	private static final float[] black = { 0, 0, 0, 1 };
	/** the vertex mesh */
	private static final float[] vertices[];
	/** an array representing the final color mesh */
	private final float[] colors[];
	/** an array representing the final color mesh for selection 11 */
	private final float[] colors_selection[];
	static {
		// creating the real mesh
		vertices = calculateVertices();
	}

	/**
	 * creating a new classic peg
	 * 
	 * @param visible
	 *            true if visible
	 * @param team
	 *            the team of this peg
	 * @param pos_start
	 *            the start position for this peg
	 */
	public ClassicPeg(boolean visible, Team team, int pos_start) {
		super(visible, team, pos_start);
		// calculating colors
		colors = new float[vertices.length][];
		float max = vertices.length;
		for (int y = 0; y < vertices.length - 1; ++y) {
			colors[y] = new float[vertices[y].length / 3 * 4];
			for (int x = 0; x < colors[y].length;) {
				if (y != 0 || x == 0) {
					colors[y][x++] = team.color[0] * (1 - y / max);
					colors[y][x++] = team.color[1] * (1 - y / max);
					colors[y][x++] = team.color[2] * (1 - y / max);
					colors[y][x++] = 1;
				}
				colors[y][x++] = team.color[0] * (1 - (y + 1) / max);
				colors[y][x++] = team.color[1] * (1 - (y + 1) / max);
				colors[y][x++] = team.color[2] * (1 - (y + 1) / max);
				colors[y][x++] = 1;
			}
		}
		colors[colors.length - 1] = black;
		colors_selection = new float[colors.length][];
		for (int y = 0; y < colors.length; ++y) {
			colors_selection[y] = new float[colors[y].length];
			for (int x = 0; x < colors_selection[y].length; ++x)			
				colors_selection[y][x] = colors[y][x] > 0.7 || x % 8 < 4 ? 1 : colors[y][x] + 0.3f;
		}
		// creating shapes
		sgobjects.add(new TriangleFan(visible, vertices[0], colors[0], null, 0));
		for (int y = 1; y < vertices.length - 1; ++y)
			sgobjects.add(new TriangleStripe(visible, vertices[y], colors[y], null, 0));
		sgobjects.add(new TriangleFan(visible, vertices[vertices.length - 1], colors[colors.length - 1], null, 0));
		// moving this peg to its start position
		TupleFloat position = Board.getPosition(this, pos_start, true);
		transfer(position.x, position.y, layer_z + bottom);
	}

	/** {@inheritDoc} */
	@Override
	public void setSelection(boolean selected) {
		int i = 0;
		// changing colors
		for (SimpleGeometricObject object : sgobjects)
			object.setColor(selected ? colors_selection[i++] : colors[i++]);
	}

	// just helping method for calculating mesh
	private static final float[][] calculateVertices() {
		float[][][] values = new float[split_vertical + 1][split_horizontal][3];
		for (int y = 0; y < split_vertical + 1; ++y) {
			float f = y == split_vertical ? 1 : (float) Math.sin(Math.PI * y * 0.85 / split_vertical);
			float z = y == split_vertical ? 0 : height - radius * (1 - (float) Math.cos(Math.PI * y * 0.85 / split_vertical));
			for (int x = 0; x < split_horizontal; ++x) {
				values[y][x][0] = f * (float) (radius * Math.sin(Math.PI * 2 * x / split_horizontal));
				values[y][x][1] = f * (float) (radius * Math.cos(Math.PI * 2 * x / split_horizontal));
				values[y][x][2] = z; 
			}
		}
		float vertices[][] = new float[split_vertical + 2][];
		int y = 0;
		vertices[y] = new float[split_horizontal * 3 + 3];
		vertices[y][0] = vertices[y][1] = 0;
		vertices[y][2] = height * p;
		for (int x = 0; x < split_horizontal; ++x) {
			vertices[y][x * 3 + 3] = values[y][x % split_horizontal][0] * p;
			vertices[y][x * 3 + 4] = values[y][x % split_horizontal][1] * p;
			vertices[y][x * 3 + 5] = values[y][x % split_horizontal][2] * p;
		}
		for (y = 1; y < split_vertical + 1; ++y) {
			vertices[y] = new float[split_horizontal * 6 + 6];
			for (int x = 0; x < split_horizontal + 1; ++x) {
				vertices[y][x * 6] = values[y - 1][x % split_horizontal][0] * p;
				vertices[y][x * 6 + 1] = values[y - 1][x % split_horizontal][1] * p;
				vertices[y][x * 6 + 2] = values[y - 1][x % split_horizontal][2] * p;
				vertices[y][x * 6 + 3] = values[y][x % split_horizontal][0] * p;
				vertices[y][x * 6 + 4] = values[y][x % split_horizontal][1] * p;
				vertices[y][x * 6 + 5] = values[y][x % split_horizontal][2] * p;
			}
		}
		vertices[y] = new float[split_horizontal * 3 + 3];
		vertices[y][0] = vertices[y][1] = vertices[y][2] = 0;
		for (int x = 0; x < split_horizontal; ++x) {
			vertices[y][x * 3 + 3] = values[y - 1][x % split_horizontal][0] * p;
			vertices[y][x * 3 + 4] = values[y - 1][x % split_horizontal][1] * p;
			vertices[y][x * 3 + 5] = values[y - 1][x % split_horizontal][2] * p;
		}
		return vertices;
	}
}
