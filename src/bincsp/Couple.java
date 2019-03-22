package bincsp;

import java.util.ArrayList;

public class Couple {

	private String value1, value2;
	
	public Couple(String value1, String value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	public String getValue1() {
		return value1;
	}

	public String getValue2() {
		return value2;
	}
	
	public boolean isContained(ArrayList<Couple> couples) {
		for (Couple couple : couples) {
			if (this.equals(couple)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "(" + value1 + ", " + value2 + ")";
	}
	
	@Override
	public boolean equals(Object o) {
		Couple c = (Couple) o;
		if (value1.equals(c.getValue1()) && value2.equals(c.getValue2()))
			return true;
		return false;
	}
}
