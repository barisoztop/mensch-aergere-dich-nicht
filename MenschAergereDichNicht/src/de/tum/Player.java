package de.tum;

import android.util.Log;

/**
 * a player can be a human player or an artificial intelligence. Every player
 * controls one team
 */
public abstract class Player {
	/** the current dice number */
	private static int number;

	/** the current player */
	private static Player player;

	/** the current movable pegs for the current player */
	private static Peg[] movables;

	/** the team of this player */
	private Team team;

	/** is true if a player has won */
	private boolean won;

	/** the current try in a round */
	private int current_try;

	/** the pegs of this player */
	protected Peg[] pegs;

	/**
	 * creating a player
	 * 
	 * @param team
	 *            the team of this player
	 */
	public Player(Team team) {
		this.team = team;
		pegs = Board.getPegs(team);
	}

	/**
	 * resetting this player. All pegs of this player are reset.
	 */
	public final void reset() {
		won = false;
		current_try = 0;
		for (Peg peg : pegs)
			peg.reset();
	}

	/**
	 * it's this player's turn. If the player hasn't won, the player is asked to
	 * throw the dice
	 */
	public final void makeTurn() {
		player = this;
		++current_try;
		if (won)
			MenschAergereDichNichtActivity.nextTurn(team);
		else
			throwDice();
	}

	/** the player is asked to throw the dice */
	protected abstract void throwDice();

	/**
	 * dice was thrown. If there is only one peg movable according o the dice
	 * number, than that peg is chosen. Otherwise the choosePegForMove()-method
	 * will be called in order to select a peg
	 * 
	 * @param number
	 *            the number the dice shows
	 */
	public static final void diceThrown(int number) {
		Player.number = number;
		int movable = 0, index = 0, found1st = 0;
		for (Peg peg : player.pegs) // calculates which pegs can be moved
			if (peg.checkMove(number)) {
				++movable;
				found1st = index;
				movables[index++] = peg;
			} else
				movables[index++] = null;
		if (movable == 1) // just one peg - no options for the player
			player.pegChosen(player.pegs[found1st]);
		else if (movable == 0) // no peg can be moved
			player.checkForMoreTurns();
		else // player has to select one of two or more pegs
			player.choosePegForMove(movables);
	}

	// checks whether this player can throw again. E.g. when a player has all
	// pegs on start, the player is allowed to throw three times
	private final void checkForMoreTurns() {
		if (!won && current_try < 3 && (number == 6 || !checkForOneFieldMove()))
			makeTurn();
		else {
			current_try = 0;
			MenschAergereDichNichtActivity.nextTurn(team);
		}
	}

	// checks whether any peg can move one field. Than the player is not allowed
	// to get another try
	private final boolean checkForOneFieldMove() {
		for (Peg peg : pegs)
			if (peg.checkMove(1))
				return true;
		return false;
	}

	/**
	 * player has to choose a peg for moving
	 * 
	 * @param movables
	 *            the current movable pegs
	 */
	protected abstract void choosePegForMove(Peg[] movables);

	/**
	 * peg is chosen
	 * 
	 * @param peg
	 *            the peg to move
	 */
	protected final void pegChosen(Peg peg) {
		peg.move();
	}
	
	/** peg was moved */
	public static final void pegMoved() {
		player.verifyWinner();
		player.checkForMoreTurns();
	}

	// just to verify if the player has won
	private final void verifyWinner() {
		for (Peg peg : pegs)
			if (!peg.hasFinished())
				return;
		won = true;
		Log.d("team " + team, "won");
	}
}
