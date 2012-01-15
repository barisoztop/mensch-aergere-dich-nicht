package de.tum.player;

import android.util.Log;
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
		Dice.throwIt(team);
		// ############################### needs some change
	}

	/** {@inheritDoc} */
	protected void choosePegForMove(Peg[] movables, int movable) {
	Log.d("human player", "movable=" + movable);
		Peg chosen = null;
		for (Peg peg : movables)
			if (peg != null) {
				peg.setSelection(true);
				chosen = peg;
			}
		pegChosen(chosen);
		// ############################### needs some change
	}
}
