package de.tum.multiplayer;

import java.io.Serializable;

public class DataClient implements Serializable{
	
	private String same;

	public DataClient(String same) {
		this.same = same;
	}
	
	public void setValue(String same) {
		this.same = same;
	}
	
	public String getValue() {
		return same;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
