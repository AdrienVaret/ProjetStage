package solver_sat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import bincsp.BinCSP;
import bincsp.Variable;
import conversion.BinCSPConverter;
import generator.Generator;
import parser.Parser;
import sat.Clause;
import sat.Litteral;
import sat.SAT;
import utils.CreateCSP;
import utils.GenericCouple;
import utils.GenericCouple2;
import utils.Utils;

public class OldSolver {

	static long backtrackTime;
	static long findUnaffectedTime;
	static long propagationTime;
	static long selectCoupleTime;
	static long restoreTime;
	static long solveTime;
	static long finalTime;
	
	static boolean flagBT = false;
	static int id;
	static int nbNodes = 0;
	static boolean nodeState = true;
	
	public static Litteral negation(SAT sat, Litteral litteral) {
		if (litteral.getId() % 2 == 0)
			return sat.getLitteral(litteral.getId() + 1);
		else
			return sat.getLitteral(litteral.getId() - 1);
	}
	
	public static boolean estSat(SAT sat, Litteral l) {
		if ((l.getId() % 2 == 0 && sat.getLitteralState(l.getId()/2) == 1) ||
			(l.getId() % 2 == 1 && sat.getLitteralState((l.getId()-1)/2) == -1)) {
			return true;
		}
		return false;
	}
	
	public static GenericCouple2<Integer, Litteral> findUnafectedOrSatLitteral(SAT sat, Clause clause) {
		long begin = System.currentTimeMillis();
		
		Litteral l1 = sat.getCouplePtr(clause.getId()).getV1();
		Litteral l2 = sat.getCouplePtr(clause.getId()).getV2();
		
		/*
		int i = 0;
		for (Litteral l : clause.getLitterals()) {
			int id;
			if (l.getId() % 2 == 0)
				id = l.getId()/2;
			else
				id = (l.getId()-1)/2;
			
			// partir de la position 2
			if ( (!l.equals(l1) && !l.equals(l2) && 
					((sat.getLitteralState(id) == 0) || (estSat(sat, l)))))
					return new GenericCouple2<Integer, Litteral>(i,l);
			
			i ++;
		}*/
		
		for (int i = 2 ; i < clause.getLitterals().size() ; i++) {
			Litteral l = clause.get(i);
			int id;
			if (l.getId() % 2 == 0)
				id = l.getId()/2;
			else
				id = (l.getId()-1)/2;
			
			if (((sat.getLitteralState(id) == 0) || (estSat(sat, l)))){
				return new GenericCouple2<Integer, Litteral>(i,l);
			}
		}
		
		long end = System.currentTimeMillis();
		long time = end - begin;
		findUnaffectedTime += time;
		return new GenericCouple2<Integer, Litteral> (null, null);
	}
	
	public static boolean isAffected(SAT sat, Litteral l) {
		int id;
		if (l.getId() % 2 == 0)
			id = l.getId() / 2;
		else
			id = (l.getId() - 1) / 2;
		return sat.getLitteralState(id) != 0;
	}
	
	public static void affect(SAT sat, Litteral l) {
		if (l.getId() % 2 == 0)
			sat.getLitteralsStates()[l.getId()/2] = 1;
		else
			sat.getLitteralsStates()[(l.getId()-1)/2] = -1;
	}
	
	public static Result propagation(SAT sat, ArrayList<Litteral> L){
		long begin = System.currentTimeMillis();
		int [] propagateds = new int [sat.getNbVariables() * 2];
		int [] statesClauses = new int [sat.getNbClauses()];
		ArrayList<Litteral> F = new ArrayList<Litteral>();
		for (Litteral l : L) {
			propagateds[l.getId()] = 1;
			F.add(l);
		}
		
		while (L.size() > 0) {
			
			Litteral l = L.get(0);
			Litteral nl = negation(sat, l);
			
			if (l.getId() == 7)
				System.out.print("");
			
			for (Clause c : sat.getClauses()) {
				Litteral x = sat.getCouplePtr(c.getId()).getV1();
				Litteral y = sat.getCouplePtr(c.getId()).getV2();
				
				GenericCouple2<Integer, Litteral> coupleAff = findUnafectedOrSatLitteral(sat, c);
				Litteral affectable = coupleAff.getValue2();
				
				if (x.equals(nl) && affectable == null) { //c va devenir unitaire
					if (isAffected(sat, y)) {
						if (estSat(sat, y))
							statesClauses[c.getId()] = 1; //CLAUSE SATISFIED, DO_NOTHING
						else 
							return new Result(F, false);
					} else {
						if (propagateds[y.getId()] == 0) {
							propagateds[y.getId()] = 1;
							L.add(y);
							F.add(y);
						}
					}
		
				} else if (y.equals(nl) && affectable == null) { //c va devenir unitaire
					if (isAffected(sat, x)) {
						if (estSat(sat, x))
							statesClauses[c.getId()] = 1; //CLAUSE SATISFIED, DO_NOTHING
						else 
							return new Result(F, false);
					} else {
						if (propagateds[x.getId()] == 0) {
							propagateds[x.getId()] = 1;
							L.add(x);
							F.add(x);
						}
					}
					
				} else if (x.equals(nl) && affectable != null) {
					x.removeOccurence(c);
					Collections.swap(c.getLitterals(), 0, coupleAff.getValue1());
					sat.setCouplePtr(c.getId(), c.get(0), c.get(1));
					c.get(0).addOccurence(c);
				} else if (y.equals(nl) && affectable != null) {
					y.removeOccurence(c);
					Collections.swap(c.getLitterals(), 1, coupleAff.getValue1());
					sat.setCouplePtr(c.getId(), c.get(0), c.get(1));
					c.get(1).addOccurence(c);
				}
			}
			affect(sat, l);
			L.remove(l);
		}
		long end = System.currentTimeMillis();
		long time = end - begin;
		propagationTime += time;
		return new Result(F, true);
	}
	
	public static void restore(SAT sat, ArrayList<Litteral> L) {
		long begin = System.currentTimeMillis();
		for (Litteral l : L) {
			int id;
			if (l.getId() % 2 == 0)
				id = l.getId()/2;
			else
				id = (l.getId()-1)/2;
			sat.getLitteralsStates()[id] = 0;
		}
		long end = System.currentTimeMillis();
		long time = end - begin;
		restoreTime += time;
	}
	
	public static ArrayList<Litteral> propagationAll(SAT sat, ArrayList<Litteral> L1, ArrayList<Litteral> L2) {
		Result r1 = propagation(sat, L1);
		ArrayList<Litteral> X = r1.getLitterals();
		restore (sat, X);
		Result r2 = propagation(sat, L2);
		ArrayList<Litteral> Y = r2.getLitterals();
		restore (sat, Y);
		
		if (r1.getState() && !r2.getState())
			return X;
		else if (!r1.getState() && r2.getState())
			return Y;
		else if (!r1.getState() && !r2.getState())
			return null;
		else
			return Utils.intersection(sat, X, Y);
	}
	
	public static boolean canAffect(int [][] M, int id) {
		for (int i = 0 ; i < M[id].length ; i++) {
			if (M[id][i] == 0) return true;
		}
		return false;
	}
	
	public static GenericCouple<Integer> selectCouple(BinCSP csp, int[][] matrix, int id){
		long begin = System.currentTimeMillis();
		int index1 = -1, index2 = -1;
		
		for (int i = 0 ; i < csp.getVariables().get(id).getDomain().size() ; i++) {
			boolean affect = false;
			
			if (matrix[id][i] == 0 && index1 == -1 && !affect) {
				index1 = i;
				matrix[id][i] = 1;
				affect = true;
			}
			
			if (matrix[id][i] == 0 && index2 == -1 && !affect) {
				index2 = i;
				matrix[id][i] = 1;
				affect = true;
			}
			
			if (index1 != -1 && index2 != -1) break;
		} 
		
		long end = System.currentTimeMillis();
		long time = end - begin;
		selectCoupleTime += time;
		return new GenericCouple<Integer>(index1, index2);
	}
	
	public static int getIndex(Variable v, int i, int [] shift) {
		int begin = 2 * shift[v.getIndex()];
		return begin + (i*2);
	}

	public static Litteral getLitteral(BinCSP csp, SAT sat, int id, int i, int [] shift) {
		int index = getIndex(csp.getVariables().get(id), i, shift);
		return sat.getLitteral(index);
	}
	
	public static void clearMatrix(int [][] M, int id) {
		for (int i = 0 ; i < M[id].length ; i++) {
			M[id][i] = 0;
		}
	}
	
	public static boolean backtrack(SAT sat, ArrayList<Litteral> P, ArrayList<Integer> CP, int [][] M,
			                        ArrayList<Litteral> C, ArrayList<Integer> CC, int [] shift) {
		
		long begin = System.currentTimeMillis();
		nodeState = false;
		
		int n = CP.get(CP.size()-1);
		for (int i = 0 ; i < n ; i++) {
			Litteral l = P.get(P.size()-1);
			GenericCouple<Integer> c = getMatrixPosition(M, shift, l);
			M[c.getV1()][c.getV2()] = 0;
		}
		
		if (!canAffect(M, id)) {
			clearMatrix(M, id);
			id --;
			if (id < 0) 
				return false;
			if (!canAffect(M, id))
				flagBT = true;
		}
		
		if (CP.size() == 0 || (CP.size() == 1 && CP.get(0) == 0))
			return false;
		
		//int n = CP.get(CP.size()-1);
		CP.remove(CP.size()-1);
		ArrayList<Litteral> restore = new ArrayList<Litteral>();
		sat.setNbLitteralsSat(sat.getNbLitteralsSat() - n);
		for (int i = 0 ; i < n ; i++) {
			restore.add(P.get(P.size()-1));
			/**/
			//Litteral l = P.get(P.size()-1);
			//GenericCouple<Integer> c = getMatrixPosition(M, shift, l);
			//M[c.getV1()][c.getV2()] = 0;
			/**/
			P.remove(P.size()-1);
		}
		restore(sat, restore);
		
		n = CC.get(CC.size()-1);
		CC.remove(CC.size()-1);
		for (int i = 0 ; i < n ; i++) {
			Litteral c = C.get(C.size()-1);
			int cid;
			if (c.getId() % 2 == 0) cid = c.getId() / 2;
			else cid = (c.getId()-1) / 2;
			sat.getChoises()[cid] = 0;
			/**/
			
			/**/
			C.remove(C.size()-1);
		}		
		long end = System.currentTimeMillis();
		long time = end - begin;
		backtrackTime += time;
		return true;
	}
	
	public static boolean modelExists(BinCSP csp, SAT sat) {
		for (int i = 0 ; i < sat.getNbVariables() ; i++) {
			if (sat.getLitteralState(i) == 0 && sat.getChoice(i) == 0)
				return false;
		}
		return true;
	}
	
	public static int findUnafLitteral(SAT sat) {
		for (int i = 0 ; i < sat.getNbVariables() ; i++) {
			if (sat.getLitteralState(i) == 0) return i;
		}
		return -1;
	}
	
	
	public static void deductModel(SAT sat) {
		int i = findUnafLitteral(sat);
		while (i != -1) {
			i = i * 2;
			Litteral l = sat.getLitteral(i);
			ArrayList<Litteral> L = new ArrayList<Litteral>();
			L.add(l);
			Result r = propagation(sat, L);
			if (!r.getState()) {
				L.clear();
				i ++;
				l = sat.getLitteral(i);
				L.add(l);
				propagation(sat, L);
			}
			i = findUnafLitteral(sat);
		}
	}
	
	public static void displaySolution(BinCSP csp, SAT sat) {
		int i = 0;
		for (int v = 0 ; v < csp.getNbVariables() ; v++) {
			Variable x = csp.getVariables().get(v);
			for (String value : x.getDomain().getValues()) {
				if (sat.getLitteralState(i) == 1)
					System.out.println("# " + x.getName() + " = " + value);
				i++;
			}
		}
	}
	
	public static GenericCouple<Integer> getMatrixPosition(int [][] M, int [] shift, Litteral l){
	
		int idLitteral;
		if (l.getId() % 2 == 0) idLitteral = l.getId() / 2;
		else idLitteral = (l.getId() - 1) / 2;
		
		int line = -1;
		for (int i = 0 ; i < shift.length ; i++) {
			if (i < shift.length - 1) {
				if (shift[i] <= idLitteral && idLitteral < shift[i+1])
					line = i; 
			} else {
				if (shift[i] <= idLitteral)
					line = i;
			}
		}
		
		int column = idLitteral - shift[line];
		
		return new GenericCouple<Integer>(line, column);
	}
	
	public static void solve(BinCSP csp) {
		long begin = System.currentTimeMillis();
		SAT sat = BinCSPConverter.directEncoding(csp);
		
		int [] shift = new int[csp.getNbVariables() + 1]; //getNbConstraints avant ?!
		shift [0] = 0;
		int i = 1;
		int sum = 0;
		
		int maxDomain = 0;
		for (Variable v : csp.getVariables()) {
			int size = v.getDomain().size();
			sum += size;
			shift[i] = sum;
			if (size > maxDomain)
				maxDomain = size;
			i ++;
		}
		
		int [][] M = new int [csp.getNbVariables()][maxDomain];
		ArrayList<Litteral> P = new ArrayList<Litteral>();
		ArrayList<Integer> CP = new ArrayList<Integer>();
		ArrayList<Litteral> C = new ArrayList<Litteral>();
		ArrayList<Integer> CC = new ArrayList<Integer>();
		
		id = 0;
		
		while(true) {
			GenericCouple<Integer> couple = null;
			if (!flagBT)
				couple = selectCouple(csp, M, id);
			
			
			if (nodeState) 
				nbNodes ++;
			else 
				nodeState = true;
				
			if (!flagBT && couple.getV1() != -1 && couple.getV2() != -1) {
								
				Litteral x = getLitteral(csp, sat, id, couple.getV1(), shift);
				Litteral y = getLitteral(csp, sat, id, couple.getV2(), shift);
				Litteral nx = negation(sat, x);
				Litteral ny = negation(sat, y);
				
				C.add(x);
				C.add(y);
				CC.add(2);
				int xid, yid;
				if (x.getId() % 2 == 0) xid = x.getId() / 2;
				else xid = (x.getId() - 1) / 2;
				if (y.getId() % 2 == 0) yid = y.getId() / 2;
				else yid = (y.getId() - 1) / 2;
				sat.getChoises()[xid] = 1;
				sat.getChoises()[yid] = 1;
				
				ArrayList<Litteral> L1 = new ArrayList<Litteral>(Arrays.asList(new Litteral[] {
						x, ny
				}));
				ArrayList<Litteral> L2 = new ArrayList<Litteral>(Arrays.asList(new Litteral[] {
						nx, y
				}));
				
				ArrayList<Litteral> L = propagationAll(sat, L1, L2);
			
				if (L == null) {
					CP.add(0);
					if (!backtrack(sat, P, CP, M, C, CC, shift)) {
						System.out.println("# UNSATISFIABLE");
						System.out.println("nb_nodes : " + nbNodes);
						long end = System.currentTimeMillis();
						solveTime = end - begin;
						break;
					}
				} else {
					Result r = propagation(sat, L);
					P.addAll(r.getLitterals());
					/**/
					for (Litteral l : P) {
						GenericCouple<Integer> c = getMatrixPosition(M, shift, l);
						M[c.getV1()][c.getV2()] = -1;
					}
					/**/
					CP.add(r.getLitterals().size());
					sat.setNbLitteralsSat(sat.getNbLitteralsSat() + r.getLitterals().size());
					if (modelExists(csp,sat)) {
						System.out.println("# SATISFIABLE");
						deductModel(sat);
						displaySolution(csp, sat);
						System.out.println("nb_nodes : " + nbNodes);
						long end = System.currentTimeMillis();
						solveTime = end - begin;
						break;
					}
					id ++;
				}
			} else if (!flagBT && couple.getV1() != -1 && couple.getV2() == -1) {
				
				if (nodeState) nbNodes ++;
				else nodeState = true;
				
				Litteral x = getLitteral(csp, sat, id, couple.getV1(), shift);
				
				C.add(x);
				CC.add(1);
				int xid;
				if (x.getId() % 2 == 0) xid = x.getId() / 2;
				else xid = (x.getId() - 1) / 2;
				sat.getChoises()[xid] = 1;
				
				ArrayList<Litteral> L1 = new ArrayList<Litteral>();
				L1.add(x);
				Result r = propagation(sat, L1);
				if (r.getState() == false) {
					CP.add(0);
					if (!backtrack(sat, P, CP, M, C, CC, shift)) {
						System.out.println("# UNSATISFIABLE");
						System.out.println("nb_nodes : " + nbNodes);
						long end = System.currentTimeMillis();
						solveTime = end - begin;
						break;
					}
				} else {
					P.addAll(r.getLitterals());
					/**/
					for (Litteral l : P) {
						GenericCouple<Integer> c = getMatrixPosition(M, shift, l);
						M[c.getV1()][c.getV2()] = 1;
					}
					/**/
					CP.add(r.getLitterals().size());
					sat.setNbLitteralsSat(sat.getNbLitteralsSat() + r.getLitterals().size());
					if (modelExists(csp,sat)) {
						System.out.println("# SATISFIABLE");
						System.out.println("nb_nodes : " + nbNodes);
						long end = System.currentTimeMillis();
						solveTime = end - begin;
						displaySolution(csp, sat);
						break;
					}
					id ++;
					if (id == 4)
						System.out.print("");
				}
			} else if (!flagBT && couple.getV1() == -1 && couple.getV2() == -1) {
				if (!backtrack(sat, P, CP, M, C, CC, shift)) {
					System.out.println("# UNSATISFIABLE");
					System.out.println("nb_nodes : " + nbNodes);
					long end = System.currentTimeMillis();
					solveTime = end - begin;
					break;
				};
			} else if (flagBT) {
				flagBT = false;
				if (!backtrack(sat, P, CP, M, C, CC, shift)) {
					System.out.println("# UNSATISFIABLE");
					System.out.println("nb_nodes : " + nbNodes);
					long end = System.currentTimeMillis();
					solveTime = end - begin;
					break;
				};
			}
		}
	}
	
	public static void displayTime() {
		System.out.println("propagation time : " + propagationTime + " ms");
		System.out.println("backtrack time : " + backtrackTime + " ms");
		System.out.println("restoreTime : " + restoreTime + " ms");
		System.out.println("selectCoupleTime : " + selectCoupleTime + " ms");
		System.out.println("findUnaffTime : " + findUnaffectedTime + " ms");
		System.out.println("solveTime : " + solveTime + " ms");
		System.out.println("finalTime : " + finalTime + " ms");
	}
	
	public static void main(String [] args) {
		/*Parser p;
		try {
			long begin = System.currentTimeMillis();
			p = new Parser("file.xml");
			BinCSP csp = p.buildCSP();
			csp = BinCSPConverter.convertToConflicts(csp);
			long end = System.currentTimeMillis();
			float parseTime = ((float)end - (float)begin)/(float)1000;
			begin = System.currentTimeMillis();			
			solve(csp);
			end = System.currentTimeMillis();
			float solveTime = ((float)end - (float)begin)/(float)1000;
			displayTime();
		} catch (Exception e) {
			e.printStackTrace();
		} */

		long begin = System.currentTimeMillis();
		BinCSP csp = Generator.generatePigeons(6,5);
		solve(csp); 
		csp.exportToXCSP3(csp, "output.xml"); 
		long end = System.currentTimeMillis();
		finalTime = end - begin;
		displayTime();
	}
}