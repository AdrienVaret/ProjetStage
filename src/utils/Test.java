package utils;

public class Test {

	public static int getIndex(int x) {
		return x >> 1;
	}
	
	public static void main(String [] args) {
		for (int i = 0 ; i < 11 ; i += 2) {
			System.out.println("(" + i + ", " + (i+1) + ") -> " + getIndex(i));
		}
	}
}
