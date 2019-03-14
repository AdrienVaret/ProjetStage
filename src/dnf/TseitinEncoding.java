package dnf;

import java.util.ArrayList;

import utils.GenericCouple2;

public class TseitinEncoding {

	static int i = 0;
	static ArrayList<Formula> formulas = new ArrayList<Formula>();
	
	
	public static Formula transformVar(Var fi) {
		return fi;
	}
	
	public static Formula transformNeg(Neg fi) {
		Formula f1 = fi.getF1();
		
		if (f1 instanceof Neg) {
			return ((Neg) f1).getF1();
		}
		
		if (f1 instanceof Or) {
			return new And(transform(new Neg(((Or) f1).getF1())),
					       transform(new Neg(((Or) f1).getF2())));
		}
		
		if (f1 instanceof And) {
			return new Or(transform(new Neg(((And) f1).getF1())),
					       transform(new Neg(((And) f1).getF2())));
		}
		
		if (f1 instanceof Var) {
			return fi;
		}
		
		return null;
	}
	
	public static Formula transformIff(Iff fi) {
		Formula f1 = fi.getF1();
		Formula f2 = fi.getF2();
		
		if (f1 instanceof Var && f2 instanceof Neg) {
			Formula a = new Or(new Neg(f1), transform(f2));
			Formula f2_1 = ((Neg) f2).getF1();
			Formula b = new Or(transform(f2_1), f1);
			return new And(a,b);
		}
		
		if (f1 instanceof Var && (f2 instanceof And || f2 instanceof Or)) {
			Formula a = new Or(new Neg(f1), transform(f2));
			Formula b = new Or(new Neg(f2), transform(f1));
			return new And(a,b);
		}
		
		else {
			return null;
		}
	}
	
	public static Formula transformOr(Or fi) {
		
		Formula f1 = fi.getF1();
		Formula f2 = fi.getF2();
		
		if (f1 instanceof And && f2 instanceof And) {
			Formula f1_1 = ((And) f1).getF1();
			Formula f1_2 = ((And) f1).getF2();
			Formula f2_1 = ((And) f2).getF1();
			Formula f2_2 = ((And) f2).getF2();
			
			Formula f3 = new Or(transform(f1_1), transform(f2_1));
			Formula f4 = new Or(transform(f1_1), transform(f2_2));
			Formula f5 = new Or(transform(f1_2), transform(f2_1));
			Formula f6 = new Or(transform(f1_2), transform(f2_2));
			
			return new And(f3, new And(f4, new And(f5, f6)));
		}
		
		else if (f1 instanceof Var && f2 instanceof And) {
			Formula f2_1 = ((And) f2).getF1();
			Formula f2_2 = ((And) f2).getF2();
			
			Formula f3 = new Or(f1, transform(f2_1));
			Formula f4 = new Or(f1, transform(f2_2));
			
			return new And(f3, f4);
		}
		
		else if (f1 instanceof And && f2 instanceof Var) {
			Formula f1_1 = ((And) f1).getF1();
			Formula f1_2 = ((And) f1).getF2();
			
			Formula f3 = new Or(transform(f1_1), f2);
			Formula f4 = new Or(transform(f1_2), f2);
			
			return new And(f3, f4);
		}
		
		return fi;
	}
	
	public static Formula transformAnd(Formula fi) {
		return fi;
	}
	
	public static Formula checkNegation(Neg fi) {
		Formula f1 = fi.getF1();
		if (f1 instanceof Neg) {
			return ((Neg) f1).getF1();
		}
		return fi;
	}
	
	public static Formula transform(Formula fi) {
		
		if (fi instanceof Iff) {
			
			Formula f1 = ((Iff) fi).getF1();
			Formula f2 = ((Iff) fi).getF2();
			
			//attention au transform des négations
			
			//return new And(new Or(checkNegation(new Neg(transform(f1))), transform(f2)),
			//		       new Or(checkNegation(new Neg(transform(f2))), transform(f1)));
			
			return new And(transform(new Or(transform(new Neg(transform(f1))), transform(f2))),
				       transform(new Or(transform(new Neg(transform(f2))), transform(f1))));
			
			
		} else if (fi instanceof If) {
			
			Formula f1 = ((If) fi).getF1();
			Formula f2 = ((If) fi).getF2();
			
			return new Or(new Neg(transform(f1)), transform(f2));
		
		} else if (fi instanceof Or) {
			 return transformOr((Or) fi);
		}
		
		else if (fi instanceof Var) {
			return transformVar((Var)fi);
		}
		
		else if (fi instanceof Neg) {
			return transformNeg((Neg) fi);
		}
		
		else {
			return fi;
		}
	}
	
	public static Formula exploreFormula(Formula fi) {
		
		i++;
		
		if (fi instanceof Var) {
			i --;
			return fi;
		}
		
		else if (fi instanceof Neg) {
			Formula f = ((Neg) fi).getF1();
			
			int index = i;
			
			formulas.add(new Iff(new Var(Integer.toString(i)), 
					             new Neg(exploreFormula(f))));
			
			return new Var(Integer.toString(index));
		}
		
		else if (fi instanceof And) {
			Formula f1 = ((And) fi).getF1();
			Formula f2 = ((And) fi).getF2();
			
			int index = i;
			
			Formula e1 = exploreFormula(f1);
			Formula e2 = exploreFormula(f2);
			
			formulas.add(new Iff(new Var(Integer.toString(index)), 
					             new And(e1, e2)));
			
			return new Var(Integer.toString(index));
		}
		
		else if (fi instanceof Or) {
			Formula f1 = ((Or) fi).getF1();
			Formula f2 = ((Or) fi).getF2();
			
			int index = i;
			
			Formula e1 = exploreFormula(f1);
			Formula e2 = exploreFormula(f2);
			
			formulas.add(new Iff(new Var(Integer.toString(index)), 
					             new Or(e1, e2)));
			
			return new Var(Integer.toString(index));
		}
		
		else if (fi instanceof If) {
			Formula f1 = ((If) fi).getF1();
			Formula f2 = ((If) fi).getF2();
			
			int index = i;
			
			Formula e1 = exploreFormula(f1);
			Formula e2 = exploreFormula(f2);
			
			formulas.add(new Iff(new Var(Integer.toString(index)), 
					             new If(e1, e2)));
			
			return new Var(Integer.toString(index));
		}
		
		else {
			Formula f1 = ((If) fi).getF1();
			Formula f2 = ((If) fi).getF2();
			
			int index = i;
			
			Formula e1 = exploreFormula(f1);
			Formula e2 = exploreFormula(f2);
			
			formulas.add(new Iff(new Var(Integer.toString(index)), 
					             new If(e1, e2)));
			
			return new Var(Integer.toString(index));
		}

	}
	
	
	public static Formula encode(Formula fi) {
		formulas.add(new Var(Integer.toString(1)));
		exploreFormula(fi);
		i = 0;
		
		ArrayList<Formula> transformedFormulas = new ArrayList<Formula>();
		
		for (Formula f : formulas) 
			System.out.println(f.toString());
		
		int i = 0;
		for (Formula f : formulas) {
			if (i > 2) {
				Formula tf = transform(f);
				transformedFormulas.add(tf);
			}
			i++;
		}
		
		System.out.println("###########");
		
		for (Formula f : transformedFormulas) 
			System.out.println(f.toString());
		
		return null;
	}
	
	public static void main(String [] args) {
		If fi = new If(new Var("a"), new Or(new Var("b"), new Var("c")));
		
		Neg f1 = new Neg(new Var("s"));
		Or f2 = new Or(new Var("p"), new Var("q"));
		And f3 = new And(f2, new Var("r"));
		If fi2 = new If(f3, f1);
		
		Neg neg = new Neg(new Or(new Neg(new Var("a")), new Var("c")));
		Or or1 = new Or(new Var("b"), neg);
		Or or2 = new Or(new Neg(new Var("a")), or1);
		
		
		encode(or2);
	}
}
