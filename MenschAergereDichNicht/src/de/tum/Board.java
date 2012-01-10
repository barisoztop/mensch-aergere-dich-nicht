package de.tum;

/**
 * a board is a game object. The pegs are moving on the board.
 */
public abstract class Board extends GameObject {
	public static final int start_pegs = 4;
	// length of the path around the board
	public static int path_length;
	// the coordinates of the fields
	private static TupleFloat[] fields;
	// matching current position of pegs to fields
	private static Peg[] peg_fields;
	// maximum amount players allowed for that board
	private static int teams;
	// current playing players
	private static int players;
	// amount of all start fields for later calculation
	private static int start_length;
	// all pegs interacting on the current board
	private static Peg[] pegs;

	/**
	 * creating a board
	 * 
	 * @param visible
	 *            true if visible
	 * @param fields
	 *            all fields on this actual board
	 * @param teams
	 *            the amount of teams this board is made for
	 * @param players
	 *            the amount of players that actually play
	 */
	public Board(boolean visible, TupleFloat[] fields, int teams, int players) {
		super(visible);
		set(fields, teams, players);
		createPegs();
	}

	/**
	 * getting the amount of teams for this board
	 * 
	 * @return the amount of teams, typically four or eight
	 */
	public static final int getTeams() {
		return teams;
	}

	/**
	 * getting the amount of players
	 * 
	 * @return the amount of playing teams
	 */
	public static final int getPlayers() {
		return players;
	}

	/**
	 * getting the real 2D-position of a field
	 * 
	 * @param team
	 *            the team of this peg
	 * @param fieldPos
	 *            the current position for this peg
	 * @return a pair of floats representing the location on the board
	 */
	public static final TupleFloat getPosition(Peg peg, int position) {
		int position_abs = position < start_pegs ? position + peg.getTeam().id * start_pegs : getAbsolutePositionOnPathOrEnd(peg.getTeam(), position);
		// updating current position
		for (int i = 0; i < peg_fields.length; ++i)
			if (peg_fields[i] == peg) {
				peg_fields[i] = null;
				break;
			}
		peg_fields[position_abs] = peg;
		return fields[position_abs];
	}

	/**
	 * getting the next field for a peg
	 * 
	 * @param team
	 *            the team of this peg
	 * @param fieldPos
	 *            the current position for this peg
	 * @param distance
	 *            the distance to move, typically the number the dice shows
	 * @return the number of the next field for the given peg or -1 if the peg
	 *         is not allowed to move
	 */
	public static final int getPositionNext(Team team, int fieldPos,
			int distance) {
		if (fieldPos < start_pegs) // peg didn't start
			return distance == 6 && isFree(team, getAbsolutePositionOnPathOrEnd(team, start_pegs)) ? start_pegs : -1;
		int posNext = fieldPos + distance;
		if (posNext >= 2 * start_pegs + path_length) // too far
			return -1;
		return isFree(team, getAbsolutePositionOnPathOrEnd(team, posNext)) ? posNext : -1;
	}
	
	// just for checking whether the given field is free or a different team is there	
	private static final boolean isFree(Team team, int absolute_pos) {
		return peg_fields[absolute_pos] == null
				|| peg_fields[absolute_pos].getTeam() != team;
	}

	// just for calculating the absolute field on board
	private static int getAbsolutePositionOnPathOrEnd(Team team,
			int relative_pos) {
		return start_length
				+ (relative_pos < start_pegs + path_length ? (team.id
						* path_length / teams + relative_pos - start_pegs)
						% path_length : relative_pos - start_pegs + team.id
						* start_pegs);
	}

	/**
	 * getting the pegs of a special team
	 * 
	 * @param team
	 *            the team that owns the pegs
	 * @return an array containing all pegs of the given team
	 */
	public static final Peg[] getPegs(Team team) {
		Peg[] pegs = new Peg[start_pegs];
		for (int i = 0; i < start_pegs; ++i)
			pegs[i] = Board.pegs[i + team.id * start_pegs];
		return pegs;
	}

	// creating all pegs
	private static final void createPegs() {
		pegs = new Peg[players * start_pegs];
		for (int i = 0; i < pegs.length; ++i) {
			pegs[i] = new SimplePeg(true, Team.values()[i / start_pegs], i
					% start_pegs);
			Room.addRenderable(pegs[i]);
		}
	}

	// just for setting and calculating some helping values
	private static final void set(TupleFloat[] fields, int teams, int players) {
		Board.fields = fields;
		Board.teams = teams;
		Board.players = players;
		start_length = teams * start_pegs;
		// start length = end length
		path_length = fields.length - start_length * 2;
		peg_fields = new Peg[fields.length];
	}
}
