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
	 * it's this player's turn. If the player hasn't won, the dice will be
	 * thrown
	 */
	public final void makeTurn() {
		player = this;
		++current_try;
		if (won)
			MenschAergereDichNichtActivity.nextTurn(team);
		else
			Dice.throwIt();
	}

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
		for (Peg peg : player.pegs)
			if (peg.checkMove(number)) {
				++movable;
				found1st = index;
				movables[index++] = peg;
			} else
				movables[index++] = null;
		if (movable == 1)
			player.pegChosen(player.pegs[found1st]);
		else if (movable == 0)
			player.checkForMoreTurns();
		else
			player.choosePegForMove(movables);
	}

	// checks whether this player can throw again. E.g. when a player has all
	// pegs on start, the player is allowed to throw three times
	private void checkForMoreTurns() {
		if (number == 6 && current_try < 3)
			makeTurn();
		else {
			current_try = 0;
			MenschAergereDichNichtActivity.nextTurn(team);
		}
		// ############################### needs some change
	}

	/**
	 * player has to choose a peg for moving
	 * 
	 * @param movables
	 *            the current movable pegs
	 */
	protected abstract void choosePegForMove(Peg[] movables);

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
	protected final void pegChosen(Peg peg) {
		peg.move(number);
		verifyWinner();
		checkForMoreTurns();
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
