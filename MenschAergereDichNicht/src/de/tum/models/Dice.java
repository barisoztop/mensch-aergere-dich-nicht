package de.tum.models;

import de.tum.player.Player;

/**
 * a dice can be used for generating a random number from 1 to 6. Usually there
 * is only one dice and it can be rendered of course
 */
public class Dice {
	
	/**
	 * throws the dice. Calculates a random number from 1 to 6
	 */	
	public static void throwIt() {
		Player.diceThrown(
				//index++ < x.length ? x[index - 1] : 2);
		1 + (int) (Math.random() * 6));
	}
	//private static int[] x = {6, 6, 5, 6};static int index = 0;
}
