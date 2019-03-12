package dnf;

public class Or implements Formula{
	
	private Formula f1, f2;
	
	public Or(Formula f1, Formula f2) {
		this.f1 = f1;
		this.f2 = f2;
	}
	
	public String toString() {
		return "(" + f1.toString() + " \\or " + f2.toString() + ")";
	}

	public Formula getF1() {
		return f1;
	}

	public Formula getF2() {
		return f2;
	}
	
}
