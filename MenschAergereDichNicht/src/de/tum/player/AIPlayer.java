package de.tum.player;

import de.tum.Team;
import de.tum.models.Dice;
import de.tum.models.Peg;

/**
 * this player is an artificial intelligence. All moves are calculated by this AI.
 * There is no direct interaction with any human player
 */
public class AIPlayer extends Player {
	/**
	 * creating a player
	 * 
	 * @param team
	 *            the team of this player
	 */
	public AIPlayer(Team team) {
		super(team);
	}

	/** {@inheritDoc} */
	protected void throwDice() {
		Dice.throwIt();
	}

	/** {@inheritDoc} */
	protected void choosePegForMove(Peg[] movables) {
		pegChosen(movables[0]);
		// ############################### needs some change
	}
}
