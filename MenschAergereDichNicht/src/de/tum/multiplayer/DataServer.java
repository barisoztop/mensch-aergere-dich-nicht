package de.tum.multiplayer;

import java.io.Serializable;

public class DataServer implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public final int[] tokens;
	
	public DataServer(int[] tokens){
		this.tokens = tokens;
	}
}
