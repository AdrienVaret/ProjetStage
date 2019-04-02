package solver_sat;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import bincsp.BinCSP;
import bincsp.Variable;
import conversion.BinCSPConverter;
import generator.Generator;
import sat.Clause;
import sat.Litteral;
import sat.SAT;
import utils.Cause;
import utils.GenericCouple;
import utils.Utils;

public class Solver2 {

	static BufferedWriter w;
	
	public enum Action {
		HEURISTIC, SAME_VARIABLE
	}
	
	static BinCSP csp;
	static SAT sat;
	
	/*
	 * Flags
	 */
	static boolean flagAllSolutions = false;
	static boolean flagDomHeuristic = true;
	static boolean flagDegHeuristic;
	static boolean flagSymetries = false;
	static boolean flagNoMoreSymmetries = false;
	static boolean flagSupport = false;
	static boolean flagDisplay = true;
	
	/*
	 * Time declarations
	 */
	static long backtrackTime;
	static long findUnaffectedTime;
	static long propagationTime;
	static long selectCoupleTime;
	static long restoreTime;
	static long solveTime;
	static long finalTime;
	static long timeComputeSolution;
	
	/*
	 * Statics variable
	 */
	static boolean nodeState = true;
	static int nbNodes = 0;
	static int [] domainsSizes;
	static int [] variablesStates;
	static int nbVariablesSat = 0;
	static GenericCouple<Litteral> couple;
	static int idClause;
	static Litteral [][] explicitsPropagations;
	static int [] iEP;
	static int [] affectations;
	static Action action = Action.HEURISTIC;
	static int [][] occ;
	static ResultPropagation result;
	
	static Litteral [] LP, PA, P, C, L1, L2;
	static int iLP = 0, iA = 0, iP = 0, iC = 0;
	
	static ArrayList<Integer> CP = new ArrayList<Integer>();
	static ArrayList<Integer> CC = new ArrayList<Integer>();
	static int [] propagateds;
	
	static Litteral x, y, nx, ny;
	
	/*
	 * Variables used for breaking symmetries
	 */
	static int [] state;
	static int V;
	static Litteral [] SP; //Symetries propagations
	static ArrayList<Integer> countSP;
	static int iSP;
		
	/*
	 * Utilitaries
	 */
	public static void clearEP(int index) {
		for (int i = 0 ; i < iEP[index] ; i++)
			explicitsPropagations[index][i] = null;
		iEP[index] = 0;
	}
	
	public static void clearLP() {
		for (int i = 0 ; i < iLP ; i++) LP[i] = null;
		iLP = 0;
	}
	
	/**
	 * Converts litteral's id into litteral's value
	 * @param x
	 * @return litteral's value
	 */
	public static int getIndex(int x) {
		return x >> 1;
	}
	

	static Litteral [][] ep;
	static int [] iep;
	static int idC, idCC;
	static ArrayList<Integer> cp;
	static int ip, icp;
	static Litteral [] p;
	static Litteral [] c;
	static int ic;
	static ArrayList<Litteral []> solutions = new ArrayList<Litteral []>();
	
	public static int backtrackMultipleSolution() {
	
		int n = cp.get(cp.size()-1);
		cp.remove(cp.size()-1);
		 
		for (int i = 0 ; i < n ; i++) {
			Litteral l = p[ip-1];
			restore(l);
			p[ip-1] = null;
			ip --;
		}
		
		Litteral l = negation(c[ic-1]);
		Litteral [] L = new Litteral [sat.getNbVariables() * 2]; 
		L[0] = l;
		
		boolean r = bcp(L, false);
		
		c[ic - 1] = null;
		ic --;
		
		for (int i = 0 ; i < L.length ; i++) {
			if (L[i] == null) break;
			ep[idCC][iep[idCC]] = L[i];
			iep[idCC] ++;
			L[i] = null;
		}

		while (!r) {
			if (idCC == 0) return -1;
			else {
				int max = iep[idCC];
				for (int i = 0 ; i < max ; i++) {
					Litteral toRestore = ep[idCC][i];
					restore (toRestore);
				}
								
				iep[idCC] = 0;
				idCC --;
				int nb = CC.get(idCC);
				idC -= nb;
			}
			
			n = cp.get(cp.size()-1);
			cp.remove(cp.size()-1);
			
			for (int i = 0 ; i < n ; i++) {
				l = p[ip-1];
				restore(l);
				p[ip-1] = null;
				ip --;
			}
			
			if (ic > 0) {
				l = negation(c[ic-1]);
				L = new Litteral [sat.getNbVariables() * 2]; 
				L[0] = l;
			
				r = bcp(L, false);
			
				c[ic - 1] = null;
				ic --;
				
				for (int i = 0 ; i < L.length ; i++) {
					if (L[i] == null) break;
					ep[idCC][iep[idCC]] = L[i];
					iep[idCC] ++;
					L[i] = null;
				}
			}
		}
		return 1;
	}
	
	public static void deductMultipleSolution() {
		
		long begin = System.currentTimeMillis();
				
		ep = new Litteral [csp.getNbVariables()][sat.getNbVariables()*2]; 
		iep = new int [csp.getNbVariables()];
		
		idC = 0;
		idCC = 0;
		Litteral l1, l2;
		p = new Litteral [sat.getNbVariables() * 2];
		c = new Litteral [sat.getNbVariables() * 2];
		cp = new ArrayList<Integer>();
		ic = 0;
		ip = 0; 
		icp = 0;
		
		Litteral [] L = new Litteral [sat.getNbVariables() * 2];
		Litteral [] solution = new Litteral[csp.getNbVariables()];
		
		
		while (true) {

			if (idCC >= CC.size()) break;
						
			int n = CC.get(idCC);	
			
			if (n == 2) {
				l1 = C[idC];
				l2 = C[idC + 1];
			} else {
				l1 = C[idC];
				l2 = null;
			}
			
			Litteral l = null;
			if (!(l1 == null && l2 == null)) {
				if (sat.getLitteralsStates()[getIndex(l1.getId())] != -1) {
					l = l1;
				} else if (l2 != null && sat.getLitteralsStates()[getIndex(l2.getId())] != -1) {
					l = l2;
				} else {
					l = null;
				}
			}
			
			if (l != null) {
				c[ic] = l;
				ic ++;
				L[0] = l;
				boolean r = bcp(L, false);

				int size = 0;
				for (int i = 0 ; i < L.length ; i++) {
					if (L[i] == null) break;
					p[ip] = L[i];
					ip ++;
					size ++;
					L[i] = null;
				}
				
				cp.add(size);
				
				if (r == true && cp.size() == CC.size()) {
					int index = 0;
					int indexL = 0;
					for (int i = 0 ; i < sat.getNbVariables() ; i++) {
						if (sat.getLitteralsStates()[i] == 1) {
							solution[index] = sat.getLitteral(indexL);
							index ++;
						}
						indexL += 2;
							
					}
					solutions.add(new Litteral[solution.length]);
					for (int i = 0 ; i < solution.length ; i++ ) {
						solutions.get(solutions.size() - 1)[i] = solution[i];
					}
					
					if (flagAllSolutions) {
						if (backtrackMultipleSolution() == -1) {
							break;
						}
					} else {
						break;
					}
				} else if (r == false) {
					if (backtrackMultipleSolution() == -1) {
						break;
					}
				}
				else {
					idC += n;
					idCC ++;
				}
				
			} else {
				cp.add(0);
				if (backtrackMultipleSolution() == -1) {
					break;
				}
			}
		}

		int max = iep[0];
		for (int i = 0 ; i < max ; i++) {
			Litteral toRestore = ep[0][i];
			restore(toRestore);
		}
		
		long end = System.currentTimeMillis();
		timeComputeSolution += (end - begin);
	}
		
	/**
	 * Return litteral's negation
	 * @param sat
	 * @param litteral
	 * @return litteral
	 */
	public static Litteral negation(Litteral litteral) {
		if (litteral.getId() % 2 == 0)
			return sat.getLitteral(litteral.getId() + 1);
		else
			return sat.getLitteral(litteral.getId() - 1);
	}
	
	/**
	 * Check if a litteral is affected
	 * @param sat
	 * @param l
	 * @return boolean
	 */
	public static boolean isAffected(Litteral l) {
		return sat.getLitteralState(getIndex(l.getId())) != 0;
	}
	
	/**
	 * Select one or two values from the variable at index position
	 * @param sat
	 * @param index
	 * @return
	 */
	public static GenericCouple<Litteral> selectCouple(int index){
		
		long begin = System.currentTimeMillis();
		
		if (index < 0) 
			return new GenericCouple<Litteral>(null, null);
		
		Litteral l1 = sat.getCouplePtr(index).getV1();
		Litteral l2 = sat.getCouplePtr(index).getV2();
		
		if (l1 != null && l2 == null) {
			int l1id = getIndex(l1.getId());
			
			if (sat.getLitteralState(l1id) != -1) {
				long end = System.currentTimeMillis();
				selectCoupleTime += (end - begin);
				nbNodes ++;	
				return new GenericCouple<Litteral> (l1, null);
			} else {
				long end = System.currentTimeMillis();
				selectCoupleTime += (end - begin);
				return new GenericCouple<Litteral> (null, null);
			}
		}
		
		else if (l1 != null && l2 != null) {
			int l1id = getIndex(l1.getId());
			int l2id = getIndex(l2.getId());
			
			if (sat.getLitteralState(l1id) != -1 && sat.getLitteralState(l2id) != -1) {
				long end = System.currentTimeMillis();
				selectCoupleTime += (end - begin);
				nbNodes ++;	
				return new GenericCouple<Litteral> (l1, l2);
			}
		
			else if (sat.getLitteralState(l1id) != -1 && sat.getLitteralState(l2id) == -1) {
				long end = System.currentTimeMillis();
				selectCoupleTime += (end - begin);
				nbNodes ++;	
				return new GenericCouple<Litteral> (l1, null);
			}
		
			else if (sat.getLitteralState(l1id) == -1 && sat.getLitteralState(l2id) != -1) {
				long end = System.currentTimeMillis();
				selectCoupleTime += (end - begin);
				nbNodes ++;	
				return new GenericCouple<Litteral> (l2, null);
			}
		
			else {
				long end = System.currentTimeMillis();
				selectCoupleTime += (end - begin);
				return new GenericCouple<Litteral> (null, null);
			}
		}

		long end = System.currentTimeMillis();
		selectCoupleTime += (end - begin);
		return new GenericCouple<Litteral> (null, null);
	}
	
	/**
	 * Return variable's index who have minimal domain size
	 * @return int
	 */
	public static int domHeuristic() {
		int minSize = Integer.MAX_VALUE;
		int index = -1;
		for (int i = 0 ; i < domainsSizes.length ; i++) {
			if (variablesStates[i] == 0) {
				if (domainsSizes[i] < minSize) {
					minSize = domainsSizes[i];
					index = i;
				}
			}
		}
		return index;
	}
	
	public static int degHeuristic() {
		int maxDegree = -1;
		int index = -1;
		for (int i = 0 ; i < domainsSizes.length ; i++) {
			if (variablesStates[i] == 0) {
				if (csp.getDegrees()[i] > maxDegree) {
					maxDegree = csp.getDegrees()[i];
					index = i;
				}
			}
		}
		return index;
	}
	
	/**
	 * Check if a litteral is satisfied
	 * @param sat
	 * @param l
	 * @return boolean
	 */
	public static boolean isSat(Litteral l) {	
		if ((l.getId() % 2 == 0 && sat.getLitteralState(getIndex(l.getId())) == 1) ||
			(l.getId() % 2 == 1 && sat.getLitteralState(getIndex(l.getId())) == -1)) {
				return true;
			}
		return false;
	}
	
	/**
	 * Affect a litteral
	 * @param sat
	 * @param l
	 */
	public static void affect(Litteral l) {
		if (l.getId() % 2 == 0)
			sat.getLitteralsStates()[getIndex(l.getId())] = 1;
		else
			sat.getLitteralsStates()[getIndex(l.getId())] = -1;
	}
	
	
	public static int findUnafectedOrSatLitteral(Clause clause) {
		long begin = System.currentTimeMillis();
		
		for (int i = 2 ; i < clause.getLitterals().size() ; i++) {
			Litteral l = clause.get(i);
			int id = getIndex(l.getId());			
			if (((sat.getLitteralState(id) == 0) || (isSat(l)))) return i;			
		}
		
		long end = System.currentTimeMillis();
		long time = end - begin;
		findUnaffectedTime += time;
		return -1;
	}
	

	public static boolean bcp(Litteral [] L, boolean intersection) {
		
		long begin = System.currentTimeMillis();
		int idLitteral = 0;
		
		for (Litteral l : L) {
			if (l == null) break;
			idLitteral ++;
			propagateds[l.getId()] = 1;
		}
		
		int indexLitteral = 0;
		Litteral l = L[indexLitteral], nl;
		
		while (l != null) {
			nl = negation(l);
			int toShift = 0;
			for (int i = 1 ; i < occ[nl.getId()][0] + 1 ; i++) {
				Clause c = sat.getClauses().get(occ[nl.getId()][i]);
				Litteral x = sat.getCouplePtr(c.getId()).getV1();
				Litteral y = sat.getCouplePtr(c.getId()).getV2();
				
				int iUnaff = findUnafectedOrSatLitteral(c);
				Litteral affectable;
				if (iUnaff == -1)
					affectable = null;
				else 
					affectable = c.get(iUnaff);
				
				if (affectable == null) {
					if (x != null && x.equals(nl)) {
						
						if (y == null) {
							result.setState(false);							
							Utils.shiftAll(occ[nl.getId()], toShift);
							toShift = 0;
							
							for (int index = 0 ; index < L.length ; index++) {
								if (L[index] == null) break;
								propagateds[L[index].getId()] = 0;
							}
							
							long end = System.currentTimeMillis();
							propagationTime += (end-begin);
							
							return false;
						}
						
						if (isAffected(y)) {
							if (!isSat(y)) {
								result.setState(false);
								Utils.shiftAll(occ[nl.getId()], toShift);
								toShift = 0;
								indexLitteral ++;
								
								for (int index = 0 ; index < L.length ; index++) {
									if (L[index] == null) break;
									propagateds[L[index].getId()] = 0;
								}
								
								long end = System.currentTimeMillis();
								propagationTime += (end-begin);	
								return false;
							}
						} else {
							if (propagateds[y.getId()] == 0) {
								propagateds[y.getId()] = 1;

								L[idLitteral] = y;
								idLitteral ++;	
								
								if (intersection)
									result.incr(y.getId());							
								
								if (intersection && result.get(y.getId()) == 2) {
									LP[iLP] = y;
									iLP ++;
								}						
								
							}
						}
					} else if (y != null && y.equals(nl)) {
						if (isAffected(x)) {
							if (!isSat(x)) {
								result.setState(false);		
								Utils.shiftAll(occ[nl.getId()], toShift);
								toShift = 0;
								
								for (int index = 0 ; index < L.length ; index++) {
									if (L[index] == null) break;
									propagateds[L[index].getId()] = 0;
								}
								
								long end = System.currentTimeMillis();
								propagationTime += (end-begin);
								
								return false;
							}
						} else { 
							if (propagateds[x.getId()] == 0) {
								propagateds[x.getId()] = 1;
								
								L[idLitteral] = x;
								idLitteral ++;
								
								if (intersection)
									result.incr(x.getId());
								
								if (intersection && result.get(x.getId()) == 2) {
									LP[iLP] = x;
									iLP ++;
								}		
							}
						}
					}
				} else {
					if (x.equals(nl)) {

						int index = i;
						
						occ[nl.getId()][index] = -1;
						toShift ++;
						
						int lid = affectable.getId();
						
						int size = occ[lid][0];
						occ[lid][size+1] = c.getId();
						occ[lid][size+2] = -1;
						occ[lid][0] ++;
						
						Collections.swap(c.getLitterals(), 0, iUnaff);
						sat.setCouplePtr(c.getId(), c.get(0), c.get(1));
						
					} else if (y.equals(nl)){
						
						int index = i;

						occ[nl.getId()][index] = -1;
						toShift ++;
						
						int lid = affectable.getId();
						
						int size = occ[lid][0];
						occ[lid][size+1] = c.getId();
						occ[lid][size+2] = -1;
						occ[lid][0] ++;
						
						Collections.swap(c.getLitterals(), 1, iUnaff);
						sat.setCouplePtr(c.getId(), c.get(0), c.get(1));
					}
				
				}
			}
			Utils.shiftAll(occ[nl.getId()], toShift);
			toShift = 0;
			affect(l);
			indexLitteral ++;
			l = L[indexLitteral];
		}
		
		for (int index = 0 ; index < L.length ; index++) {
			if (L[index] == null) break;
			propagateds[L[index].getId()] = 0;
		}
		
		long end = System.currentTimeMillis();
		propagationTime += end - begin;
		
		return true;
	}	
	
	/**
	 * Restore one affectation
	 * @param sat
	 * @param l
	 */
	public static void restore(Litteral l) {
		long begin = System.currentTimeMillis();
		int id = getIndex(l.getId());
		sat.getLitteralsStates()[id] = 0;
		long end = System.currentTimeMillis();
		long time = end - begin;
		restoreTime += time;
	}
	
	/**
	 * Restore many affectations
	 * @param sat
	 * @param L
	 */
	public static void restoreAll(ArrayList<Litteral> L) {
		long begin = System.currentTimeMillis();
		for (Litteral l : L) {
			restore(l);
		}
		long end = System.currentTimeMillis();
		long time = end - begin;
		restoreTime += time;
	}
	
	public static void restoreAll(Litteral [] L) {
		long begin = System.currentTimeMillis();
		
		for (Litteral l : L) {
			if (l == null) break;
			restore(l);
		}
		
		long end = System.currentTimeMillis();
		long time = end - begin;
		restoreTime += time;
	}
	
	public static int testAndFilter() {
		boolean result1 = bcp(L1, true);
		restoreAll(L1);
		boolean result2 = bcp(L2, true);
		restoreAll(L2);
		
		result.clear();
		
		if (result1 && !result2) return 1;	
		else if (!result1 && result2) return 2;		
		else if (!result1 && !result2) return 3;	
		else return 4;
	}
	
	public static boolean backtrack() {

		long begin = System.currentTimeMillis();
		
		nodeState = false;
		
		if (CP.size() == 0 || (CP.size() == 1 && CP.get(0) == 0)) {
			long end = System.currentTimeMillis();
			backtrackTime += (end - begin);
			return false;
		}
		
		int nbLitterals = CP.get(CP.size()-1);
		CP.remove(CP.size()-1);
		int nbChoices = CC.get(CC.size()-1);
		CC.remove(CC.size()-1);
		
		if(idClause == -1)
			return false;
		
		variablesStates[idClause] = 0;
		
		for (int i = 0 ; i < nbLitterals ; i++) {
			Litteral l = P[iP-1];
			restore(l);
			P[iP-1] = null;
			iP --;
			if (l.getId() % 2 == 1)
				domainsSizes[l.getIdVariable()] ++;
		}
		
		int iL = 0;
		
		for (int i = 0 ; i < nbChoices ; i++) {
			Litteral l = C[iC-1];
			L1[iL] = negation(l);
			iL ++;
			C[iC-1] = null;
			iC --;
		}
		
		boolean r;
		
		if (iL == 1)
			r = false;
		else if (domainsSizes[idClause] == 0)
			r = false;
		else if (iL == 2 && domainsSizes[idClause] == 2)
			r = false;
		else
			r = bcp(L1, false);
			
		for (int i = 0 ; i < L1.length ; i++) {
			if (L1[i] == null) break;
			explicitsPropagations[idClause][iEP[idClause]] = L1[i];
			iEP[idClause] ++;
		}
			
		Utils.clearArray(L1);
		
		if (!r) {
			while (!r) {
				restoreAll(explicitsPropagations[idClause]);
				
				affectations[iA-1] = -1;
				iA --;
				
				if (iA == 0) {
					long end = System.currentTimeMillis();
					backtrackTime += (end - begin);
					return false;
				}
				
				clearEP(idClause);
				variablesStates[idClause] = 0;
				idClause = affectations[iA - 1];
				nbVariablesSat --;
				if (nbVariablesSat < 0) {
					long end = System.currentTimeMillis();
					backtrackTime += (end - begin);
					return false;
				}
				
				if (CP.size() == 0 || (CP.size() == 1 && CP.get(0) == 0)) {
					long end = System.currentTimeMillis();
					backtrackTime += (end - begin);
					return false;
				}
								
				nbLitterals = CP.get(CP.size()-1);
				CP.remove(CP.size()-1);
				nbChoices = CC.get(CC.size()-1);
				CC.remove(CC.size()-1);
				
				for (int i = 0 ; i < nbLitterals ; i++) {
					Litteral l = P[iP-1];
					restore(l);
					P[iP-1] = null;
					iP --;
					
					if (l.getId() % 2 == 1)
						domainsSizes[l.getIdVariable()] ++;
				}
				
				iL = 0;
				for (int i = 0 ; i < nbChoices ; i++) {
					Litteral l = C[iC-1];
					L1[iL] = negation(l);
					iL++;
					C[iC-1] = null;
					iC --;
				}
				
				if (iL == 1)
					r = false;
				else if (domainsSizes[idClause] == 0)
					r = false;
				else if (iL == 2 && domainsSizes[idClause] == 2)
					r = false;
				else
					r = bcp(L1, false);
					
				for (int i = 0 ; i < L1.length ; i++) {
					if (L1[i] == null) break;
						explicitsPropagations[idClause][iEP[idClause]] = L1[i];
						iEP[idClause] ++;
				}
				Utils.clearArray(L1);
			}
			
			nodeState = true;
			action = Action.SAME_VARIABLE;
			long end = System.currentTimeMillis();
			backtrackTime += (end - begin);
			return true;
		} else {
			action = Action.SAME_VARIABLE;
			long end = System.currentTimeMillis();
			backtrackTime += (end - begin);
			return true;
		}	
	}
	
	public static boolean modelExists() {
		if (nbVariablesSat == csp.getNbVariables()) return true;
		return false;
	}
	
	/**
	 * Initialize occurences matrix
	 * @param sat
	 */
	public static void initializeOcc() {
		
		for (int i = 0 ; i < sat.getNbVariables() * 2 ; i++) {
			for (int j = 1 ; j < occ[i].length ; j++) {
				occ[i][j] = -1; 
			}
		}
		
		for (int i = 0 ; i < sat.getNbClauses() ; i++) {
			Clause c = sat.getClauses().get(i);
			
			if (c.size() > 1) {
				Litteral x = c.get(0);
				Litteral y = c.get(1);
			
				sat.setCouplePtr(i, x, y);
			
				occ[x.getId()][(occ[x.getId()][0]) + 1] = i;
				occ[x.getId()][0] ++;
				occ[x.getId()][(occ[x.getId()][0]) + 1] = -1;
			
				occ[y.getId()][(occ[y.getId()][0]) + 1] = i;
				occ[y.getId()][0] ++;
				occ[y.getId()][(occ[y.getId()][0]) + 1] = -1;
			}
			
			if (c.size() == 1) {
				Litteral x = c.get(0);
				sat.setCouplePtr(i, x, null);
				
				occ[x.getId()][(occ[x.getId()][0]) + 1] = i;
				occ[x.getId()][0] ++;
				occ[x.getId()][(occ[x.getId()][0]) + 1] = -1;
			}
		}
	}
	
	public static void displayAllSolutions(SAT sat, BinCSP csp) {
		System.out.println("# " + solutions.size() + " solutions found");
		int nbSolutions = 1;
		for (Litteral [] L : solutions) {
			if (flagDisplay) 
				System.out.print("# Solution " + nbSolutions + " : [");
			for (Litteral l : L) {
				if (flagDisplay)
					System.out.print(l.toString() + " ");
			}
			if (flagDisplay)
				System.out.println("]");
				System.out.println("##");
			nbSolutions ++;
		}
	}
	
	public static int findColor(int id) {
		int d = csp.getVariables().get(0).getDomain().size();
		for (int i = 0 ; i < csp.getNbVariables() ; i++) {
			int x = ((i+1)*(2*d))-1;
			if (x >= id) {
				int color = (id - (i * (2 * d))) / 2;
				return color;
			}
		}
		return -1;
	}
	
	
	
	public static int breakSymmetries() {
		//Compute symetrics colors
		ArrayList<Integer> symetricsColors = new ArrayList<Integer>();
		for (int i = 0 ; i < state.length ; i++) {
			if (state[i] == V) symetricsColors.add(i);
		}
		
		if (symetricsColors.size() == 0) {
			flagNoMoreSymmetries = true;
			return 0;
		}
		
		int d = csp.getVariables().get(0).getDomain().size();
		
		//Delete all symetrics values except one for current variable
		Litteral [] L1 = new Litteral[sat.getNbVariables() * 2];
		int iL1 = 0;
		for (int i = 1 ; i < symetricsColors.size() ; i++) {
			int color = symetricsColors.get(i);
			int indexX = (idClause * (2 * d)) + (2 * color); 
			Litteral x = negation(sat.getLitteral(indexX));
			L1[iL1] = x;
			iL1 ++;
		}
		
		boolean r = bcp(L1, false);
		V--;
		
		if (!r) return -1;
		
		int size = 0;
		for (int i = 0 ; i < L1.length ; i++) {
			if (L1[i] == null) break;
			Litteral x = L1[i];
			int indexX = x.getId();
			if (indexX % 2 != 0) { //if a negation is propagated
					int color = findColor(indexX - 1);
					state[color] --;
			}
			SP[iSP] = x;
			iSP ++;
			size ++;
		}
				
		state[symetricsColors.get(0)] = 0; //(ou décrémenter, à voir)
		
		countSP.add(size);
		clearLP();
		
		return 1;
	}
	
	public static void initialize() {
		LP = new Litteral[sat.getNbVariables() * 2];
		L1 = new Litteral[sat.getNbVariables() * 2];
		L2 = new Litteral[sat.getNbVariables() * 2];
		P = new Litteral[sat.getNbVariables() * 2];
		C = new Litteral[sat.getNbVariables()];
		affectations = new int [csp.getNbVariables()];
		explicitsPropagations = new Litteral [csp.getNbVariables()][sat.getNbVariables() * 2];
		iEP = new int [csp.getNbVariables()];
		result = new ResultPropagation(sat.getNbVariables() * 2);
		occ = new int [sat.getNbVariables() * 2][sat.getMaxOccurences() + 2];
		propagateds = new int [sat.getNbVariables() * 2];
	}
	
	public static ArrayList<Cause> getCauses(Litteral l, ArrayList<ArrayList<Cause>> graph, int [] occurences){
		
		ArrayList<Cause> causes = new ArrayList<Cause>();
		
		for (Cause cause : graph.get(l.getId())) {
			if (cause.getCouple().getV2() != null) {
				if (occurences[cause.getLevel()-1] == 0) {
					causes.add(cause);
					occurences[cause.getLevel()-1] = 1;
				}
			} else {
				causes.addAll(getCauses(cause.getCouple().getV1(), graph, occurences));
			}
			
		}
		
		return causes;
	}
	
	public static ArrayList<Cause> getCauses(Litteral l, ArrayList<ArrayList<Cause>> graph){
		
		ArrayList<Cause> causes = new ArrayList<Cause>();
		
		for (Cause cause : graph.get(l.getId())) {
			if (cause.getCouple().getV2() != null) {
				causes.add(cause);
			} else {
				causes.addAll(getCauses(cause.getCouple().getV1(), graph));
			}
			
		}
		
		return causes;
	}
	
	public static void initializeSymmetriesVariables() {
		V = csp.getNbVariables();
		state = new int[csp.getVariables().get(0).getDomain().size()];
		
		for (int j = 0 ; j < state.length ; j++) {
			state[j] = V;
		}
		
		SP = new Litteral[sat.getNbVariables()*2];
		countSP = new ArrayList<Integer>();
	}
	
	/**
	 * Solve the csp
	 * @param csp
	 */
	public static void solve() {
		
		if (flagSupport)
			sat = BinCSPConverter.supportEncoding(csp);
		else 
			sat = BinCSPConverter.directEncoding(csp);
			
		long begin = System.currentTimeMillis();
		
		initialize();
		initializeOcc();
		
		variablesStates = new int[csp.getNbVariables()];
		domainsSizes = new int [csp.getNbVariables()];
		
		int i = 1;
		
		int maxDomain = 0;
		for (Variable v : csp.getVariables()) {
			domainsSizes[v.getIndex()] = v.getDomain().size();
			int size = v.getDomain().size();
			if (size > maxDomain)
				maxDomain = size;
			i ++;
		}

		if (flagSymetries) 
			initializeSymmetriesVariables();
		
		int resultSymmetries = 0;
			
		while (true) {		
			switch (action) {
				case HEURISTIC : 
					idClause = domHeuristic();
					//idClause = degHeuristic(csp);
					
					if (flagSymetries && !flagNoMoreSymmetries) {
						resultSymmetries = breakSymmetries();
						if (resultSymmetries == -1) break;
					}
					
					couple = selectCouple(idClause);
					break;
					
				case SAME_VARIABLE :
					couple = selectCouple(idClause);
					break;
			}
		
			if (resultSymmetries == -1) {
				System.out.println("UNSATISFIABLE");
				System.out.println("nb_nodes : " + nbNodes);
				long end = System.currentTimeMillis();
				solveTime = end - begin;
				break;
			}
			
			x = couple.getV1();
			y = couple.getV2();
			
			if (x != null && y != null) {
				
				if (action != Action.SAME_VARIABLE) {
					affectations[iA] = idClause;
					iA ++;
				}
				
				nx = negation(x);
				ny = negation(y);
				
				C[iC] = x;
				iC ++;
				C[iC] = y;
				iC ++;
				CC.add(2);
				
				L1[0] = x;
				L1[1] = ny;
				L2[0] = nx;
				L2[1] = y;
				
				int r = testAndFilter();
				
				if (r == 1) {
					PA = L1;
				} else if (r == 2) {
					PA = L2;
				} else if (r == 3) {
					PA = null;
				} else {
					PA = LP;
				}				
				
				if (PA == null) {
					clearLP();
					Utils.clearArray(L1);
					Utils.clearArray(L2);
					CP.add(0);
					if (!backtrack()) {
						if (solutions.size() == 0)
							System.out.println("# UNSATISFIABLE");
						else {
							System.out.println("# SATISFIABLE");
							System.out.println("# " + solutions.size() + " solutions found");
							displayAllSolutions(sat, csp);
						}
						System.out.println("nb_nodes : " + nbNodes);
						long end = System.currentTimeMillis();
						solveTime = end - begin;
						break;
					}
				} else {
					action = Action.HEURISTIC;
					bcp(PA, false);
					
					int size = 0;
					for (int index = 0 ; i < PA.length ; index++) {
						if (PA[index] == null) break;
						P[iP] = PA[index];
						iP ++;
						size ++;
					}
					
					CP.add(size);
					
					variablesStates[idClause] = 1;
					nbVariablesSat ++; 
										
					for (Litteral l : PA) {
						if (l == null) break;
						if (l.getId() % 2 == 1)
							domainsSizes[l.getIdVariable()] --;
					}
								
					clearLP();
					Utils.clearArray(L1);
					Utils.clearArray(L2);
					
					if (modelExists()) {
						nbVariablesSat --;
						if (flagAllSolutions) {
							deductMultipleSolution();
							if (!backtrack()) {
								if (solutions.size() == 0)
									System.out.println("# UNSATISFIABLE");
								else {
									System.out.println("# SATISFIABLE");
									System.out.println("# " + solutions.size() + " solutions found");
									displayAllSolutions(sat, csp);
								}
								System.out.println("nb_nodes : " + nbNodes);
								long end = System.currentTimeMillis();
								solveTime = end - begin;
								break;
							}
						} else {
							deductMultipleSolution();
							displayAllSolutions(sat, csp);
							System.out.println("nb_nodes : " + nbNodes);
							long end = System.currentTimeMillis();
							solveTime = end - begin;
							break;
						}
					}
				}
					
			} else if (x != null && y == null) {
				
				CP.add(0);
				
				if (action != Action.SAME_VARIABLE) {
					affectations[iA] = idClause;
					iA ++;
				}
				
				nx = negation(x);
				
				C[iC] = x;
				iC ++;
				CC.add(1);
						
				action = Action.HEURISTIC;
								
				variablesStates[idClause] = 1;
				nbVariablesSat ++;
					
				clearLP();
					
				if (modelExists()) {
					nbVariablesSat --;
					if (flagAllSolutions) {
						deductMultipleSolution();
						if (!backtrack()) {
							if (solutions.size() == 0)
								System.out.println("# UNSATISFIABLE");
							else {
								System.out.println("# SATISFIABLE");
								System.out.println("# " + solutions.size() + " solutions found");
								displayAllSolutions(sat, csp);
							}
							System.out.println("nb_nodes : " + nbNodes);
							long end = System.currentTimeMillis();								
							solveTime = end - begin;
							break;
						}
					} else {
						deductMultipleSolution();
						displayAllSolutions(sat, csp);
						System.out.println("nb_nodes : " + nbNodes);
						long end = System.currentTimeMillis();
						solveTime = end - begin;
						break;
					}
				}
			} else if (x == null && y == null) {
				if (!backtrack()) {
					if (solutions.size() == 0)
						System.out.println("# UNSATISFIABLE");
					else {
						System.out.println("# " + solutions.size() + " solutions found");
						displayAllSolutions(sat, csp);
					}
					System.out.println("nb_nodes : " + nbNodes);
					long end = System.currentTimeMillis();
					solveTime = end - begin;
					break;
				}
			}
		}
	}
	
	/**
	 * Display time elapsed
	 */
	public static void displayTime() {
		System.out.println("propagation time : " + propagationTime + " ms");
		System.out.println("backtrack time : " + backtrackTime + " ms");
		System.out.println("restoreTime : " + restoreTime + " ms");
		System.out.println("selectCoupleTime : " + selectCoupleTime + " ms");
		System.out.println("findUnaffTime : " + findUnaffectedTime + " ms");
		System.out.println("TimeComputeSolution : " + timeComputeSolution);
		System.out.println("solveTime : " + solveTime + " ms");
		System.out.println("finalTime : " + finalTime + " ms");
	}
	
	public static void resetTimer() {
		propagationTime = 0;
		backtrackTime = 0;
		restoreTime = 0;
		selectCoupleTime = 0;
		findUnaffectedTime = 0;
		timeComputeSolution = 0;
		solveTime = 0;
		finalTime = 0;
	}

	public static void main(String [] args) {
		flagAllSolutions = true;
		flagDomHeuristic = false;
		flagDegHeuristic = true;
		flagSupport = false;
		flagDisplay = true;
		
		csp = Generator.generatePigeons(10, 9);
		//csp = Generator.generateRandomProblem(4, 5, 0.9, 15).getV1();
		solve();
		
		displayTime();
 
	}
}
