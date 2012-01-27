package de.tum.player;

import de.tum.Team;
import de.tum.models.Dice;
import de.tum.models.Peg;

/**
 * this player is an artificial intelligence. All moves are calculated by this
 * AI. There is no direct interaction with any human player
 */
public class AIPlayer extends Player {
	/** easy strategy means the least advanced peg is moved */
	public static final int STRATEGY_EASY = 0;
	/** medium strategy means the most advanced peg is moved */
	public static final int STRATEGY_MEDIUM = 1;
	/**
	 * hard strategy means aggressive game play if possible hit other pegs
	 * (1st humans than other AIs) - if not the most advanced peg is moved
	 */
	public static final int STRATEGY_HARD = 2;
	
	/** the strategy of this player */
	private int strategy;
	
	/**
	 * creating a player
	 * 
	 * @param team
	 *            the team of this player
	 * @param strategy
	 *            the strategy of this player
	 */
	public AIPlayer(Team team, Class<?> staticAccess, int strategy) {
		super(team, staticAccess);
		this.strategy = strategy;
	}

	/** {@inheritDoc} */
	protected void throwDice() {
		Dice.throwIt(team);
	}

	/** {@inheritDoc} */
	protected void choosePegForMove(int movable) {
		if (movable != -1) { // just one peg - no options for the player
			pegChosen(movables[movable], true);
			return;
		}
		Peg chosen = null;
		for (Peg peg : movables)
			if (chosen == null)
				chosen = peg;
			else if (peg != null)
				switch (strategy) {
				case STRATEGY_EASY:
					if (peg.getCurrentField() < chosen.getCurrentField())
						chosen = peg;
					break;
				case STRATEGY_HARD:
					if (peg.getHit() != -1) {
						if (Team.getById(peg.getHit()).isHuman()) {
							// hitting human player
							if (movable < 10 || peg.getCurrentField() > chosen.getCurrentField()) {
								chosen = peg;
								movable = 10;
							}
						} else if (movable < 9  || (movable == 9 && peg.getCurrentField() > chosen.getCurrentField())) {
							// hitting another AI
							chosen = peg;
							movable = 9;
						}
						break;
					}
				case STRATEGY_MEDIUM:
					if (peg.getCurrentField() > chosen.getCurrentField())
						chosen = peg;
				}
		pegChosen(chosen, true);
	}
}
