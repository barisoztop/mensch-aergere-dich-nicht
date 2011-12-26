package de.tum;

/**
 * a peg is a game object that moves on the board. Every player has typically
 * four pegs and every peg has the player's team color
 */
public abstract class Peg extends GameObject {
	/** the final bottom layer for pegs has his z-coordinate at 0.1 */
	protected static final float bottom = 0.1f * p;
	/** the final position where the peg starts */
	private final int pos_start;
	/** the final team of this peg */
	private final Team team;
	/** the current position of this peg */
	private int pos_current;

	/**
	 * creating a peg
	 * 
	 * @param visible
	 *            true if visible
	 * @param team
	 *            the team of this peg
	 * @param pos_start
	 *            the start position for this peg
	 */
	public Peg(boolean visible, Team team, int pos_start) {
		super(visible);
		this.team = team;
		this.pos_start = pos_current = pos_start;
	}

	/**
	 * method for moving the peg
	 * 
	 * @param fields
	 *            amount of fields to move. Typically the number the dice shows
	 */
	public final void move(int fields) {
		// getting the next position
		pos_current = Board.getPositionNext(team, pos_current, fields);
		// getting the next coordinates
		TupleFloat position = Board.getPosition(team, pos_current);
		// moving this peg
		transfer(position.x - x, position.y - y, 0);
	}

	/**
	 * resetting this peg will move it back to its start position
	 */
	public final void reset() {
		pos_current = pos_start;
	}

	/**
	 * checking whether this peg can move
	 * 
	 * @param fields
	 *            the amount of fields to move
	 * @return true if this peg can move
	 */
	public final boolean checkMove(int fields) {
		return pos_current + fields < Board.getPathLength();
	}

	/**
	 * getting the current field on the board
	 * 
	 * @return the current field
	 */
	public final int getCurrentField() {
		return pos_current;
	}

	/**
	 * getting the team
	 * 
	 * @return the peg's team
	 */
	public final Team getTeam() {
		return team;
	}

	/**
	 * getting whether this peg is already in a finish field
	 * 
	 * @return true if this peg is in a finish field
	 */
	public final boolean hasFinished() {
		return pos_current > Board.getPathLength() - Board.start_pegs;
	}
}
