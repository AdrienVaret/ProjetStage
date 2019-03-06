package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
/*
import org.w3c.dom.Document;
import org.xcsp.common.Types.TypeArithmeticOperator;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.Types.TypeFlag;
import org.xcsp.common.Types.TypeUnaryArithmeticOperator;
import org.xcsp.common.Utilities;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.XCallbacks2;
import org.xcsp.parser.entries.XVariables.XVarInteger;
*/
import bincsp.BinCSP;
import bincsp.Constraint;
import bincsp.Couple;
import bincsp.Domain;
import bincsp.Relation;
import bincsp.Variable;
import utils.Operator;
import bincsp.Relation.TypeRelation;
import conversion.BinCSPConverter;

public class Parser /*implements XCallbacks2*/{

	/**
	* The constants that can be used to pilot the parser .
	*//*
	enum XCallbacksParameters {
		RECOGNIZE_UNARY_PRIMITIVES ,
		RECOGNIZE_BINARY_PRIMITIVES ,
		RECOGNIZE_TERNARY_PRIMITIVES ,
		RECOGNIZE_LOGIC_CASES ,
		RECOGNIZE_EXTREMUM_CASES , // minimum and maximum
		RECOGNIZE_COUNT_CASES ,
		RECOGNIZE_NVALUES_CASES ,
		INTENSION_TO_EXTENSION_ARITY_LIMIT , // 0 deactivates "intension to extension" conversion
		INTENSION_TO_EXTENSION_SPACE_LIMIT ,
		INTENSION_TO_EXTENSION_PRIORITY ;
	}
	
	private Implem implem = new Implem ( this );
	private Map <XVarInteger , Variable> mapVar = new LinkedHashMap <>() ;
	
	private ArrayList<Variable> variables     = new ArrayList<Variable>();
	private ArrayList<Domain> domains         = new ArrayList<Domain>();
	private ArrayList<Constraint> constraints = new ArrayList<Constraint>();
	private ArrayList<Relation> relations     = new ArrayList<Relation>();

	private int nbVariables;
	
	/**
	* Returns a map with the default parameters that can be used to pilot the parser .
	* When parsing , by default the parser will try for example to recognize
	* primitives and special cases of count and nValues .
	*/
	/*
	@SuppressWarnings("unused")
	private Map < XCallbacksParameters , Object > defaultParameters () {
		Object dummy = new Object () ;
		Map < XCallbacksParameters , Object> map = new HashMap <>() ;
		map . put ( XCallbacksParameters . RECOGNIZE_UNARY_PRIMITIVES , dummy ) ;
		map . put ( XCallbacksParameters . RECOGNIZE_BINARY_PRIMITIVES , dummy );
		map . put ( XCallbacksParameters . RECOGNIZE_TERNARY_PRIMITIVES , dummy );
		map . put ( XCallbacksParameters . RECOGNIZE_LOGIC_CASES , dummy );
		map . put ( XCallbacksParameters . RECOGNIZE_EXTREMUM_CASES , dummy );
		map . put ( XCallbacksParameters . RECOGNIZE_COUNT_CASES , dummy );
		map . put ( XCallbacksParameters . RECOGNIZE_NVALUES_CASES , dummy );
		map . put ( XCallbacksParameters . INTENSION_TO_EXTENSION_ARITY_LIMIT , 0) ; // included
		map . put ( XCallbacksParameters . INTENSION_TO_EXTENSION_SPACE_LIMIT , 1000000) ;
		map . put ( XCallbacksParameters . INTENSION_TO_EXTENSION_PRIORITY , Boolean . TRUE );
		return map ;
	}
	
	public Parser ( String fileName ) throws Exception {
		loadInstance ( fileName );
	}

	public ArrayList<Variable> getVariables(){
		return variables;
	}
	
	public ArrayList<Domain> getDomains() {
		return domains;
	}
	
	public ArrayList<Constraint> getConstraints() {
		return constraints;
	}
	
	public ArrayList<Relation> getRelations() {
		return relations;
	}
	
	private Variable trVar ( Object x) {
		return mapVar . get (( XVarInteger ) x);
	}
	
	private Variable [] trVars ( Object vars ) {
		return Arrays . stream (( XVarInteger []) vars ). map (x -> mapVar . get (x) ). toArray (Variable []:: new );
	}
	
	@SuppressWarnings("unused")
	private Variable [][] trVars2D ( Object vars ) {
		return Arrays . stream (( XVarInteger [][]) vars ). map (t -> trVars (t)). toArray ( Variable [][]:: new );
	}
	
	@Override
	public Implem implem() {
		return implem;
	}
	
	@Override
	public Document loadDocument ( String fileName ) throws Exception {
		return Utilities.loadDocument(fileName);
	} 
	
	@Override
	public void buildVarInteger ( XVarInteger xx , int minValue , int maxValue ) {
		ArrayList<String> values = new ArrayList<String>();
		for (int i = minValue ; i <= maxValue ; i++)
			values.add(Integer.toString(i));
		
		Domain d = new Domain("D" + xx.id, values);
		
		Variable x = new Variable(xx.id, d, nbVariables);
		nbVariables ++;
		variables.add(x);
		domains.add(d);
		mapVar . put (xx ,x) ;
	}
	
	@Override
	public void buildVarInteger ( XVarInteger xx , int [] values ) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0 ; i < values.length ; i++)
			list.add(Integer.toString(values[i]));

		Domain d = new Domain("D"+xx.id, list);
		Variable x = new Variable(xx.id, d, nbVariables);
		nbVariables ++;
		variables.add(x);
		mapVar . put (xx ,x) ;
	}

	@Override
	public void buildCtrExtension ( String id , XVarInteger [] list , int [][] tuples , boolean positive , 
			Set < TypeFlag > flags ) {
		
		TypeRelation type;
		if (positive) type = TypeRelation.R_SUPPORTS;
		else type = TypeRelation.R_CONFLICTS;
		
		Variable x = null, y = null;
		
		for (int i = 0 ; i < list.length ; i++) {
			for (int j = i+1 ; j < list.length ; j++) {
				ArrayList<Couple> couples = new ArrayList<Couple>();
				x = trVar(list[i]);
				y = trVar(list[j]);
				for (int line = 0 ; line < tuples.length ; line ++) {
					String v1 = Integer.toString(tuples[line][i]);
					String v2 = Integer.toString(tuples[line][j]);
					couples.add(new Couple(v1, v2));
				}
				Relation r = new Relation(type, couples);
				Constraint c = new Constraint(x, y, r);		
				constraints.add(c);
				relations.add(r);
			}
		}
		
		
	}

	@Override
	public void buildCtrPrimitive ( String id , XVarInteger x, TypeConditionOperatorRel op , int k) {
		//System.out.println(op.toString());
		System.out.println("1");
		ArrayList<Couple> couples = new ArrayList<Couple>();
		Variable v = trVar(x);
		Domain d = v.getDomain();
		switch (op) {
			case LE :
				//lower_equal
				for (String value : d.getValues()) {
					int intValue = Integer.parseInt(value);
					if (!(intValue <= k))
						couples.add(new Couple(value, value));
				}
				break;
				
			case LT : 
				//lower_than
				for (String value : d.getValues()) {
					int intValue = Integer.parseInt(value);
					if (!(intValue < k))
						couples.add(new Couple(value, value));
				}
			
			case GE :
				//greater_equal
				for (String value : d.getValues()) {
					int intValue = Integer.parseInt(value);
					if (!(intValue >= k))
						couples.add(new Couple(value, value));
				}
				break;
				
			case GT : 
				//greater_than
				for (String value : d.getValues()) {
					int intValue = Integer.parseInt(value);
					if (!(intValue > k))
						couples.add(new Couple(value, value));
				}
				break;
				
			case EQ :
				//equal
				for (String value : d.getValues()) {
					int intValue = Integer.parseInt(value);
					if (!(intValue == k))
						couples.add(new Couple(value, value));
				}
				break;
				
			case NE : 
				//not_equal
				for (String value : d.getValues()) {
					int intValue = Integer.parseInt(value);
					if (!(intValue != k))
						couples.add(new Couple(value, value));
				}
		}
		Relation r = new Relation(TypeRelation.R_CONFLICTS, couples);
		Constraint c = new Constraint(v, v, r);
		relations.add(r);
		constraints.add(c);
	}
	
	@Override
	public void buildCtrPrimitive ( String id , XVarInteger x, TypeArithmeticOperator opa ,
			XVarInteger y, TypeConditionOperatorRel op , int k) {	
		System.out.println("1");
		//System.out.println(op.toString() + "(" + x.toString() + ", " + y.toString() + ")");
		ArrayList<Couple> couples = new ArrayList<Couple>();
		Variable vx = trVar(x);
		Variable vy = trVar(y);
		if (k == 0) {
			switch (op) {
				case LE :
					//x <= y
					for (String v1 : vx.getDomain().getValues()) {
						for (String v2 : vy.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							if (!(intv1 <= intv2))
								couples.add(new Couple(v1, v2));
						}
					}
					break;
					
				case LT : 
					//x < y
					for (String v1 : vx.getDomain().getValues()) {
						for (String v2 : vy.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							if (!(intv1 < intv2))
								couples.add(new Couple(v1, v2));
						}
					}
					break;
					
				case GE : 
					//x >= y
					for (String v1 : vx.getDomain().getValues()) {
						for (String v2 : vy.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							if (!(intv1 >= intv2))
								couples.add(new Couple(v1, v2));
						}
					}
					break;
					
				case GT : 
					//x > y
					for (String v1 : vx.getDomain().getValues()) {
						for (String v2 : vy.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							if (!(intv1 > intv2))
								couples.add(new Couple(v1, v2));
						}
					}
					break;
					
				case EQ : 
					//x == y
					for (String v1 : vx.getDomain().getValues()) {
						for (String v2 : vy.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							if (!(intv1 == intv2))
								couples.add(new Couple(v1, v2));
						}
					}
					
				case NE : 
					//x != y
					for (String v1 : vx.getDomain().getValues()) {
						for (String v2 : vy.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							if (!(intv1 != intv2))
								couples.add(new Couple(v1, v2));
						}
					}
					break;
			}
			Relation r = new Relation(TypeRelation.R_CONFLICTS, couples);
			Constraint c = new Constraint(vx, vy, r);
			relations.add(r);
			constraints.add(c);
		} else {
			System.out.println("k != 0 unimplemented");
		}
	}
	
	@Override
	public void buildCtrPrimitive ( String id , XVarInteger x, TypeArithmeticOperator opa ,
			XVarInteger y, TypeConditionOperatorRel op , XVarInteger z) {
		//System.out.println(opa.toString() + " // " + op.toString());
		System.out.println("3");
		Variable vx = trVar(x);
		Variable vy = trVar(y);
		Variable vz = trVar(z);
		
		Operator operator = null;
		
		switch(opa) {
			case ADD : 
				operator = new Operator("+");
				break;
				
			case SUB : 
				operator = new Operator("-");
				break;
				
			case MUL :
				operator = new Operator("*");
				break;
				
			case DIV : 
				operator = new Operator("/");
				break;
		
			case MOD :
				operator = new Operator("%");
				break;
				
			case POW : 
				operator = new Operator("^");
				break;
				
			case DIST : 
				operator = new Operator("D");
				break;
		}
		
		ArrayList<Couple> couplesxy = new ArrayList<Couple>();
		ArrayList<Couple> couplesxz = new ArrayList<Couple>();
		ArrayList<Couple> couplesyz = new ArrayList<Couple>();
		
		switch (op) {
			case EQ : 
				for (String v1 : vx.getDomain().getValues()) {
					for (String v2 : vy.getDomain().getValues()) {
						for (String v3 : vz.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							int intv3 = Integer.parseInt(v3);
							//Double r = new Double(operator.compute(intv1, intv2));
							Number r = operator.compute(intv1, intv2);
							
							if (((Integer)r == intv3)) {
								couplesxy.add(new Couple(v1, v2));
								couplesxz.add(new Couple(v1, v3));
								couplesyz.add(new Couple(v2, v3));
							}
						}
					}
				}
				break;
				
			case GE : 
				for (String v1 : vx.getDomain().getValues()) {
					for (String v2 : vy.getDomain().getValues()) {
						for (String v3 : vz.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							int intv3 = Integer.parseInt(v3);
							//double r = (double)operator.compute(intv1, intv2);
							Number r = operator.compute(intv1, intv2);
							if (((Integer)r >= intv3)) {
								couplesxy.add(new Couple(v1, v2));
								couplesxz.add(new Couple(v1, v3));
								couplesyz.add(new Couple(v2, v3));
							}
						}
					}
				}
				break;
				
			case GT : 
				for (String v1 : vx.getDomain().getValues()) {
					for (String v2 : vy.getDomain().getValues()) {
						for (String v3 : vz.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							int intv3 = Integer.parseInt(v3);
							//double r = (double)operator.compute(intv1, intv2);
							Number r = operator.compute(intv1, intv2);
							if (((Integer)r > intv3)) {
								couplesxy.add(new Couple(v1, v2));
								couplesxz.add(new Couple(v1, v3));
								couplesyz.add(new Couple(v2, v3));
							}
						}
					}
				}
				break;
				
			case LE : 
				for (String v1 : vx.getDomain().getValues()) {
					for (String v2 : vy.getDomain().getValues()) {
						for (String v3 : vz.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							int intv3 = Integer.parseInt(v3);
							double r = (double)operator.compute(intv1, intv2);
							if ((r <= intv3)) {
								couplesxy.add(new Couple(v1, v2));
								couplesxz.add(new Couple(v1, v3));
								couplesyz.add(new Couple(v2, v3));
							}
						}
					}
				}
				break;
				
			case LT :
				for (String v1 : vx.getDomain().getValues()) {
					for (String v2 : vy.getDomain().getValues()) {
						for (String v3 : vz.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							int intv3 = Integer.parseInt(v3);
							double r = (double)operator.compute(intv1, intv2);
							if ((r < intv3)) {
								couplesxy.add(new Couple(v1, v2));
								couplesxz.add(new Couple(v1, v3));
								couplesyz.add(new Couple(v2, v3));
							}
						}
					}
				}
				break;
				
			case NE :
				for (String v1 : vx.getDomain().getValues()) {
					for (String v2 : vy.getDomain().getValues()) {
						for (String v3 : vz.getDomain().getValues()) {
							int intv1 = Integer.parseInt(v1);
							int intv2 = Integer.parseInt(v2);
							int intv3 = Integer.parseInt(v3);
							double r = (double)operator.compute(intv1, intv2);
							if ((r != intv3)) {
								couplesxy.add(new Couple(v1, v2));
								couplesxz.add(new Couple(v1, v3));
								couplesyz.add(new Couple(v2, v3));
							}
						}
					}
				}
				break;
		}
		Relation rxy = new Relation(TypeRelation.R_SUPPORTS, couplesxy);
		Relation rxz = new Relation(TypeRelation.R_SUPPORTS, couplesxz);
		Relation ryz = new Relation(TypeRelation.R_SUPPORTS, couplesyz);
		
		relations.add(rxy);
		relations.add(rxz);
		relations.add(ryz);
		
		Constraint cxy = new Constraint(vx, vy, rxy);
		Constraint cxz = new Constraint(vx, vz, rxz);
		Constraint cyz = new Constraint(vy, vz, ryz);
		
		constraints.add(cxy);
		constraints.add(cxz);
		constraints.add(cyz);
	}
	
	public void buildCtrIntension ( String id , XVarInteger [] scope , XNodeParent < XVarInteger > tree ) {
		System.out.println("4");
		System.out.println("Not implemented");
	}
	
	public void buildCtrPrimitive ( String id , XVarInteger x, TypeConditionOperatorSet op ,int [] t) {
		System.out.println("5");
		Variable vx = trVar(x);
		ArrayList<Couple> couples = new ArrayList<Couple>();
		switch(op) {
			case IN : 
				for (String v : vx.getDomain().getValues()) {
					int vint = Integer.parseInt(v);
					boolean contains = false;
					for (int i = 0 ; i < t.length ; i++) {
						if (t[i] == vint) {
							contains = true;
							break;
						}
					}
					if (!contains) couples.add(new Couple(v, v));
				}
				break;
				
			case NOTIN :
				for (String v : vx.getDomain().getValues()) {
					int vint = Integer.parseInt(v);
					boolean contains = false;
					for (int i = 0 ; i < t.length ; i++) {
						if (t[i] == vint) {
							contains = true;
							break;
						}
					}
					if (contains)
						couples.add(new Couple(v, v));
				}
				break;
		}
		
		Relation r = new Relation(TypeRelation.R_CONFLICTS, couples);
		Constraint c = new Constraint(vx, vx, r);
		
		relations.add(r);
		constraints.add(c);
	}
	
	public void buildCtrPrimitive ( String id , XVarInteger x, TypeConditionOperatorSet op 
			,int min , int max ) {
		
		System.out.println("6");
		ArrayList<Couple> couples = new ArrayList<Couple>();
		Variable vx = trVar(x);
		
		switch(op) {
			case IN : 
				for (String v : vx.getDomain().getValues()) {
					int intvx = Integer.parseInt(v);
					if (!(min <= intvx && intvx <= max))
						couples.add(new Couple(v, v));
				}
				break;
				
			case NOTIN : 
				for (String v : vx.getDomain().getValues()) {
					int intvx = Integer.parseInt(v);
					if ((min <= intvx && intvx <= max))
						couples.add(new Couple(v, v));
				}
				break;
		}
		
		Relation r = new Relation(TypeRelation.R_CONFLICTS, couples);
		Constraint c = new Constraint(vx, vx, r);
		
		relations.add(r);
		constraints.add(c);
	}
			
	public void buildCtrPrimitive ( String id , XVarInteger x, TypeArithmeticOperator aop ,
			int p, TypeConditionOperatorRel op , int k) {
		
		System.out.println("7");
		Operator operator = null;
		
		switch(aop) {
			case ADD : 
				operator = new Operator("+");
				break;
				
			case SUB : 
				operator = new Operator("-");
				break;
				
			case MUL :
				operator = new Operator("*");
				break;
				
			case DIV : 
				operator = new Operator("/");
				break;
		
			case MOD :
				operator = new Operator("%");
				break;
				
			case POW : 
				operator = new Operator("^");
				break;
				
			case DIST : 
				operator = new Operator("D");
				break;
		}
		
		ArrayList<Couple> couples = new ArrayList<Couple>();
		
		Variable vx = trVar(x);
		
		switch(op) {
			case EQ : 
				for (String v : vx.getDomain().getValues()) {
					int intv = Integer.parseInt(v);
					Number r = operator.compute(intv, p);
					if (!((Integer)r == k)) {
						couples.add(new Couple(v, v));
					}
				}
				break;
				
			case GE : 
				for (String v : vx.getDomain().getValues()) {
					int intv = Integer.parseInt(v);
					Number r = operator.compute(intv, p);
					if (!((Integer)r >= k)) {
						couples.add(new Couple(v, v));
					}
				}
				break;
				
			case GT :
				for (String v : vx.getDomain().getValues()) {
					int intv = Integer.parseInt(v);
					Number r = operator.compute(intv, p);
					if (!((Integer)r > k)) {
						couples.add(new Couple(v, v));
					}
				}
				break;
				
			case LE : 
				for (String v : vx.getDomain().getValues()) {
					int intv = Integer.parseInt(v);
					Number r = operator.compute(intv, p);
					if (!((Integer)r <= k)) {
						couples.add(new Couple(v, v));
					}
				}
				break;
				
			case LT : 
				for (String v : vx.getDomain().getValues()) {
					int intv = Integer.parseInt(v);
					Number r = operator.compute(intv, p);
					if (!((Integer)r < k)) {
						couples.add(new Couple(v, v));
					}
				}
				break;
				
			case NE : 
				for (String v : vx.getDomain().getValues()) {
					int intv = Integer.parseInt(v);
					Number r = operator.compute(intv, p);
					if (!((Integer)r != k)) {
						couples.add(new Couple(v, v));
					}
				}
				break;
		}
	
		Relation r = new Relation(TypeRelation.R_CONFLICTS, couples);
		Constraint c = new Constraint(vx, vx, r);
		
		relations.add(r);
		constraints.add(c);
	}
	
	
	public void buildCtrPrimitive ( String id , XVarInteger x, TypeUnaryArithmeticOperator aop 
			,XVarInteger y) {
		System.out.println("WARNING : can be incorrect");
		System.out.println("8");
		Variable vx = trVar(x);
		Variable vy = trVar(y);
		ArrayList<Couple> couples = new ArrayList<Couple>();
		switch (aop) {
			case ABS :
				for (String v1 : vx.getDomain().getValues()) {
					for (String v2 : vy.getDomain().getValues()) {
						int intvx = Integer.parseInt(v1);
						int intvy = Integer.parseInt(v2);
						if (! (Math.abs(intvx) == intvy))
							couples.add(new Couple(v1, v2));
					}
				}
				break;
				
			case NEG : 
				for (String v1 : vx.getDomain().getValues()) {
					for (String v2 : vy.getDomain().getValues()) {
						int intvx = Integer.parseInt(v1);
						int intvy = Integer.parseInt(v2);
						if (! (-intvx == intvy))
							couples.add(new Couple(v1, v2));
					}
				}
				break;
				
			case NOT :
				System.out.println("Operator NOT not implemented ...");
				break;
				
			case SQR :
				for (String v1 : vx.getDomain().getValues()) {
					for (String v2 : vy.getDomain().getValues()) {
						int intvx = Integer.parseInt(v1);
						int intvy = Integer.parseInt(v2);
						if (! ((double)Math.sqrt(intvx) == (double)intvy))
							couples.add(new Couple(v1, v2));
					}
				}
				break;
		}
		
		Relation r = new Relation(TypeRelation.R_CONFLICTS, couples);
		Constraint c = new Constraint(vx, vy, r);
		
		relations.add(r);
		constraints.add(c);
	}
			
	public void buildCtrPrimitive ( String id , XVarInteger x, TypeArithmeticOperator aop ,
			int p, TypeConditionOperatorRel op , XVarInteger y) {
		System.out.println("9");
		System.out.println("Coming soon ...");
	}
			
	
	public BinCSP buildCSP() {
		BinCSP csp = new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
				          variables, domains, constraints, relations);
		csp = BinCSPConverter.convertToConflicts(csp);
		return csp;
	}
	
	public static void main(String [] args) throws Exception {
		Parser parser = new Parser("file.xml");
		System.out.println(parser.buildCSP().toString());	
	} */
}
