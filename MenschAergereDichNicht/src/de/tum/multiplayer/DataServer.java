package de.tum.multiplayer;

import java.io.Serializable;

import de.tum.models.Board;
import de.tum.player.Player;

public class DataServer implements Serializable{
	
	public Board board;
	public Player[] player;
	
	public DataServer(){
		
	}

}
