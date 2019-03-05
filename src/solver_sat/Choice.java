package solver_sat;

import sat.Litteral;

public class Choice {

	private Litteral l1, l2;
	private int size;
	
	public Choice (Litteral l1, Litteral l2) {
		this.l1 = l1;
		this.l2 = l2;
		size = 2;
	}
	
	public Choice (Litteral l1) {
		this.l1 = l1;
		this.l2 = null;
		size = 1;
	}

	public Litteral getL1() {
		return l1;
	}

	public Litteral getL2() {
		return l2;
	}

	public int getSize() {
		return size;
	}
}
