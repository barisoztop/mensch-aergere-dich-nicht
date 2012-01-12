package de.tum.player;

import de.tum.Team;
import de.tum.models.Dice;
import de.tum.models.Peg;

/**
 * this player is a human player. All moves are chosen by a human
 */
public class HumanPlayer extends Player {
	/**
	 * creating a player
	 * 
	 * @param team
	 *            the team of this player
	 */
	public HumanPlayer(Team team) {
		super(team);
	}

	/** {@inheritDoc} */
	protected void throwDice() {
		Dice.throwIt();
		// ############################### needs some change
	}

	/** {@inheritDoc} */
	protected void choosePegForMove(Peg[] movables) {
		pegChosen(movables[0]);
		// ############################### needs some change
	}
}
