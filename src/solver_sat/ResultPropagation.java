package solver_sat;

public class ResultPropagation {

	private boolean state;
	private int [] litteralsStates;
	
	public ResultPropagation(int size) {
		state = false;
		litteralsStates = new int[size];
	}
	
	public boolean state() {
		return state;
	}
	
	public void setState(boolean state) {
		this.state = state;
	}
	
	public int get(int index) {
		return litteralsStates[index];
	}
	
	public void incr(int index) {
		litteralsStates[index] ++;
	}
	
	public void clear() {
		state = false;
		for (int index = 0 ; index < litteralsStates.length ; index ++) {
			litteralsStates[index] = 0;
		}
	}
	
	@Override
	public String toString() {
		String string = "";
		if (state) string += "[T][";
		else string += "[F][";
		for (int index = 0 ; index < litteralsStates.length ; index ++) {
			string += litteralsStates[index] + ", ";
		}
		string += "]";
		return string;
	}
}
