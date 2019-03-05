package solver_sat;

import java.util.ArrayList;

import sat.Litteral;

public class Result {
	private ArrayList<Litteral> litterals;
	private boolean state;
	
	public Result(ArrayList<Litteral> litterals, boolean state) {
		super();
		this.litterals = litterals;
		this.state = state;
	}

	public ArrayList<Litteral> getLitterals() {
		return litterals;
	}

	public boolean getState() {
		return state;
	}
	
	@Override
	public String toString() {
		String state;
		if (this.state)
			state = "T";
		else 
			state = "F";
		String str = "[" + state + "][";
		for (Litteral litteral : litterals) {
			str += litteral.toString() + ", ";
		}
		str += "]";
		return str;
	}
}
