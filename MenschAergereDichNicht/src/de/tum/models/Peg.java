package de.tum.models;

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
	/** the amount of frames a peg needs to reach the next field */
	private static final int frames = 20;
	/** the current frame of a move */
	private int frame_current;
	/** the final position where the peg starts */
	private final int pos_start;
	/** the final team of this peg */
	private final Team team;
	/** the current position of this peg */
	private int pos_current;
	/** the next position of this peg */
	private TupleFloat pos_next;
	/** the offset for the current move */
	private final TupleFloat pos_offset;
	/** the final possible position of this peg */
	private int pos_final;
	/** true if moving */
	private boolean moving;

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
		if (moving)
			if (frames == frame_current++) {
				frame_current = 0;
				if (pos_current < pos_final) {
//					getting the next coordinates
					pos_next = Board.getPosition(this, pos_final == Board.start_pegs ? pos_current = pos_final : ++pos_current, pos_current == pos_final);
//					calculating difference
					pos_offset.set((pos_next.x - x) / frames, (pos_next.y - y) / frames);
//					resetting the current frame
				}
				else if (pos_final == 0) {
					pos_offset.set(pos_offset.x * -1, pos_offset.y * -1);
					pos_final = -1;
				}
				else {
					moving = false;
					if (pos_final > 0)
					  Player.pegMoved();					
				}
			}
			else
//				moving this peg
				transfer(pos_offset.x, pos_offset.y, 0);
	}

	/** method for moving the peg */
	public final void move() {
		moving = true;
		frame_current = frames;
		pos_next.set(x, y);
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
		moving = true;
		pos_final = 0;
		pos_offset.set(dx / frames, dy / frames);
	}

	/**
	 * resetting this peg will move it back to its start position
	 */
	public final void reset() {
		pos_current = pos_start;
		// getting the coordinates
		TupleFloat position = Board.getPosition(this, pos_current, true);
		// moving this peg
		transfer(position.x - x, position.y - y, 0);
	}

	/**
	 * checking whether this peg can move
	 * 
	 * @param fields
	 *            the amount of fields to move
	 * @return true if this peg can move
	 */
	public final boolean checkMove(int fields) {
		return (pos_final = Board.getPositionNext(team, pos_current, fields)) != -1;
	}

	/**
	 * getting the current field on the board
	 * 
	 * @return the current field
	 */
	public final int getCurrentField() {
		return pos_current;
	}

	/**
	 * getting the team
	 * 
	 * @return the peg's team
	 */
	public final Team getTeam() {
		return team;
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
