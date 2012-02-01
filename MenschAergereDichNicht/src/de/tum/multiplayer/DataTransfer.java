package de.tum.multiplayer;

import java.io.Serializable;

public class DataTransfer implements Serializable {
	private static final long serialVersionUID = 1L;

	/** notification for players */
	public static final transient int IS_NOTIFICATION = 0;
	public static final transient int SETUP_GAME = 1;
	public static final transient int CONNECTION_STATUS = 2; 
	
	public final int reason;
	public final int[] tokens;
	
	public DataTransfer(int reason, int[] tokens) {
		this.reason = reason;
		this.tokens = tokens;
	}
}
