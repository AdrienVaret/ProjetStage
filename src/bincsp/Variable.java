package bincsp;

public class Variable {

	private String name;
	private Domain domain;
	private int index;
	
	public Variable(String name, Domain domain, int index) {
		this.name = name;
		this.domain = domain;
		this.index = index;
	}

	public Variable(String name, Domain domain) {
		this.name = name;
		this.domain = domain;
	}
	
	public String getName() {
		return name;
	}

	public Domain getDomain() {
		return domain;
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	public boolean equals(Object o) {
		Variable v = (Variable) o;
		if (name.equals(v.getName()) && domain.equals(v.getDomain())) return true;
		return false;
	}
	
	@Override
	public String toString() {
		return name + "(" + domain.toString() + ")";
	}
}
