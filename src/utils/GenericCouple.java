package utils;

public class GenericCouple<T> {

	private T v1, v2;
	
	public GenericCouple(T v1, T v2){
		this.v1 = v1;
		this.v2 = v2;
	}
	
	public T getV1() {
		return v1;
	}
	
	public T getV2() {
		return v2;
	}
	
	@Override
	public String toString() {
		if (v1 != null && v2 != null)
			return "(" + v1.toString() + ", " + v2.toString() + ")";
		else if (v1 != null && v2 == null)
			return ("(" + v1.toString() + ")");
		else if (v1 == null && v2 != null)
			return ("(" + v2.toString() + ")");
		else 
			return ("(null, null)");
	}
}
