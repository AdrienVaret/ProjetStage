package solver_sat;

import java.util.ArrayList;
import java.util.Arrays;

import sat.Litteral;

public class Result2 {

	private boolean state;
	private int [] litterals;
	private int count;
	
	public Result2(int nbVariables) {
		litterals = new int [2 * nbVariables];
		count = 0;
	}
	
	public int [] getLitterals() {
		return litterals;
	}
	
	public boolean getState() {
		return state;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setState(boolean state) {
		this.state = state;
	}
	
	public void clear() {
		count = 0;
		for (int index = 0 ; index < litterals.length ; index ++)
			litterals[index] = 0;
	}
	
	public void incr() {
		count ++;
	}
	
	@Override
	public String toString() {
		String s;
		if (state) s = "[T] [";
		else s = "[F] [";
		for (int i = 0 ; i < litterals.length ; i++) {
			s += litterals[i] + ", ";
		}
		s += "]";
		return s;
	}
	
	public static void main (String [] args) {
		Litteral [] [] T = new Litteral [5][5];
		T [0][0] = null;
		System.out.println("");
	}
}
