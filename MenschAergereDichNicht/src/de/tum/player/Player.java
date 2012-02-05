package de.tum.player;

import android.content.Context;
import android.media.MediaPlayer;
import de.tum.MenschAergereDichNichtActivity;
import de.tum.R;
import de.tum.Team;
import de.tum.models.Board;
import de.tum.models.Peg;
import de.tum.multiplayer.MultiplayerActivity;

/**
 * a player can be a human player or an artificial intelligence. Every player
 * controls one team
 */
public abstract class Player {
	/** is true if sound is enabled */
	public static boolean sound;

	/** the current dice number */
	private static int number;

	/** the current player */
	protected static Player player;

	/** the players */
	protected static final Player players[] = new Player[4];

	/** the current movable pegs for the current player */
	protected static final Peg[] movables = new Peg[Board.start_pegs];

	/** the team of this player */
	protected Team team;

	/** is true if a player has won */
	private boolean won;

	/** the current try in a round */
	private int current_try;

	/** is true if the dice's number is a six at the 1st try in a player's turn */
	private boolean is1st6;

	/** the pegs of this player */
	private Peg[] pegs;
	
	/** when a player win play sound */
	private MediaPlayer mp;

	/**
	 * creating a player
	 * 
	 * @param team
	 *            the team of this player
	 */
	public Player(Team team) {
		this.team = team;
		team.setHuman(this instanceof HumanPlayer);
		pegs = Board.getPegs(team);
		for (Peg peg : pegs)
			peg.setVisible(true);
		players[team.id] = player = this;
	}

	/** removing all players and resetting the pegs */
	public static final void removePlayers() {
		for (int i = 0; i < players.length; ++i) {
			for (int u = 0; u < players[i].pegs.length; ++u)
				players[i].pegs[u].setVisible(false);
			players[i] = null;
		}
		player = null;
	}

	/**
	 * resetting this player. All pegs of this player are reset.
	 */
	public final void reset() {
		won = false;
		current_try = 0;
		for (Peg peg : pegs)
			peg.reset();
	}

	/**
	 * it's this player's turn. If the player hasn't won, the player is asked to
	 * throw the dice
	 */
	private final void makeTurn() {
		player = this;
		++current_try;
		// already won
		if (won)
			nextTurn();
		else
			throwDice();
	}

	/** the player is asked to throw the dice */
	protected abstract void throwDice();

	/**
	 * dice was thrown. If there is only one peg movable according o the dice
	 * number, than that peg is chosen. Otherwise the choosePegForMove()-method
	 * will be called in order to select a peg
	 * 
	 * @param number
	 *            the number the dice shows
	 */
	public static final void diceThrown(int number) {
		Player.number = number;
		int movable = 0, index = 0, found1st = -1;
		for (Peg peg : player.pegs) // calculates which pegs can be moved
			if (peg.checkMove(number)) {
				++movable;
				if (found1st == -1)
					found1st = index;
				movables[index++] = peg;
				if (!peg.hasStarted()) { // peg has to move out
					movable = 1;
					found1st = --index;
					if (player.current_try == 1)
						player.is1st6 = true;
					else if (player.current_try == 3 && !player.is1st6)
						player.current_try = 2;
					break;
				}
			} else
				movables[index++] = null;
		if (movable == 0) // no peg can be moved
			player.checkForMoreTurns();
		else // just one peg - no options for the player or
			//player has to select one of two or more pegs
			player.choosePegForMove(movable == 1 ? found1st : -1);
		MultiplayerActivity.tokenDone();
	}

	// checks whether this player can throw again. E.g. when a player has all
	// pegs on start, the player is allowed to throw three times
	private final void checkForMoreTurns() {
		if (!won && current_try < 3 && (number == 6 || !checkForOneFieldMove()))
			makeTurn();
		else {
			current_try = 0;
			is1st6 = false;
			nextTurn();
		}
	}

	// checks whether any peg can move one field. Than the player is not allowed
	// to get another try
	private final boolean checkForOneFieldMove() {
		for (Peg peg : pegs)
			if (peg.checkMove(1))
				return true;
		return false;
	}

	/**
	 * player has to choose a peg for moving
	 * 
	 * @param movable
	 *            the current movable peg(s)
	 */
	protected abstract void choosePegForMove(int movable);

	/**
	 * peg is chosen
	 * 
	 * @param peg
	 *            the peg to move
	 * @param notify
	 *            true if player has to notify other players
	 */
	protected final void pegChosen(Peg peg, boolean notify) {
		if (notify)
			MultiplayerActivity.notifyPlayers(
					new int[] {NetworkPlayer.PEG_MOVED, peg.pos_start});
		peg.move();
	}
	
	/** peg was moved */
	public static final void pegMoved() {
		player.verifyWinner();
		player.checkForMoreTurns();
		MultiplayerActivity.tokenDone();
	}

	// just to verify if the player has won
	private final void verifyWinner() {
		for (Peg peg : pegs)
			if (!peg.hasFinished())
				return;
		won = true;
		// player has won - a sound is played
		if (sound) {
			Context context;
			if (MenschAergereDichNichtActivity.getContext() == null)
				context = MultiplayerActivity.getActivity();
			else
				context = MenschAergereDichNichtActivity.getContext();
	
		    mp = MediaPlayer.create(context, R.raw.tada);
		    mp.setLooping(false);
			mp.start();
		}

	    // notification is done
		if (this instanceof HumanPlayer)
			MultiplayerActivity.showToast(R.string.you_won);
		else
			switch(team.id) {
			case 0:
				MultiplayerActivity.showToast(R.string.won_red);
				break;
			case 1:
				MultiplayerActivity.showToast(R.string.won_yellow);
				break;
			case 2:
				MultiplayerActivity.showToast(R.string.won_green);
				break;
			case 3:
				MultiplayerActivity.showToast(R.string.won_blue);
			}
	}
	
	/**
	 * it's the next player's turn. Calls the next player for its turn
	 */
    public static final void nextTurn() {
    	Team team = player.team;
    	Player player = null;
    	int count = 0;
    	while(player == null || player.won) {
    		if (count == players.length) {// game over
    			MultiplayerActivity.showToast(R.string.game_over);
    			return;
    		}
    		team = Team.getById((team.id + 1) % players.length);
    		player = players[team.id];
    		++count;
    	}
    	player.makeTurn();
    }
}
