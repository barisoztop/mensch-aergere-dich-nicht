package de.tum.models;

import android.util.Log;
import de.tum.Team;
import de.tum.TupleFloat;
import de.tum.player.Player;
import de.tum.renderable.GameObject;

/**
 * a peg is a game object that moves on the board. Every player has typically
 * four pegs and every peg has the player's team color
 */
public abstract class Peg extends GameObject {
	/** the final bottom layer for pegs has his z-coordinate at 0.1 */
	protected static final float bottom = 0.01f * p;
	/** the current frame of an action */
	private int frame_current;
	/** the final position where the peg starts */
	public final int pos_start;
	/** the final team of this peg */
	public final Team team;
	/** the current position of this peg */
	private int pos_current;
	/** the next position of this peg */
	private TupleFloat pos_next;
	/** the offset for the current move */
	private final TupleFloat pos_offset;
	/** the z-offset for the current move */
	private float pos_offset_z;
	/** the final possible position of this peg */
	private int pos_final;
	/** true if action */
	private boolean action;
	/** true if moving */
	public static boolean moving;

	/**
	 * creating a peg
	 * 
	 * @param visible
	 *            true if visible
	 * @param team
	 *            the team of this peg
	 * @param pos_start
	 *            the start position for this peg
	 */
	public Peg(boolean visible, Team team, int pos_start) {
		super(visible);
		this.team = team;
		this.pos_start = pos_current = pos_start;
		pos_next = new TupleFloat(0, 0);
		pos_offset = new TupleFloat(0, 0);
	}

	/** {@inheritDoc} */
	@Override
	protected final void action() {
		if (!action)
			return;
		if (frames == frame_current++) {
			frame_current = 0;
			if (pos_current < pos_final) {
//					getting the next coordinates
				pos_next = Board.getPosition(this, pos_final == Board.start_pegs || !moving ? pos_current = pos_final : ++pos_current, pos_current == pos_final);
//					calculating difference
				if (moving) // show move
					pos_offset.set((pos_next.x - x) / frames, (pos_next.y - y) / frames);
				else { // no moves
					pos_offset.set(pos_next.x - x, pos_next.y - y);
					frame_current = frames - 1;
				}
			}
			else if (pos_final == -1) {
				pos_offset.set(pos_offset.x * -1, pos_offset.y * -1);
				pos_final = -2;
			}
			else {
				action = false;
				if (pos_final > 0)
				  Player.pegMoved();					
			}
		} else {
			// moving this peg
			if (pos_final == -3) // just jumping to reset
				if (frame_current == frames)
					pos_offset_z = 0;
				else
					pos_offset_z -= 2 * p / frames;
			transfer(pos_offset.x, pos_offset.y, pos_offset_z);
		}
	}

	/** method for moving the peg */
	public final void move() {
		if (action)	
			Log.d("peg",
					"move: already moving ######	################!!!!+++++++++++++++++++++++");
		frame_current = frames;
		pos_next.set(x, y);
		pos_offset_z = 0x00;
		action = true;
	}

	/**
	 * method for moving the peg away
	 * 
	 * @param dx
	 *            the offset in x-direction
	 * @param dy
	 *            the offset in y-direction
	 */
	public final void giveWay(float dx, float dy) {
		if (action)	
			Log.d("peg",
					"giveWay: already moving ######	################!!!!+++++++++++++++++++++++");
		action = true;
		pos_final = -1;
		pos_offset.set(dx / frames, dy / frames);
		pos_offset_z = 0x00;
	}

	/**
	 * resetting this peg will move it back to its start position
	 */
	public final void reset() {
		if (action)	
			Log.d("peg",
					"reset: already moving ######	################!!!!+++++++++++++++++++++++");
		action = true;
		pos_current = pos_start;
//		// getting the coordinates
		pos_next = Board.getPosition(this, pos_current, true);
		if (moving) { // moves are shown
			pos_offset.set((pos_next.x - x) / frames, (pos_next.y - y) / frames);
			pos_offset_z = p;
		}
		else { // moves are skipped
			pos_offset.set(pos_next.x - x, pos_next.y - y);
			frame_current = frames - 1;
		}
		pos_final = -3;
	}

	/**
	 * checking whether this peg can move
	 * 
	 * @param fields
	 *            the amount of fields to move
	 * @return true if this peg can move
	 */
	public final boolean checkMove(int fields) {
		pos_final = (int) (pos_next = Board.getPositionNext(team, pos_current, fields)).x;
		return pos_next.y != -2;
	}
	
	/**
	 * checking whether this peg hits another peg
	 * 
	 * @return -1 if the final field is empty or the team id of the field's peg
	 */
	public final int getHit() {
		return (int) pos_next.y;
	}

	/**
	 * selecting this peg
	 * 
	 * @param selected
	 *            true if selected
	 */
	public abstract void setSelection(boolean selected);

	/**
	 * getting the current field on the board
	 * 
	 * @return the current field
	 */
	public final int getCurrentField() {
		return pos_current;
	}

	/**
	 * getting whether this peg is already in a finish field
	 * 
	 * @return true if this peg is in a finish field
	 */
	public final boolean hasFinished() {
		return pos_current >= Board.path_length + Board.start_pegs;
	}

	/**
	 * getting whether this peg has already started
	 * 
	 * @return true if this peg has already started
	 */
	public final boolean hasStarted() {
		return pos_current >= Board.start_pegs;
	}
}
