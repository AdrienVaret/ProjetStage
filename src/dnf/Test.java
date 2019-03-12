package dnf;

public class Test {

	public static void main(String [] args) {
		If fi = new If(new Var("a"), new Or(new Var("b"), new Var("c")));
		System.out.println(fi.toString());
	}
}
