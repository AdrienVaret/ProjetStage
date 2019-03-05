package utils;

public class Operator {

	private String operator;
	
	public Operator(String operator) {
		this.operator = operator;
	}
	
	public Number compute(int x, int y) {
		if (operator.equals("+")) return x + y;
		else if (operator.equals("-")) return x - y;
		else if (operator.equals("/")) return x / y;
		else if (operator.equals("*")) return x * y;
		else if (operator.equals("%")) return x % y;
		else if (operator.equals("^")) return x ^ y;
		else if (operator.equals("D")) {
			int r = x - y;
			if (r < 0) return -r;
			else return r;
		}
		return null;
	}
}
