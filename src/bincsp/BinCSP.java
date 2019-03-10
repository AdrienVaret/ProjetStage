package bincsp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import bincsp.Relation.TypeRelation;

public class BinCSP {

	private int nbVariables, nbDomains, nbConstraints, nbRelations;
	
	private ArrayList<Variable> variables;
	private ArrayList<Domain> domains;
	private ArrayList<Constraint> constraints;
	private ArrayList<Relation> relations;
	private int [] degrees;
	
	public BinCSP(int nbVariables, int nbDomains, int nbConstraints, int nbRelations, ArrayList<Variable> variables,
			ArrayList<Domain> domains, ArrayList<Constraint> constraints, ArrayList<Relation> relations) {
		this.nbVariables = nbVariables;
		this.nbDomains = nbDomains;
		this.nbConstraints = nbConstraints;
		this.nbRelations = nbRelations;
		this.variables = variables;
		this.domains = domains;
		this.constraints = constraints;
		this.relations = relations;
		this.computeDegree();
	}

	public int [] getDegrees() {
		return degrees;
	}
	
	public int getNbVariables() {
		return nbVariables;
	}

	public int getNbDomains() {
		return nbDomains;
	}

	public int getNbConstraints() {
		return nbConstraints;
	}

	public int getNbRelations() {
		return nbRelations;
	}

	public ArrayList<Variable> getVariables() {
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
	
	public void computeDegree() {
		degrees = new int[nbVariables];
		for (Constraint constraint : constraints) {
			degrees[constraint.getVariable1().getIndex()] ++;
			degrees[constraint.getVariable2().getIndex()] ++;
		}
	}
	
	public static void exportToXCSP3(BinCSP csp, String path) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(path));
			writer.write("<instance  format=\"XCSP3\" type=\"CSP\">\r\n");
			
			writer.write("\t<variables>\r\n");
			for (Variable variable : csp.getVariables()) {
				String domain = " ";
				for (String value : variable.getDomain().getValues()) {
					domain += value + " ";
				}
				writer.write("\t\t<var id=\"" + variable.getName() + "\">" + domain + "</var>\r\n");
			}
			writer.write("\t</variables>\r\n");
			
			writer.write("\t<constraints>\r\n");
			for (Constraint constraint : csp.getConstraints()) {
				String state;
				if (constraint.getRelation().getTypeRelation() == TypeRelation.R_CONFLICTS)
					state = "conflicts";
				else 
					state = "supports";
				
				String couples = "";
				for (Couple couple : constraint.getRelation().getCouples()) {
					couples += "(" + couple.getValue1() + "," + couple.getValue2() + ")";
				}
				
				writer.write("\t\t<extension>\r\n");
				writer.write("\t\t\t<list>" + constraint.getVariable1().getName() + " " + 
				                            constraint.getVariable2().getName() +"</list>\r\n");
				writer.write("\t\t\t<" + state + ">" + couples +"</" + state + ">\r\n");
				writer.write("\t\t</extension>\r\n");
			}
			writer.write("\t</constraints>\r\n");
			
			writer.write("</instance>");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	@Override
	public String toString() {
		String string = "";
		string += "X, D : <" + "\n";
		for (Variable variable : variables) {
			string += "\t" + variable.toString() + "\n";
		}
		string += ">" + "\n";
		string += "C, R : <" + "\n";
		for (Constraint constraint : constraints) {
			string += "\t" + constraint.toString() + "\n";
		}
		string += ">";
		return string;
	}
}
