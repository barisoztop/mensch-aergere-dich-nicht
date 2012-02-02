package de.tum.models;

import de.tum.Room;
import de.tum.Team;
import de.tum.TupleFloat;
import de.tum.player.Player;
import de.tum.renderable.GameObject;

/**
 * a board is a game object. The pegs are moving on the board.
 */
public abstract class Board extends GameObject {
	/** amount of start pegs */
	public static final int start_pegs = 4;
	/** length of the path around the board */
	public static int path_length;
	// the coordinates of the fields
	private static TupleFloat[] fields;
	// matching current position of pegs to a direction for stepping beside
	private static TupleFloat[] besides;
	// the coordinates for the dice to start
	private static TupleFloat[] dice_fields;
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
	 */
	public Board(boolean visible, TupleFloat[] fields, TupleFloat[] besides,
			TupleFloat[] dice_fields, int teams) {
		super(visible);
		set(fields, besides, dice_fields, teams);
		createPegs();
	}
	
	/**
	 * setting the amount of players and starting the game
	 * 
	 * @param players
	 *            the amount of playing teams
	 */
	public static final void startGame(int players) {
		Board.players = players;
		Dice.reset();
		Player.nextTurn();
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
	 * @param position
	 *            the current position for this peg
	 * @param permanent
	 *            true if the pegs is there permanent (false if peg is just crossing the field)
	 * @return a pair of floats representing the location on the board
	 */
	public static final TupleFloat getPosition(Peg peg, int position, boolean permanent) {
		int position_abs = position < start_pegs ? position + peg.team.id
				* start_pegs : getAbsolutePositionOnPathOrEnd(peg.team,
				position);
		if (permanent) {
			// updating current position
			for (int i = 0; i < peg_fields.length; ++i)
				if (peg_fields[i] == peg) {
					peg_fields[i] = null;
					break;
				}
			if (peg_fields[position_abs] != null)
				peg_fields[position_abs].reset();
			peg_fields[position_abs] = peg;
		}
		else if (peg_fields[position_abs] != null) // moving peg beside that field
			peg_fields[position_abs].giveWay(besides[position_abs - start_length].x, besides[position_abs - start_length].y);
		return new TupleFloat(fields[position_abs].x, fields[position_abs].y);
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
	 * @return a tuple containing the number of the next field for the given peg
	 *         and the team's id or -1 if the field is empty or -2 if the peg is not allowed to move
	 */
	public static final TupleFloat getPositionNext(Team team, int fieldPos,
			int distance) {
		return fieldPos < start_pegs ? new TupleFloat(start_pegs,
				distance == 6 ? isFree(team, start_pegs) : -2)
				: new TupleFloat(distance += fieldPos, distance >= 2
						* start_pegs + path_length ? -2
						: isFree(team, distance));
	}
	
	/**
	 * getting the current start field for the dice
	 * 
	 * @param team
	 *            the current team
	 * @param choice
	 *            additional value, default = 0
	 * @return the coordinates of the current start field
	 */
	public static final TupleFloat getPositionForDice(Team team, int choice) {
		return dice_fields[team.id * 2 + choice];
	}
	
	/**
	 * getting the dice start field choices
	 * 
	 * @return the amount of options for the dice start field
	 */
	public static final int getDiceStartFieldChoices() {
		return 2;
	}
	
	// just for checking whether the given field is free or a different team is there	
	private static final int isFree(Team team, int relative_pos) {
		return peg_fields[relative_pos = getAbsolutePositionOnPathOrEnd(team,
				relative_pos)] == null ? -1 : (relative_pos =
				peg_fields[relative_pos].team.id) == team.id ? -2 : relative_pos;
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
		pegs = new Peg[teams * start_pegs];
		for (int i = 0; i < pegs.length; ++i) {
			pegs[i] = new ClassicPeg(false, Team.values()[i / start_pegs], i
					% start_pegs);
			Room.addRenderable(pegs[i]);
		}
	}

	// just for setting and calculating some helping values
	private static final void set(TupleFloat[] fields, TupleFloat[] besides, TupleFloat[] dice_fields, int teams) {
		Board.fields = fields;
		Board.besides = besides;
		Board.dice_fields = dice_fields;
		Board.teams = teams;
		start_length = teams * start_pegs;
		// start length = end length
		path_length = fields.length - start_length * 2;
		peg_fields = new Peg[fields.length];
	}
}
