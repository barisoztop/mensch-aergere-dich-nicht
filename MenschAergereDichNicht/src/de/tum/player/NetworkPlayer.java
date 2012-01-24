package de.tum.player;

import android.util.Log;
import de.tum.Team;
import de.tum.models.Dice;
import de.tum.models.Peg;

public class NetworkPlayer extends Player {
	public static final int DICE_THROWN = 0;
	public static final int PEG_MOVED = 1;
	
	public NetworkPlayer(Team team, Class<?> staticAccess) {
		super(team, staticAccess);
	}

	@Override
	protected void throwDice() {
		return;
	}

	@Override
	protected void choosePegForMove(int movable) {
		Log.d("networkplayer", "choose for move");
		Log.d("networkplayer", "movable");
		int i = 0;
		for (Peg peg : movables)
		  Log.d("networkplayer", i + ": " + peg);
		return;
	}
	
	public static void notify(int[] tokens) {
		switch (tokens[0]) {
		case NetworkPlayer.DICE_THROWN:
			Dice.throwIt(player.team, tokens[1]);
			break;
		case NetworkPlayer.PEG_MOVED:
			  Log.d("networkplayer notify", "tokens[1]=" + tokens[1]);
			player.pegChosen(movables[tokens[1]], false);
		}
	}
}
