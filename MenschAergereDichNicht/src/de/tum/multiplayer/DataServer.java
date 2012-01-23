package de.tum.multiplayer;

import java.io.Serializable;

import de.tum.Room;
import de.tum.player.Player;

public class DataServer implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String message;
	
	public DataServer(String message){
		this.message = message;
	}
}
