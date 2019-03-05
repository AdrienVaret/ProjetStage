package bincsp;

import java.util.ArrayList;

public class Domain {

	private String name;
	private ArrayList<String> values;
	private int [] shift;
	
	public Domain(String name, ArrayList<String> values) {
		this.values = values;
		this.name = name;
	}

	public ArrayList<String> getValues() {
		return values;
	}
	
	public String get(int index) {
		return values.get(index);
	}
	
	public int size() {
		return values.size();
	}
	
	public String getName() {
		return name;
	}
	
	public int [] getShift() {
		return shift;
	}
	
	public void setShift(int [] shift) {
		this.shift = shift;
	}
	
	public void remove(int index) {
		values.remove(index);
	}
	
	@Override
	public boolean equals(Object o) {
		Domain d = (Domain) o;
		if (name.equals(d.getName())) {
			if (d.size() != size()) return false;
			for (int i = 0 ; i < size() ; i++) {
				if (!get(i).equals(d.get(i))) return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		String string = "[";
		for (String value : values) {
			string += value + ", ";
		}
		string += "]";
		return string;
	}
}
