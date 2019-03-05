package sat;

import java.util.ArrayList;

public class Clause {

	private int id;
	private ArrayList<Litteral> litterals;
	
	public Clause(int id) {
		this.id = id;
		this.litterals = new ArrayList<Litteral>();
	}

	public Litteral get(int index) {
		return litterals.get(index);
	}
	
	public int getId() {
		return id;
	}

	public ArrayList<Litteral> getLitterals() {
		return litterals;
	}
	
	public void addLitteral(Litteral litteral) {
		litterals.add(litteral);
	}
	
	public int size() {
		return litterals.size();
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
