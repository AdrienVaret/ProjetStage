package bincsp;

import bincsp.Relation.TypeRelation;

public class Constraint {

	private Variable variable1, variable2;
	private Relation relation;
	
	public Constraint(Variable variable1, Variable variable2, Relation relation) {
		this.variable1 = variable1;
		this.variable2 = variable2;
		this.relation = relation;
	}

	public Variable getVariable1() {
		return variable1;
	}

	public Variable getVariable2() {
		return variable2;
	}

	public Relation getRelation() {
		return relation;
	}
	
	@Override
	public String toString() {
		String state;
		if (relation.getTypeRelation() == TypeRelation.R_CONFLICTS)
			state = "conflicts";
		else
			state = "supports";
		
		return "C" + variable1.getName() + "_" + variable2.getName() + relation.toString() + "(" + state + ")";
	}
}
