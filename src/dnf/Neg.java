package dnf;

public class Neg implements Formula{
	
	private Formula f1;
	
	public Neg(Formula f1) {
		this.f1 = f1;
	}
	
	public String toString() {
		return "(\\not " + f1.toString() + ")";
	}

	public Formula getF1() {
		return f1;
	}
	
}
