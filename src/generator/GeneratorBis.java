package generator;

import java.util.ArrayList;

import bincsp.BinCSP;
import bincsp.Constraint;
import bincsp.Couple;
import bincsp.Domain;
import bincsp.Relation;
import bincsp.Variable;
import utils.GenericCouple;
import bincsp.Relation.TypeRelation;

public class GeneratorBis {

	public static GenericCouple<BinCSP> generateExampleBug1() {
		
		ArrayList<Variable> variables = new ArrayList<Variable>();
		ArrayList<Domain> domains = new ArrayList<Domain>();
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
		ArrayList<Relation> relations = new ArrayList<Relation>();
		
		int value = 0;
		for (int i = 0 ; i < 4 ; i++) {
			ArrayList<String> values = new ArrayList<String>();
			for (int j = 0 ; j < 3 ; j++) {
				values.add(Integer.toString(value));
				value ++;
			}
			domains.add(new Domain("D" + i, values));
		}
		
		for (int i = 0 ; i < 4 ; i++) {
			variables.add(new Variable("X" + i, domains.get(i), i));
		}
		
		Variable x = variables.get(1);
		Variable y = variables.get(2);
		
		ArrayList<Couple> couples = new ArrayList<Couple>();
		couples.add(new Couple(x.getDomain().get(0), y.getDomain().get(0)));
		couples.add(new Couple(x.getDomain().get(1), y.getDomain().get(0)));
		couples.add(new Couple(x.getDomain().get(1), y.getDomain().get(1)));
		couples.add(new Couple(x.getDomain().get(2), y.getDomain().get(0)));
		
		Relation relation = new Relation(TypeRelation.R_CONFLICTS, couples);
		Constraint constraint = new Constraint(x, y, relation);
		
		relations.add(relation);
		constraints.add(constraint);
		
		BinCSP csp = new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
		          variables, domains, constraints, relations);
		
		int [][] M = new int [4][4];
		M[1][2] = 1;
		M[2][1] = 1;
		
		BinCSP csp2 = Generator.conflictToSupports(csp, 3, M);
		
		return new GenericCouple<BinCSP>(csp, csp2);
		
	}
	
	public static void main(String [] args) {
		
	}
}
