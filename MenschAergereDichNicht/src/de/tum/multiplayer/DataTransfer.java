package de.tum.multiplayer;

import java.io.Serializable;

/** the serializable object makes data transfer easy */
public class DataTransfer implements Serializable {
	private static final long serialVersionUID = 1L;

	/** notification for players (reasons) */
	public static final transient int IS_NOTIFICATION = 0;
	public static final transient int SETUP_GAME = 1;
	public static final transient int STATUS_WAITING = 2;
	public static final transient int STATUS_SETTINGS = 3;

	/** reason why this data was sent */
	public final int reason;
	/** the content of this data */
	public final int[] tokens;

	/**
	 * creating the data transfer
	 * 
	 * @param reason
	 *            the reason for this transfer
	 * @param tokens
	 *            the content
	 */
	public DataTransfer(int reason, int[] tokens) {
		this.reason = reason;
		this.tokens = tokens;
	}
}
