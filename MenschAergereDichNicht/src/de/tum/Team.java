package de.tum;

/**
 * A team is kind of an identity for the players and the pegs. A team helps to
 * match the team color and the team number
 */
public enum Team {
	/** different teams */
	RED(0, new float[] { 1, 0, 0, 1 }),
	YELLOW(1, new float[] { 1, 1, 0, 1 }),
	GREEN(2, new float[] { 0, 1, 0, 1 }),
	BLUE(3, new float[] { 0, 0, 1, 1 });

	/** the id of that team */
	public final int id;

	/** the typical color of that team */
	public final float color[];

	/**
	 * creating a team
	 * 
	 * @param id
	 *            the id of the team
	 * @param color
	 *            an rgba-color array representing the typical team color
	 */
	Team(int id, float[] color) {
		this.id = id;
		this.color = color;
	}
}