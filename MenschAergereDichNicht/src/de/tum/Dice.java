package de.tum;

/**
 * a dice can be used for generating a random number from 1 to 6. Usually there
 * is only one dice and it can be rendered of course
 */
public class Dice {
	
	/**
	 * throws the dice. Calculates a random number from 1 to 6
	 */	
	public static void throwIt() {
		Player.diceThrown(1 + (int) (Math.random() * 5));
	}
}
