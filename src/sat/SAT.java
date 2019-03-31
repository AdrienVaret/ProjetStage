package sat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import utils.GenericCouple;

public class SAT {

	private int nbVariables, nbClauses;
	private ArrayList<Clause> clauses;
	private ArrayList<Litteral> litterals;
	private ArrayList<GenericCouple<Litteral>> tabPtrs;
	private int[] litteralsStates;
	private int maxOccurences;
	
	public SAT(int nbVariables, int nbClauses, ArrayList<Clause> clauses, ArrayList<Litteral> litterals, int maxOccurences) {
		this.nbClauses   = nbClauses;
		this.nbVariables = nbVariables;
		this.clauses     = clauses;
		this.litterals   = litterals;
		litteralsStates  = new int [nbVariables];
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
	
	public void exportToCNFFile(String outputFileName) {
		String toString = toString();
		String [] arrayToString = toString.split("\n");
		
		File outputFile = new File(outputFileName);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			for (String line : arrayToString) {
				writer.write(line + "\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void parseCNFFile(String path) {
		try {
			BufferedReader r = new BufferedReader(new FileReader(new File(path)));
			String line = null;
			while ((line = r.readLine()) != null) {
				String [] splittedLine = line.split(" ");
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
