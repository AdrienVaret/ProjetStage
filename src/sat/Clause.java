package sat;

import java.util.ArrayList;

public class Clause {

	private int id;
	private Litteral [] litterals;
	
	public Clause(int id, ArrayList<Litteral> litterals) {
		this.id = id;
		this.litterals = new Litteral[litterals.size()];
		for (int i = 0 ; i < litterals.size() ; i++) {
			this.litterals[i] = litterals.get(i);
		}
	}

	public Litteral get(int index) {
		return litterals[index];
	}
	
	public int getId() {
		return id;
	}

	public Litteral [] getLitterals() {
		return litterals;
	}
	
	public int size() {
		return litterals.length;
	}
	
	@Override
	public String toString() {
		String str = "";
		for (Litteral l : litterals) {
			str += l.toString() + " ";
		}
		return str;
	}
}
