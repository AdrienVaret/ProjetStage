package sat;

import java.util.ArrayList;

public class Litteral {

	private int id;
	private ArrayList<Clause> occurences;
	private int idVariable;
	
	public Litteral(int id, int idVariable) {
		this.id = id;
		this.idVariable = idVariable;
		occurences = new ArrayList<Clause>();
	}

	public int getId() {
		return id;
	}
	
	public void addOccurence(Clause clause) {
		occurences.add(clause);
	}
	
	public void removeOccurence(Clause clause) {
		occurences.remove(clause);
	}
	
	public int getIdVariable() {
		return idVariable;
	}
	
	@Override
	public String toString() {
		if (id % 2 == 0)
			return Integer.toString((id / 2) + 1);
		else {
			return "-" + Integer.toString(((id - 1) / 2) + 1);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		Litteral l = (Litteral) o;
		return (id == l.getId());
	}
}
