package sat;

import java.util.ArrayList;
import utils.GenericCouple;

public class SAT {

	private int nbVariables, nbClauses;
	private ArrayList<Clause> clauses;
	private ArrayList<Litteral> litterals;
	private ArrayList<GenericCouple<Litteral>> tabPtrs;
	
	private int[] litteralsStates;
	private int[] choices;
	
	private int nbLitteralsSat;
	private int maxOccurences;
	
	public SAT(int nbVariables, int nbClauses, ArrayList<Clause> clauses, ArrayList<Litteral> litterals, int maxOccurences) {
		this.nbClauses   = nbClauses;
		this.nbVariables = nbVariables;
		this.clauses     = clauses;
		this.litterals   = litterals;
		litteralsStates  = new int [nbVariables];
		choices          = new int [nbVariables];
		nbLitteralsSat   = 0;
		this.maxOccurences = maxOccurences;
		initializePtrs();
	}

	public ArrayList<Clause> getClauses() {
		return clauses;
	}

	public ArrayList<Litteral> getLitterals() {
		return litterals;
	}
	
	public int getNbVariables() {
		return nbVariables;
	}
	
	public int getNbClauses() {
		return nbClauses;
	}
	
	public void initializePtrs() {
		tabPtrs = new ArrayList<GenericCouple<Litteral>>();
		for (Clause clause : clauses) {
			GenericCouple<sat.Litteral> ptrs;
			
			if (clause.size() > 1)
				ptrs = new GenericCouple<Litteral>(clause.get(0), clause.get(1));
			else 
				ptrs = new GenericCouple<Litteral>(clause.get(0), null);
			
			tabPtrs.add(ptrs);
			ptrs.getV1().addOccurence(clause);
			if (clause.size() > 1)
				ptrs.getV2().addOccurence(clause);
		}
	}
	
	public int getLitteralState(int index) {
		return litteralsStates[index];
	}
	
	public int[] getLitteralsStates() {
		return litteralsStates;
	}
	
	public Litteral getLitteral(int index) {
		return litterals.get(index);
	}
	
	public GenericCouple<Litteral> getCouplePtr(int index){
		return tabPtrs.get(index);
	}
	
	public void setCouplePtr(int index, Litteral x, Litteral y) {
		tabPtrs.set(index, new GenericCouple<Litteral>(x, y));
	}
	
	public int getChoice(int index) {
		return choices[index];
	}
	
	public int [] getChoises() {
		return choices;
	}
	
	public int getNbLitteralsSat() {
		return nbLitteralsSat;
	}
	
	public void setNbLitteralsSat(int nbLitteralsSat) {
		this.nbLitteralsSat = nbLitteralsSat;
	}
	
	@Override
	public String toString() {
		String str = "p cnf " + nbVariables + " " + nbClauses + "\n";
		int [] T = new int [nbVariables * 2];
		int v = 1;
		for (int i = 0 ; i < T.length ; i += 2) {
			T [i] = v;
			T [i+1] = v;
			v--;
		}
		for (Clause clause : clauses) {
			String c = "";
			for (Litteral li : clause.getLitterals()) {
				if (li.getId() % 2 == 0) {
					int l = li.getId() + T[li.getId()];
					c += l + " ";
				} else {
					int l = -1 * ((li.getId()-1) + T[li.getId()-1]);
					c += l + " ";
				}
			}
			str += c + "0\n";
		}
		return str;
	}
	
	public int getMaxOccurences() {
		return maxOccurences;
	}
}
