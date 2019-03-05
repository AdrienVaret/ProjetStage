package sat;

public class Affectation {

	private Litteral litteral;
	private int level;
	
	public Affectation(Litteral litteral, int level) {
		this.litteral = litteral;
		this.level = level;
	}

	public Litteral getLitteral() {
		return litteral;
	}

	public int getLevel() {
		return level;
	}
	
	public String toString() {
		return litteral.toString() + " - " + level;
	}
}