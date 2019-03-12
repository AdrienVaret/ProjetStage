package dnf;

public class Var implements Formula{

	private String name;
	
	public Var(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
}
