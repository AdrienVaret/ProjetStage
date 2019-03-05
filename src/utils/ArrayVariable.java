package utils;

import java.util.ArrayList;

import bincsp.Variable;

public class ArrayVariable {

	private String name;
	private ArrayList<Variable> variables;
	
	public ArrayVariable(String name, ArrayList<Variable> variables) {
		this.name = name;
		this.variables = variables;
	}

	public String getName() {
		return name;
	}

	public ArrayList<Variable> getVariables() {
		return variables;
	}
}
