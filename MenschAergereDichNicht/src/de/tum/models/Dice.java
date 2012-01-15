package de.tum.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import de.tum.Team;
import de.tum.TupleFloat;
import de.tum.player.Player;
import de.tum.renderable.GameObject;
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
	private static float[] dice_red = { 0.7f, 0.2f, 0.2f, 1 };
	/** texture IDs */
	private static final int[] textures = new int[6];
	/** the current frame of an action */
	private static int frame_current;
	/** true if action */
	private static boolean action;
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
		for (int i = 0; i < 6; ++i)
			sgobjects.add(new TriangleStripe(visible, vertices[i],
					null, texture, textures[i]));
		transfer(-side / 2, -side / 2, -side / 2);
		x = y = z = 0;
		ax = ay = az = 1;
		poppushtranslationrotation = true;
		transfer(0, 0, side / 2);
		dice = this;
	}

	/** {@inheritDoc} */
	@Override
	protected void action() {
		if (!action)
			return;
		if (++frame_current == frames * 3) {
			frame_current = 0;
			action = false;
			Player.diceThrown(1 + (int) (Math.random() * 6));
			return;
		}
		angle += 5;
	}

	/**
	 * throws the dice. Calculates a random number from 1 to 6
	 * 
	 * @param team
	 *            the team of this peg
	 */
	public static void throwIt(Team team) {
		action = true;
		TupleFloat start = Board.getPositionForDice(team);
		dice.transfer(start.x - dice.x, start.y - dice.y, 0);
	}
	
	// just helping method for calculating textures
	private static void calculateTextures() {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		for (int i = 0; i < 6;) {
			Bitmap bitmap = Bitmap.createBitmap(64, 64, Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawRGB((int) (255 * dice_red[0]), (int) (255 * dice_red[1]),
					(int) (255 * dice_red[2]));
			paint.setARGB(255, 255, 255, 255);
			switch (++i) {
				case 6:
					canvas.drawCircle(32, 11, 8, paint);
					canvas.drawCircle(32, 53, 8, paint);
				case 4:
				case 5:
					canvas.drawCircle(11, 11, 8, paint);
					canvas.drawCircle(53, 53, 8, paint);
				case 2:
				case 3:
					canvas.drawCircle(11, 53, 8, paint);
					canvas.drawCircle(53, 11, 8, paint);
					if (i != 3 && i != 5)
						break;
				case 1:
					canvas.drawCircle(32, 32, 8, paint);
					break;
			}
			textures[i - 1] = Textures.addTexture(bitmap);
		}
	}	
}
