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
	private Litteral [] litterals;
	private ArrayList<GenericCouple<Litteral>> tabPtrs;
	private int[] litteralsStates;
	private int maxOccurences;
	
	public SAT(int nbVariables, int nbClauses, ArrayList<Clause> clauses, Litteral [] litterals, int maxOccurences) {
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

	public Litteral[] getLitterals() {
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
		return litterals[index];
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
	
	public static SAT parseCNFFile(String path) {
		
		long begin = System.currentTimeMillis();
		
		int maxOccurences = 0;
		int nbVariables = -1;
		int nbClauses = -1;
		int nbClausesDomain = 0;
		Litteral [] litterals = null; 
		ArrayList<Clause> clauses = new ArrayList<Clause>();
		
		int [] occurences = null;
		try {			
			
			BufferedReader r = new BufferedReader(new FileReader(new File(path)));
			String line = null;
			int clausesCreateds = 0;
			
			while ((line = r.readLine()) != null) {
				String [] splittedLine = line.split(" ");
				
				if (splittedLine[0].equals("p")) {
					nbVariables = Integer.parseInt(splittedLine[2]);
					nbClauses = Integer.parseInt(splittedLine[3]);
					occurences = new int[nbVariables * 2];
					litterals = new Litteral[nbVariables * 2];
					for (int i = 0 ; i < nbVariables * 2 ; i += 2) {
						litterals[i] = new Litteral(i);
						litterals[i+1] = new Litteral(i+1);
					}
				}
				
				else if (splittedLine[0] != "c"){
					//Clause clause = new Clause(clausesCreateds);
					ArrayList<Litteral> litts = new ArrayList<Litteral>();
					boolean clauseDomain = true;
					for (int i = 0 ; i < splittedLine.length - 1 ; i++) {
						if (splittedLine[i].charAt(0) == '-') clauseDomain = false;
						int intLitteral = Integer.parseInt(splittedLine[i]);
						
						int index;
						if (intLitteral < 0) {
							index = (2 * ((-1 * intLitteral) - 1)) + 1;
						} else {
							index = (2 * (intLitteral - 1));
						}
						
						occurences[index] ++;
						//clause.addLitteral(litterals[index]);
						litts.add(litterals[index]);
					}
					clauses.add(new Clause(clausesCreateds, litts));
					if (clauseDomain) {
						for (int i = 0 ; i < splittedLine.length - 1 ; i++) {
							int intLitteral = Integer.parseInt(splittedLine[i]);
							
							int index;
							if (intLitteral < 0) {
								index = (2 * ((-1 * intLitteral) - 1)) + 1;
							} else {
								index = (2 * (intLitteral - 1));
							}
							
							litterals[index].setIdVariable(clausesCreateds);
						}
						nbClausesDomain ++;
					}			
					clausesCreateds ++;
				}
			}
			
			for (int i = 0 ; i < occurences.length ; i++) {
				if (occurences[i] > maxOccurences)
					maxOccurences = occurences[i];
			}
			
			r.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		long end = System.currentTimeMillis();
		long time = end - begin;
		
		System.out.println("read instance : " + path);
		System.out.println(nbVariables + " variables, " + nbClauses + " clauses");
		System.out.println(nbClausesDomain + " domain clauses : ");
		
		for (int i = 0 ; i < nbClausesDomain ; i++) {
			System.out.println("C" + (i+1) + "\t---\t" + clauses.get(i).toString());
		}
		
		System.out.println("parse time : " + time + " ms.");
		
		return new SAT(nbVariables, nbClauses, clauses, litterals, maxOccurences);
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
