package de.tum.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import de.tum.Team;
import de.tum.TupleFloat;
import de.tum.multiplayer.MultiplayerActivity;
import de.tum.player.NetworkPlayer;
import de.tum.player.Player;
import de.tum.renderable.GameObject;
import de.tum.renderable.SimpleGeometricObject;
import de.tum.renderable.Textures;
import de.tum.renderable.TriangleStripe;

/**
 * a dice can be used for generating a random number from 1 to 6. Usually there
 * is only one dice and it can be rendered of course
 */
public class Dice extends GameObject {
	/** the height of this peg */
	private static final float side = 2 * p;
	/** the vertex mesh */
	private static final float[][] vertices = {
		{ 0, 0, 0, 0, side, 0, side, 0, 0, side, side, 0},
		{ 0, 0, 0, 0, side, 0, 0, 0, side, 0, side, side},
		{ 0, side, side, side, side, side, 0, side, 0, side, side, 0},
		{ side, 0, side, 0, 0, side, side, 0, 0, 0, 0, 0},
		{ side, side, side, side, 0, side, side, side, 0, side, 0, 0},
		{ 0, 0, side, side, 0, side, 0, side, side, side, side, side}};
	/** texture values */
	private static final short[] texture = { 0, 0, 1, 0, 0, 1, 1, 1 };
	/** float array for a red dice color */
	private static float[] dice_red = { 0.5f, 0.2f, 0.2f, 1 };
	/** texture IDs */
	private static final int[] textures = new int[6];
	/** the current frame of an action */
	private static int frame_current;
	/** the current x- and y-speed */
	private static TupleFloat speed;
	/** the current z-speed */
	private static float speed_z;
	/** the current angle difference */
	private static float angle_diff;
	/** true if action */
	private static boolean action;
	/** result of the dice */
	private static int result;
	/** dice object */
	private static Dice dice;
	static {
		calculateTextures();
	}
	
	/**
	 * creating a peg
	 * 
	 * @param visible
	 *            true if visible
	 */
	public Dice(boolean visible) {
		super(visible);
		// creating game object
		for (int i = 0; i < 6; ++i)
			sgobjects.add(new TriangleStripe(visible, vertices[i],
					null, texture, null, null, textures[i]));
		// setting position
		for (SimpleGeometricObject object : sgobjects)
			object.transfer(-side / 2, -side / 2, -side / 2);
		x = y = z = 0;
		ax = ay = az = 1;
		transfer(0, 0, side / 2);
		// updating game object info
		rotated = true;
		dice = this;
		speed = new TupleFloat(0, 0);
	}

	/** {@inheritDoc} */
	@Override
	protected void action() {
		if (!action)
			return;
		if (++frame_current == 5 * frames) {
			// animation over
			frame_current = 0;
			action = false;
			Player.diceThrown(result);
			return;
		}
		// updating game object
		angle += angle_diff;
		transfer(speed.x, speed.y, speed_z);
		speed_z -= side / 8;
		if (z < side && speed_z < 0) {
			speed_z *= -0.6;
			speed.set(speed.x * 0.75f, speed.y * 0.75f);
			if (speed_z < side / 6) {
				angle_diff = 0;
				speed.set(0, 0);
				speed_z = 0;
			}
		}
	}

	/**
	 * throws the dice. Calculates a random number from 1 to 6
	 * 
	 * @param team
	 *            the team of this peg
	 */
	public static void throwIt(Team team) {
		throwIt(team, 1 + (int) (Math.random() * 6));
		MultiplayerActivity.notifyPlayers(new int[] {NetworkPlayer.DICE_THROWN, result});
	}
	
	/**
	 * throws the dice. Calculates a number with shaking values
	 * 
	 * @param team
	 *            the team of this peg
	 * @param values
	 *            the shaking values
	 */
	public static void throwIt(Team team, float values[]) {
		throwIt(team, 1 + (int) Math.abs(values[0] + values[1] + values[2]) % 6);
		MultiplayerActivity.notifyPlayers(
				new int[] {NetworkPlayer.DICE_THROWN, result});
	}
	
	/**
	 * throws the dice. Just for animation for a network player
	 * 
	 * @param team
	 *            the team of this peg
	 * @param result
	 *            the result the dice shows
	 */
	public static void throwIt(Team team, int result) {
		Dice.result = result;
		TupleFloat start = Board.getPositionForDice(team);
		dice.transfer(start.x - dice.x, start.y - dice.y, 7 * side - dice.z);
		speed.set(-dice.x / frames / 0.9f, -dice.y / frames / 0.9f);
		speed_z = 0;
		angle_diff = 15;
		action = true;
	}
	
	// just helping method for calculating textures
	private static void calculateTextures() {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		int a = 256;
		// drawing textures for the six sides
		for (int i = 0; i < 6;) {
			Bitmap bitmap = Bitmap.createBitmap(a, a, Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawRGB((int) (255 * dice_red[0]), (int) (255 * dice_red[1]),
					(int) (255 * dice_red[2]));
			paint.setARGB(255, 255, 255, 255);
			switch (++i) {
				case 6:
					canvas.drawCircle(a / 2, a / 6, a / 8, paint);
					canvas.drawCircle(a / 2, a * 5 / 6, a / 8, paint);
				case 4:
				case 5:
					canvas.drawCircle(a / 6, a / 6, a / 8, paint);
					canvas.drawCircle(a * 5 / 6, a * 5 / 6, a / 8, paint);
				case 2:
				case 3:
					canvas.drawCircle(a / 6, a * 5 / 6, a / 8, paint);
					canvas.drawCircle(a * 5 / 6, a / 6, a / 8, paint);
					if (i != 3 && i != 5)
						break;
				case 1:
					canvas.drawCircle(a / 2, a / 2, a / 8, paint);
					break;
			}
			// adding textures
			textures[i - 1] = Textures.addTexture(bitmap);
		}
	}	
}
