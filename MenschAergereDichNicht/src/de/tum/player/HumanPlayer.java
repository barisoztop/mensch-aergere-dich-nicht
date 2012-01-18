package de.tum.player;

import de.tum.GameTouchListener;
import de.tum.MenschAergereDichNichtActivity;
import de.tum.Team;
import de.tum.models.Dice;
import de.tum.models.Peg;

/**
 * this player is a human player. All moves are chosen by a human
 */
public class HumanPlayer extends Player {
	private Peg chosen;
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
		MenschAergereDichNichtActivity.showMessage("touch to throw the dice", false);
		GameTouchListener.waitForInput(this, GameTouchListener.waitingForDice);
	}

	/** {@inheritDoc} */
	protected void choosePegForMove(Peg[] movables, int movable) {
//	Log.d("human player", "movable=" + movable);
		for (Peg peg : movables)
			if (peg != null) {
				(chosen = peg).setSelection(true);
				break;
			}
		MenschAergereDichNichtActivity.showMessage("touch to select next peg", false);
		GameTouchListener.waitForInput(this, GameTouchListener.waitingForPegSelected);
	}
	
	public final void waitedForInput(int waitingFor) {
		switch (waitingFor) {
		case GameTouchListener.waitingForDice:
			GameTouchListener.stopWaiting();
			Dice.throwIt(team);
			break;
		case GameTouchListener.waitingForPegSelected:
			GameTouchListener.stopWaiting();
			pegChosen(chosen);
		}
	}
}
