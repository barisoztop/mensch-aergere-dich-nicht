package de.tum;

/**
 * a board is a game object. The pegs are moving on the board.
 */
public class Board extends GameObject {
	public static final int start_pegs = 4;
	private static TupleFloat[] fields;
	private static int teams;
	private static int start_length;
	private static int path_length;
	private static int players;
	private Peg[] pegs;

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
	public static final TupleFloat getPosition(Team team, int fieldPos) {
		if (fieldPos < start_pegs)
			// pegs didn't start
			return fields[fieldPos + team.id * start_pegs];
		int index = start_length
				+ (team.id * path_length / teams + fieldPos - start_pegs)
				% path_length;
		return fields[index]; // ############################### needs some
								// change
	}

	/**
	 * getting the path length of this board
	 * 
	 * @return the amount of fields a peg has to cross for one round
	 */
	public static final int getPathLength() {
		return path_length;
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
	 * @return the number of the next field for the given peg
	 */
	public static final int getPositionNext(Team team, int fieldPos,
			int distance) {
		if (fieldPos < start_pegs)
			// pegs didn't start
			return start_pegs;
		else
			return fieldPos + distance; // ############################### needs
										// some change
	}

	/**
	 * getting the pegs of a special team
	 * 
	 * @param team
	 *            the team that owns the pegs
	 * @return an array containing all pegs of the given team
	 */
	public final Peg[] getPegs(Team team) {
		Peg[] pegs = new Peg[start_pegs];
		for (int i = 0; i < start_pegs; ++i)
			pegs[i] = this.pegs[i + team.id * start_pegs];
		return pegs;
	}

	// not needed later - just for testing
	public final void movePeg(int peg, int distance) {
		pegs[peg].move(1);
	}

	// creating all pegs
	private final void createPegs() {
		pegs = new Peg[players * start_pegs];
		for (int i = 0; i < pegs.length; ++i) {
			pegs[i] = new SimplePeg(true, Team.values()[i / 4], i % 4);
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
	}
}
