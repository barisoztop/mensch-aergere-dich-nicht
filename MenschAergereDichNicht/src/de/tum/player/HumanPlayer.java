package de.tum.player;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import de.tum.GameListener;
import de.tum.MenschAergereDichNichtActivity;
import de.tum.Team;
import de.tum.models.Dice;

/**
 * this player is a human player. All moves are chosen by a human
 */
public class HumanPlayer extends Player {
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
		this(team, null);
	}

	public HumanPlayer(Team team, Handler handler) {
		super(team);
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
		GameListener.waitForInput(this, GameListener.waitingForDice);
	}

	/** {@inheritDoc} */
	protected void choosePegForMove(int movable) {
		this.movable = movable;
		if (movable != -1)
			movables[this.peg = movable].setSelection(true);
		else {
			for (int i = 0; i < movables.length; ++i)
				if (movables[i] != null) {
					movables[this.peg = i].setSelection(true);
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
		GameListener.waitForInput(this, GameListener.waitingForPegSelection);
	}

	public final void waitedForInput(int waitingFor, float values[]) {
		x: switch (waitingFor) {
		case GameListener.waitingForDice:
			GameListener.stopWaiting();
			if (values == null)
			  Dice.throwIt(team);
			else
				Dice.throwIt(team, values);
			break;
		case GameListener.waitingForPegSelection:
			// Log.d("touch", "tap");
			if (movable != -1)
				break;
			// Log.d("touch", "peg=" + peg);
			// Log.d("touch", "selecting next peg");
			movables[peg].setSelection(false);
			for (int i = 1; i < movables.length; ++i)
				if (movables[(peg + i) % movables.length] != null) {
					movables[peg = (peg + i) % movables.length].setSelection(true);
					break x;
				}
		case GameListener.waitingForPegChosen:
			GameListener.stopWaiting();
			movables[peg].setSelection(false);
			pegChosen(movables[peg], true);
			break;
		case GameListener.waitingTimeOut:
	        Message msg = mHandler.obtainMessage(MenschAergereDichNichtActivity.MESSAGE_TOAST);
	        Bundle bundle = new Bundle();
	        bundle.putString(MenschAergereDichNichtActivity.TOAST, "hurry up !");
	        msg.setData(bundle);
	        mHandler.sendMessage(msg);			
		}
	}
}
