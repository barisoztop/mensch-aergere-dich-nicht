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
		Peg chosen = null;
		for (Peg peg : movables)
			if (chosen == null)
				chosen = peg;
			else if (peg != null && peg.getCurrentField() > chosen.getCurrentField())
				chosen = peg;
		pegChosen(chosen);
		// ############################### needs some change
	}
}
