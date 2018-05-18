package controller;

import utilities.Coordinate;

public class TrueCoord {
	public float x;
	public float y;
	
	
	public TrueCoord(Coordinate c) {
		this.x = c.x+0.5f;
		this.y = c.y+0.5f;
	}
	
	public TrueCoord(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public boolean equals(TrueCoord input) {
		return (Float.compare(input.x, this.x) == 0 && Float.compare(input.y, this.y) == 0);
	}
	
	public boolean approxEquals(TrueCoord input) {
		return (Math.abs(input.x - this.x) <= 0.50f && 
				Math.abs(input.y - this.y) <= 0.50f);
	}

}
