package de.tum.player;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import de.tum.GameTouchListener;
import de.tum.MenschAergereDichNichtActivity;
import de.tum.Team;
import de.tum.models.Dice;
import de.tum.models.Peg;

/**
 * this player is a human player. All moves are chosen by a human
 */
public class HumanPlayer extends Player {
	private Peg[] pegs;
	private int movable;
	private int peg;
	private final Handler mHandler;

	/**
	 * creating a player
	 * 
	 * @param team
	 *            the team of this player
	 */
	public HumanPlayer(Team team) {
		super(team);
		mHandler = null;
	}

	public HumanPlayer(Team team, Handler handler) {
		super(team);
		mHandler = handler;
	}
	
	public HumanPlayer(Team team, Handler handler, Class<?> staticAccess) {
		super(team, staticAccess);
		mHandler = handler;
	}	

	/** {@inheritDoc} */
	protected void throwDice() {
//		MenschAergereDichNichtActivity.showMessage("touch to throw the dice");
		/*Show the message at the title bar*/
//		Message msg = mHandler.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TITLE);
//		Bundle bundle = new Bundle();
//		bundle.putString(MenschAergereDichNichtActivity.TITLE, "touch to throw the dice");
//		msg.setData(bundle);
//		mHandler.sendMessage(msg);
		/*Toast the message*/
        Message msg = mHandler.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MenschAergereDichNichtActivity.TOAST, "touch to throw the dice");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
		GameTouchListener.waitForInput(this, GameTouchListener.waitingForDice);
	}

	/** {@inheritDoc} */
	protected void choosePegForMove(Peg[] movables, int movable) {
		this.movable = movable;
		pegs = movables;
		if (movable != -1)
			pegs[this.peg = movable].setSelection(true);
		else {
			for (int i = 0; i < pegs.length; ++i)
				if (pegs[i] != null) {
					pegs[this.peg = i].setSelection(true);
					break;
				}
//			MenschAergereDichNichtActivity.showMessage("touch to select next peg");
			/*Show the message at the title bar*/
//			Message msg = mHandler.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TITLE);
//			Bundle bundle = new Bundle();
//			bundle.putString(MenschAergereDichNichtActivity.TITLE, "touch to select next peg");
//			msg.setData(bundle);
//			mHandler.sendMessage(msg);
			/*Toast the message*/
	        Message msg = mHandler.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TOAST);
	        Bundle bundle = new Bundle();
	        bundle.putString(MenschAergereDichNichtActivity.TOAST, "touch to select next peg");
	        msg.setData(bundle);
	        mHandler.sendMessage(msg);
		}
		GameTouchListener.waitForInput(this, GameTouchListener.waitingForPegSelection);
	}

	public final void waitedForInput(int waitingFor) {
		x: switch (waitingFor) {
		case GameTouchListener.waitingForDice:
			GameTouchListener.stopWaiting();
			Dice.throwIt(team);
			break;
		case GameTouchListener.waitingForPegSelection:
			// Log.d("touch", "tap");
			if (movable != -1)
				break;
			// Log.d("touch", "peg=" + peg);
			// Log.d("touch", "selecting next peg");
			pegs[peg].setSelection(false);
			for (int i = 1; i < pegs.length; ++i)
				if (pegs[(peg + i) % pegs.length] != null) {
					pegs[peg = (peg + i) % pegs.length].setSelection(true);
					break x;
				}
		case GameTouchListener.waitingForPegChosen:
			GameTouchListener.stopWaiting();
			pegs[peg].setSelection(false);
			pegChosen(pegs[peg]);
		}
	}
}
