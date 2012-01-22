package de.tum.player;

import de.tum.Team;
import de.tum.models.Dice;
import de.tum.models.Peg;
import de.tum.multiplayer.MultiplayerActivity;

/**
 * this player is an artificial intelligence. All moves are calculated by this
 * AI. There is no direct interaction with any human player
 */
public class AIPlayer extends Player {
	/**
	 * creating a player
	 * 
	 * @param team
	 *            the team of this player
	 */
	public AIPlayer(Team team, Class<?> staticAccess) {
		super(team, staticAccess);
	}

	/** {@inheritDoc} */
	protected void throwDice() {
		Dice.throwIt(team);
	}

	/** {@inheritDoc} */
	protected void choosePegForMove(Peg[] movables, int movable) {
		if (movable != -1) { // just one peg - no options for the player
			pegChosen(movables[movable]);
			return;
		}
		Peg chosen = null;
		for (Peg peg : movables)
			if (chosen == null)
				chosen = peg;
			else if (peg != null
			// peg with biggest field position is chosen
					&& peg.getCurrentField() > chosen.getCurrentField())
				chosen = peg;
		pegChosen(chosen);
	}
}
