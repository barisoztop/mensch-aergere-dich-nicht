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

	/** true if that team has a human player*/
	private boolean human;

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
	
	/**
	 * verifying if this team is human
	 * 
	 * @return true if human
	 */
	public final boolean isHuman() {
		return human;
	}
	
	/**
	 * setting whether this team is human or not
	 * 
	 * @param human
	 *            true if human
	 */
	public final void setHuman(boolean human) {
		this.human = human;
	}
	
	/**
	 * getting the team with the given id
	 * 
	 * @param id
	 *            the id of the team
	 * 
	 * @return the team with the given id
	 */
	public static final Team getById(int id) {
		switch (id) {
		case 0:
			return RED;
		case 1:
			return YELLOW;
		case 2:
			return GREEN;
		case 3:
			return BLUE;
			default:
				throw new RuntimeException("wrong team id");
		}
	}
}