package de.tum.player;

import de.tum.GameTouchListener;
import de.tum.Team;
import de.tum.models.Dice;
import de.tum.models.Peg;

/**
 * this player is a human player. All moves are chosen by a human
 */
public class HumanPlayer extends Player {
	private Peg[] pegs;
	private int peg;
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
//		MenschAergereDichNichtActivity.showMessage("touch to throw the dice", false);
		GameTouchListener.waitForInput(this, GameTouchListener.waitingForDice);
	}

	/** {@inheritDoc} */
	protected void choosePegForMove(Peg[] movables, int movable) {
//	Log.d("human player", "movable=" + movable);
		pegs = movables;
		if (movable != -1)
			pegs[this.peg = movable].setSelection(true);
		else
			for (int i = 0; i < pegs.length; ++i)
				if (pegs[i] != null) {
					pegs[this.peg = i].setSelection(true);
					break;
				}
//		MenschAergereDichNichtActivity.showMessage("touch to select next peg", false);
		GameTouchListener.waitForInput(this, GameTouchListener.waitingForPegSelection);
	}
	
	public final void waitedForInput(int waitingFor) {
		switch (waitingFor) {
		case GameTouchListener.waitingForDice:
			GameTouchListener.stopWaiting();
			Dice.throwIt(team);
			break;
		case GameTouchListener.waitingForPegSelection:
			if (peg != -1)
				break;
			pegs[peg].setSelection(false);
			for (int i = 0; i < pegs.length; ++i)
				if (pegs[(peg + i) % pegs.length] != null) {
					pegs[peg = i].setSelection(true);
					break;
				}
		case GameTouchListener.waitingForPegChosen:
			GameTouchListener.stopWaiting();
			pegs[peg].setSelection(false);
			pegChosen(pegs[peg]);
		}
	}
}
