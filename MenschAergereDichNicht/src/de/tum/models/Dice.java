package de.tum.models;

import javax.microedition.khronos.opengles.GL10;

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
	/** start z for throwing the dice */
	private static final float start_z = 10 * p;
	/** angle of rotation */
	private static float angle;
	private static float basic_angle;
	/** the axis of angle of rotation */
	private static float ax;
	private static float ay;
	private static float az;
	private static float basic_ax;
	private static float basic_ay;
	private static float basic_az;
	/** true if action */
	private static boolean action;
	/** result of the dice */
	private static int result;
	/** true if moving */
	public static boolean moving;
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
		// verifying if dice is already created
		if (dice != null)
			throw new RuntimeException("second dice");
		// creating game object
		for (int i = 0; i < 6; ++i)
			sgobjects.add(new TriangleStripe(visible, vertices[i],
					null, texture, null, null, textures[i]));
		// setting position
		for (SimpleGeometricObject object : sgobjects)
			object.transfer(-side / 2, -side / 2, -side / 2);
		// updating game object info
		rotated = true;
		dice = this;
		reset();
	}
	
	/** resets the dice */
	public static final void reset() {
		dice.x = dice.y = dice.z = 0;
		dice.transfer(0, 0, side / 2);
		speed = new TupleFloat(0, 0);
	}

	/** {@inheritDoc} */
	@Override
	protected final void action() {
		if (!action)
			return;
		if (frame_current++ == 4 * frames) {
			// animation finished
			frame_current = 0;
			action = false;
			Player.diceThrown(result);
			return;
		}
		// updating game object
		float p = frame_current / 3.0f / frames;
		if (p <= 1) {
			angle = 360 * p * 4;
			p += 1;
			transfer(speed.x, speed.y, start_z * (float)
					(Math.abs(Math.cos(p * p * 4)) * (2 - p))
					+ side / 2 - z);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected final void rotate(GL10 gl) {
		gl.glRotatef(basic_angle, basic_ax, basic_ay, basic_az);
		gl.glRotatef(angle, ax, ay, az);
	}

	/**
	 * throws the dice. Calculates a random number from 1 to 6
	 * 
	 * @param team
	 *            the team of this peg
	 */
	public static final void throwIt(Team team) {
		throwIt(team, 1 + (int) (Math.random() * 6), null);
	}
	
	/**
	 * throws the dice. Calculates a number with shaking values
	 * 
	 * @param team
	 *            the team of this peg
	 * @param values
	 *            the shaking values
	 */
	public static final void throwIt(Team team, float values[]) {
		throwIt(team, 1 + (int) Math.abs(values[0] + values[1] + values[2]) % 6, null);
	}
	
	/**
	 * throws the dice. Just for animation for a network player
	 * 
	 * @param team
	 *            the team of this peg
	 * @param tokens
	 *            the tokens for the dice
	 */
	public static final void throwIt(Team team, int tokens[]) {
		throwIt(team, -1, tokens);
	}
	
	/**
	 * throws the dice
	 * 
	 * @param team
	 *            the team of this peg
	 * @param result
	 *            the result the dice shows
	 * @param tokens
	 *            the tokens for the dice
	 */
	private static final void throwIt(Team team, int result, int tokens[]) {
		TupleFloat start = null;
		if (result == -1) { // not a real move, just animation to show
			result = tokens[1];
			start = Board.getPositionForDice(team, tokens[2]);
			ax = tokens[3] / 100.0f;
			ay = tokens[4] / 100.0f;
			az = tokens[5] / 100.0f;
		}
		else { // real move, creating notification
			tokens = new int[6];
			tokens[0] = NetworkPlayer.DICE_THROWN;
			tokens[1] = result;
			start = Board.getPositionForDice(team, tokens[2] = (int) (Board.getDiceStartFieldChoices() * Math.random()));
			ax = (tokens[3] = (int) (Math.random() * 200) - 100) / 100.0f;
			ay = (tokens[4] = (int) (Math.random() * 200) - 100) / 100.0f;
			az = (tokens[5] = (int) (Math.random() * 200) - 100) / 100.0f;
			MultiplayerActivity.notifyPlayers(tokens);
		}
		Dice.result = result;
		dice.transfer(start.x - dice.x, start.y - dice.y, start_z - dice.z);
		angle = 0;
		// rotate to the correct number
		basic_az = 0x00;
		basic_angle = 0x00;
		switch (result) {
		case 5:
			basic_angle += 90;
		case 1:
			basic_angle += 90;
		case 2:
			basic_angle += 90;
			basic_ax = 0x00;
			basic_ay = 0x01;
			break;
		case 4:
			basic_angle += 180;
		case 3:
			basic_angle += 90;
			basic_ax = 0x01;
			basic_ay = 0x00;
		}
		if (!moving) {
			frame_current = frames * 3 - 1;
			speed.set(-dice.x * 2, -dice.y * 2);
		}
		else
			speed.set(-dice.x / frames / 1.5f, -dice.y / frames / 1.5f);
		action = true;
	}
	
	// just helping method for calculating textures
	private static final void calculateTextures() {
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
