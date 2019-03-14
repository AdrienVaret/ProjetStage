package utils;

import bincsp.Couple;
import sat.Litteral;

public class Cause {

	private GenericCouple<Litteral> couple;
	private int level;
	
	public Cause(GenericCouple<Litteral> couple, int level) {
		this.couple = couple;
		this.level = level;
	}
	
	public GenericCouple<Litteral> getCouple(){
		return couple;
	}
	
	public int getLevel() {
		return level;
	}
	
	public String toString() {
		return "<" + couple.toString() + ", " + Integer.toString(level) + ">";
	}
	
}
