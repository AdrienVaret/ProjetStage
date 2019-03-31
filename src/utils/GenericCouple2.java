package utils;

public class GenericCouple2<S, T> {

	private S value1;
	private T value2;
	
	public GenericCouple2() {
		//DO_NOTHING
	}
	
	public GenericCouple2(S v1, T v2) {
		this.value1 = v1;
		this.value2 = v2;
	}

	public S getValue1() {
		return value1;
	}

	public void setValue1(S value1) {
		this.value1 = value1;
	}

	public T getValue2() {
		return value2;
	}

	public void setValue2(T value2) {
		this.value2 = value2;
	}
	
	public String toString() {
		return "(" + value1.toString() + ", " + value2.toString() + ")";
	}
}
