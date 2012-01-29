package de.tum.player;

import de.tum.GameListener;
import de.tum.R;
import de.tum.Team;
import de.tum.models.Dice;
import de.tum.multiplayer.MultiplayerActivity;

/**
 * this player is a human player. All moves are chosen by a human
 */
public class HumanPlayer extends Player {
	/** current selected peg */
	private int peg;
	/** movable pegs */
	private int movable;

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
		// show message
		MultiplayerActivity.showToast(R.string.throw_dice);
		// waiting for a touch event or a shaking input
		GameListener.waitForInput(this, GameListener.waitingForDice);
	}

	/** {@inheritDoc} */
	protected void choosePegForMove(int movable) {
		this.movable = movable;
		if (movable != -1) // just one possible peg
			movables[this.peg = movable].setSelection(true);
		else {
			// select 1st peg
			for (int i = 0; i < movables.length; ++i)
				if (movables[i] != null) {
					movables[this.peg = i].setSelection(true);
					break;
				}
			// show message
			MultiplayerActivity.showToast(R.string.select_peg);
		}
		// waiting for a touch event
		GameListener.waitForInput(this, GameListener.waitingForPegSelection);
	}

	/**
	 * input from a listener
	 * 
	 * @param waitingFor
	 *            the reason for the input
	 * @param values
	 *            additional input values
	 */
	public final void waitedForInput(int waitingFor, float values[]) {
		x: switch (waitingFor) {
		case GameListener.waitingForDice:
			GameListener.stopWaiting();
			// throw the dice
			if (values == null)
			  Dice.throwIt(team);
			else
				Dice.throwIt(team, values);
			break;
		case GameListener.waitingForPegSelection:
			if (movable != -1)
				break;
			// select next peg
			movables[peg].setSelection(false);
			for (int i = 1; i < movables.length; ++i)
				if (movables[(peg + i) % movables.length] != null) {
					movables[peg = (peg + i) % movables.length].setSelection(true);
					break x;
				}
		case GameListener.waitingForPegChosen:
			// peg chosen
			GameListener.stopWaiting();
			movables[peg].setSelection(false);
			pegChosen(movables[peg], true);
			break;
		case GameListener.waitingTimeOut:
			// show message and hurry up
			MultiplayerActivity.showToast(R.string.hurry);
		}
	}
}
