package de.tum.player;

import android.util.Log;
import de.tum.Team;
import de.tum.models.Dice;

/**
 * this player is a network player. It is always waiting for notifications
 */
public class NetworkPlayer extends Player {
	/** notification for dice thrown */
	public static final int DICE_THROWN = 0;
	/** notification for peg moved */
	public static final int PEG_MOVED = 1;
	/** saving the first token in case, that game start is late */
	private static int firstToken[];
	
	/**
	 * creating a player
	 * 
	 * @param team
	 *            the team of this player
	 */
	public NetworkPlayer(Team team) {
		super(team);
	}

	/** {@inheritDoc} */
	@Override
	protected void throwDice() {
		if (firstToken != null) {
			notify(firstToken);
			firstToken = null;
		}
		// waiting for notification
		return;
	}

	/** {@inheritDoc} */
	@Override
	protected void choosePegForMove(int movable) {
		// waiting for notification
		return;
	}
	
	/**
	 * getting a notification
	 * 
	 * @param tokens
	 *            the notification
	 */
	public static void notify(int[] tokens) {
		if (player == null) { // storing token for game start
			firstToken = tokens;
			return;
		}
		switch (tokens[0]) {
		case NetworkPlayer.DICE_THROWN:
			Log.d("notify", "player=" + player + "; ");
			Dice.throwIt(player.team, tokens);
			break;
		case NetworkPlayer.PEG_MOVED:
			player.pegChosen(movables[tokens[1]], false);
		}
	}
}
