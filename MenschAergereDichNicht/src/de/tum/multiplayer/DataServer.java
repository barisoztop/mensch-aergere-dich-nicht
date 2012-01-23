package de.tum.multiplayer;

import java.io.Serializable;

import de.tum.Room;
import de.tum.player.Player;

public class DataServer implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Player[] player;
	private String message;
	public Room room;
	
	public DataServer(String message, Player[] player){
		this.message = message;
		this.player = player;
	}

	public DataServer(Room room) {
		this.room = room;

	}

}
