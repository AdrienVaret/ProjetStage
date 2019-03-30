package solver_sat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import bincsp.BinCSP;
import bincsp.Variable;
import conversion.BinCSPConverter;
import generator.Generator;
import generator.GeneratorBis;
import sat.Clause;
import sat.Litteral;
import sat.SAT;
import utils.Cause;
import utils.GenericCouple;
import utils.GenericCouple2;
import utils.Utils;

public class Solver {

	static BufferedWriter w;
	
	public enum Action {
		HEURISTIC, SAME_VARIABLE
	}
	
	/*
	 * Flags
	 */
	static boolean flagAllSolutions = false;
	static boolean flagDomHeuristic = true;
	static boolean flagDegHeuristic;
	static boolean flagWriteTree = false;
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
	static Litteral [] [] explicitsPropagations;
	static int [] iEP;
	static int [] affectations;
	static Action action = Action.HEURISTIC;
	static int [][] occ;
	static ResultPropagation result;
	
	
	/*
	 * X, Y sets used by the propagation
	 * 
	 */
	static Litteral [] X, Y, LP, PA, toPropage, P, C;
	static int iX = 0, iY = 0, iLP = 0, iPA = 0, iTP = 0, iA = 0, iP = 0, iC = 0;
	
	static ArrayList<Integer> CP = new ArrayList<Integer>();
	static ArrayList<Integer> CC = new ArrayList<Integer>();
	
	/*
	 * Variables used for breaking symmetries
	 */
	static int [] state;
	static int V;
	static Litteral [] SP; //Symetries propagations
	static ArrayList<Integer> countSP;
	static int iSP;
	
	/*
	 * Variables used for clause learning
	 */
	static ArrayList<ArrayList<Cause>> reason;
	static ArrayList<ArrayList<Cause>> G1, G2;
	static int decisionLevel;
	static int[] P1;
	static int[] P2;
	static Litteral conflict;
	
	
	/*
	 * Utilitaries
	 */
	
	public static void clearArray(int [] array) {
		for (int i = 0 ; i < array.length ; i++) {
			array[i] = 0;
		}
	}
	
	public static void clearGraph(ArrayList<ArrayList<Cause>> G) {
		for (ArrayList<Cause> causes : G) {
			causes.clear();
		}
	}
	
	public static void clearP1() {
		for (int i = 0 ; i < P1.length ; i++) {
			P1[i] = 0;
		}
	}
	
	public static void clearP2() {
		for (int i = 0 ; i < P2.length ; i++) {
			P2[i] = 0;
		}
	}
	
	public static void clearArray(Object [] array) {
		for (int index = 0 ; index < array.length ; index ++) array[index] = null;
	}
	
	public static void clearArray(Litteral [] array, int index) {
		for (int i = 0 ; i < index ; i++) 
			array[i] = null;
		index = 0;
	}
	
	// TODO : changer ce bordel c'est moche !
	public static void clearEP(int index) {
		for (int i = 0 ; i < iEP[index] ; i++)
			explicitsPropagations[index][i] = null;
		iEP[index] = 0;
	}
	
	public static void clearTP() {
		for (int i = 0 ; i < toPropage.length ; i++) toPropage[i] = null;
		iTP = 0;
	}
	
	public static void clearPA() {
		for (int i = 0 ; i < iPA ; i++) PA[i] = null;
		iPA = 0;
	}
	
	public static void clearX() {
		for (int i = 0 ; i < iX ; i++) X[i] = null;
		iX = 0;
	}
	
	public static void clearY() {
		for (int i = 0 ; i < iY ; i++) Y[i] = null;
		iY = 0;
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
	
	/**
	 * Find unaffected Litteral
	 * @param sat
	 * @return litteral's index
	 */
	public static int findUnafLitteral(SAT sat) {
		for (int i = 0 ; i < sat.getNbVariables() ; i++) {
			if (sat.getLitteralState(i) == 0) return i;
		}
		return -1;
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
	
	public static int backtrackMultipleSolution(SAT sat, BinCSP csp) {
	
		int n = cp.get(cp.size()-1);
		cp.remove(cp.size()-1);
		 
		for (int i = 0 ; i < n ; i++) {
			Litteral l = p[ip-1];
			restore(sat, l);
			p[ip-1] = null;
			ip --;
		}
		
		Litteral l = negation(sat, c[ic-1]);
		Litteral [] L = new Litteral [sat.getNbVariables() * 2]; 
		L[0] = l;
		
		boolean r = propagation(sat, L, 0, 1, false);
		
		c[ic - 1] = null;
		ic --;
		
		for (int i = 0 ; i < iX ; i++) {
			ep[idCC][iep[idCC]] = X[i];
			iep[idCC] ++;
		}
		clearX();

		
		while (!r) {
			if (idCC == 0) return -1;
			else {
				int max = iep[idCC];
				for (int i = 0 ; i < max ; i++) {
					Litteral toRestore = ep[idCC][i];
					restore (sat, toRestore);
				}
				
				//ep non mis à jour ? origine du pb ??
				
				iep[idCC] = 0;
				idCC --;
				int nb = CC.get(idCC);
				idC -= nb;
			}
			
			n = cp.get(cp.size()-1);
			cp.remove(cp.size()-1);
			
			for (int i = 0 ; i < n ; i++) {
				l = p[ip-1];
				restore(sat, l);
				p[ip-1] = null;
				ip --;
			}
			
			if (ic > 0) {
				l = negation(sat, c[ic-1]);
				L = new Litteral [sat.getNbVariables() * 2]; 
				L[0] = l;
			
				r = propagation(sat, L, 0, 1, false);
			
				c[ic - 1] = null;
				ic --;
			
				for (int i = 0 ; i < iX ; i++) {
					ep[idCC][iep[idCC]] = X[i];
					iep[idCC] ++;
				}
				clearX();
			} else {
				System.out.println("");
			}
		}
		
		return 1;
	}
	
	public static boolean allAffected(SAT sat) {
		for (int i = 0 ; i < sat.getNbVariables() ; i ++) {
			if (sat.getLitteralsStates()[i] == 0)
				return false;
		}
		return true;
	}
	
	public static void deductMultipleSolution(SAT sat, BinCSP csp, ArrayList<Integer> CC) {
		
		long begin = System.currentTimeMillis();
		
		//int [] initialStates = sat.getLitteralsStates().clone();
		
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
			
			/**/
			if (idCC >= CC.size()) break;
			/**/
			
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
				boolean r = propagation(sat, L, 0, 1, false);
				
				for (int i = 0 ; i < L.length ; i++)
					L[i] = null;
				
				for (int i = 0 ; i < iX ; i ++) {
					p[ip] = X[i];
					ip ++;
				}
				
				cp.add(iX);
				clearX();
				
				if (r == true && cp.size() == CC.size()) { //allAffected(sat) avant 
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
						if (backtrackMultipleSolution(sat, csp) == -1) {
							break;
						}
					} else {
						break;
					}
				} else if (r == false) {
					if (backtrackMultipleSolution(sat, csp) == -1) {
						break;
					}
				}
				else {
					idC += n;
					idCC ++;
				}
				
			} else {
				cp.add(0);
				if (backtrackMultipleSolution(sat, csp) == -1) {
					break;
				}
			}
		}
		
		//for (int i = 0 ; i < initialStates.length ; i++) {
		//	sat.getLitteralsStates()[i] = initialStates[i];
		//}
		
		int max = iep[0];
		for (int i = 0 ; i < max ; i++) {
			Litteral toRestore = ep[0][i];
			restore(sat, toRestore);
		}
		
		long end = System.currentTimeMillis();
		timeComputeSolution += (end - begin);
	}
	
	/**
	 * Deduct one model from affectation
	 * @param sat
	 */
	public static void deductModel(SAT sat) {
		int i = findUnafLitteral(sat);
		while (i != -1) {
			i = i * 2;
			Litteral l = sat.getLitteral(i);
			Litteral [] L = new Litteral[sat.getNbVariables() * 2];
			L[0] = l;
			
			boolean r = propagation(sat, L, 0, 1, false);
			clearArray(L);
			clearX();
			
			if (!r) {
				for (int index = 0 ; index < L.length ; index ++)L[index] = null;
				i ++;
				l = sat.getLitteral(i);
				L[0] = l;
				propagation(sat, L, 0, 1, false);
				clearArray(L);
			}
			i = findUnafLitteral(sat);
		}
	}
	
	/**
	 * Display one solution
	 * @param csp
	 * @param sat
	 */
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
	
	/**
	 * Return litteral's negation
	 * @param sat
	 * @param litteral
	 * @return litteral
	 */
	public static Litteral negation(SAT sat, Litteral litteral) {
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
	public static boolean isAffected(SAT sat, Litteral l) {
		return sat.getLitteralState(getIndex(l.getId())) != 0;
	}
	
	/**
	 * Select one or two values from the variable at index position
	 * @param sat
	 * @param index
	 * @return
	 */
	public static GenericCouple<Litteral> selectCouple(SAT sat, int index){
		
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
	
	public static int degHeuristic(BinCSP csp) {
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
	public static boolean isSat(SAT sat, Litteral l) {	
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
	public static void affect(SAT sat, Litteral l) {
		if (l.getId() % 2 == 0)
			sat.getLitteralsStates()[getIndex(l.getId())] = 1;
		else
			sat.getLitteralsStates()[getIndex(l.getId())] = -1;
	}
	
	/**
	 * Returns an unafected litteral and his index, or null
	 * @param sat
	 * @param clause
	 * @return GenericCouple<Integer, Litteral>
	 */
	public static GenericCouple2<Integer, Litteral> findUnafectedOrSatLitteral(SAT sat, Clause clause) {
		long begin = System.currentTimeMillis();
		
		for (int i = 2 ; i < clause.getLitterals().size() ; i++) {
			Litteral l = clause.get(i);
			int id = getIndex(l.getId());
			
			if (((sat.getLitteralState(id) == 0) || (isSat(sat, l)))){
				return new GenericCouple2<Integer, Litteral>(i,l);
			}
		}
		
		long end = System.currentTimeMillis();
		long time = end - begin;
		findUnaffectedTime += time;
		return new GenericCouple2<Integer, Litteral> (null, null);
	}
	
	/**
	 * Made unitary propagation of a list of litteral
	 * @param sat
	 * @param L
	 * @param action (1 = intersection), set (1 = X, 2 = Y)
	 * @return
	 */
	public static boolean propagation(SAT sat, Litteral [] L, int action, int set, boolean updateGraph) {
		
		/**/
		updateGraph = false;
		/**/
		
		long begin = System.currentTimeMillis();
		int [] propagateds = new int [sat.getNbVariables() * 2];
		int [] statesClauses = new int [sat.getNbClauses()];
		int idLitteral = 0;
		
		int [] occurences = new int [decisionLevel];
		
		ArrayList<ArrayList<Cause>> G;
		if (set == 1) G = G1;
		else G = G2;
		
		for (Litteral l : L) {
			if (l == null) break;
			idLitteral ++;
			propagateds[l.getId()] = 1;
			
			if (set == 1) {
				P1[l.getId()] = 1;
				/**/
				if (sat.getLitteralsStates()[getIndex(l.getId())] == 0) {
					X[iX] = l;
					iX ++;
				}
				if (updateGraph)
					G1.get(l.getId()).add(new Cause(couple, decisionLevel));
			} else if (set == 2) {
				P2[l.getId()] = 1;
				
				if (sat.getLitteralsStates()[getIndex(l.getId())] == 0) {
					Y[iY] = l;
					iY ++;
				}
				if (updateGraph)
					G2.get(l.getId()).add(new Cause(couple, decisionLevel));
			}
		}
		
		int indexLitteral = 0;
		Litteral l = L[indexLitteral], nl;
		
		while (l != null) {
			nl = negation(sat, l);
			//ArrayList<Integer> toShift = new ArrayList<Integer>();
			int toShift = 0;
			for (int i = 1 ; i < occ[nl.getId()][0] + 1 ; i++) {
				
				//if (occ[nl.getId()][i] == -1) {
				//	Utils.shiftToEnd(occ[nl.getId()], i);
				//	occ[nl.getId()][0] --;
				//}
				
				if (occ[nl.getId()][i] == -1 ) {
					System.out.println("");
				}
				
				Clause c = sat.getClauses().get(occ[nl.getId()][i]);
				Litteral x = sat.getCouplePtr(c.getId()).getV1();
				Litteral y = sat.getCouplePtr(c.getId()).getV2();
				
				GenericCouple2<Integer, Litteral> coupleAff = findUnafectedOrSatLitteral(sat, c);
				Litteral affectable = coupleAff.getValue2();
				
				if (affectable == null) {
					if (x != null && x.equals(nl)) { //ajout de x != null
						
						if (y == null) {
							result.setState(false);
							//ICI : ajouter le littéral conflit
							
							Utils.shiftAll(occ[nl.getId()], toShift);
							toShift = 0;
							
							return false;
						}
						
						if (isAffected(sat, y)) {
							if (isSat(sat, y))
								statesClauses[c.getId()] = 1;
							else {
								result.setState(false);
								
								Utils.shiftAll(occ[nl.getId()], toShift);
								toShift = 0;
								
								indexLitteral ++;
								
								if (updateGraph) {
									
									@SuppressWarnings("unused")
									Litteral l1 = L[indexLitteral];
									@SuppressWarnings("unused")
									Litteral l2 = L[indexLitteral-1];
								
									ArrayList<Cause> causes = new ArrayList<Cause>();
									clearArray(occurences);
									//causes.addAll(getCauses(l1, G, occurences));
									//causes.addAll(getCauses(l2, G, occurences));
								
									G.get(conflict.getId()).addAll(causes);
								}
								
								return false;
							}
						} else {
							if (propagateds[y.getId()] == 0) {
								propagateds[y.getId()] = 1;

								L[idLitteral] = y;
								idLitteral ++;	
								
								result.incr(y.getId());
								
								if (set == 1) {
									X[iX] = y;
									if (y != null) P1[y.getId()] = 1;
									iX ++;
								} else if (set == 2) {
									if (y != null) P2[y.getId()] = 1;
									Y[iY] = y;
									iY ++;
								}
								
								if (updateGraph) {
									if (y.getId() % 2 == 0) {
										//G.get(x.getId()).add(new Cause(couple, decisionLevel));
										clearArray(occurences);
										for (Litteral litteral : sat.getClauses().get(y.getIdVariable()).getLitterals()) {
											if (!litteral.equals(y)) {
												Litteral neg = negation(sat, litteral);
												ArrayList<Cause> causes = new ArrayList<Cause>();
												if (propagateds[neg.getId()] == 0) {
													causes.addAll(getCauses(neg, reason, occurences));
												} else {
													causes.addAll(getCauses(neg, G, occurences));
												}
												G.get(y.getId()).addAll(causes);
											}
										}
									} else {
										//rappel : y est propagé par l
										clearArray(occurences);
										ArrayList<Cause> causes = getCauses(l, G, occurences);
										G.get(y.getId()).addAll(causes);
									}
								}
								
								if (action == 2 && result.get(y.getId()) == 2) {
									LP[iLP] = y;
									iLP ++;
								}						
								
							}
						}
					} else if (/**/y != null && /**/ y.equals(nl)) {
						if (isAffected(sat, x)) {
							if (isSat(sat, x))
								statesClauses[c.getId()] = 1;
							else {
								result.setState(false);
								
								Utils.shiftAll(occ[nl.getId()], toShift);
								toShift = 0;
								
								if (updateGraph) {
									indexLitteral ++;
									Litteral l1 = L[indexLitteral];
									Litteral l2 = L[indexLitteral-1];
									
									clearArray(occurences);
									ArrayList<Cause> causes = new ArrayList<Cause>();
									causes.addAll(getCauses(l1, G, occurences));
									causes.addAll(getCauses(l2, G, occurences));
								
									G.get(conflict.getId()).addAll(causes);
								}
								
								return false;
							}
						} else { 
							if (propagateds[x.getId()] == 0) {
								propagateds[x.getId()] = 1;
								
								L[idLitteral] = x;
								idLitteral ++;
								
								result.incr(x.getId());
								
								if (set == 1) {
									if (x != null) P1[x.getId()] = 1;
									X[iX] = x;
									iX ++;
								} else if (set == 2) {
									if (x != null) P2[x.getId()] = 1;
									Y[iY] = x;
									iY ++;
								}
								
								if (updateGraph) {
									if (x.getId() % 2 == 0) {
										//G.get(x.getId()).add(new Cause(couple, decisionLevel));
										clearArray(occurences);
										for (Litteral litteral : sat.getClauses().get(x.getIdVariable()).getLitterals()) {
											if (!litteral.equals(x)) {
												Litteral neg = negation(sat, litteral);
												ArrayList<Cause> causes = new ArrayList<Cause>();
												if (propagateds[neg.getId()] == 0)
													causes.addAll(getCauses(neg, reason, occurences));
												else
													causes.addAll(getCauses(neg, G, occurences));
												G.get(x.getId()).addAll(causes);
											}
										}
									} else {
										//rappel : y est propagé par l
										clearArray(occurences);
										ArrayList<Cause> causes = getCauses(l, G, occurences);
										G.get(x.getId()).addAll(causes);
									}
								}
								
								if (action == 2 && result.get(x.getId()) == 2) {
									LP[iLP] = x;
									iLP ++;
								}		
							}
						}
					}
				} else { //if affectable != null, bouger les pointeurs
					if (x.equals(nl)) {
						int xid = x.getId();
						int index = 1;
						while (occ[xid][index] != c.getId()) {
							index ++;
						}
						
						occ[xid][index] = -1;
						//toShift.add(index);
						toShift ++;
						
						int lid = coupleAff.getValue2().getId();
						
						int size = occ[lid][0];
						occ[lid][size+1] = c.getId();
						occ[lid][size+2] = -1;
						occ[lid][0] ++;
						
						Collections.swap(c.getLitterals(), 0, coupleAff.getValue1());
						sat.setCouplePtr(c.getId(), c.get(0), c.get(1));
						
						//Utils.shiftAll(occ[nl.getId()], toShift);
						//toShift = 0;
						
					} else if (y.equals(nl)){
						int yid = y.getId();
						int index = 1;
						while (occ[yid][index] != c.getId()) {
							index ++;
						}

						occ[yid][index] = -1;
						//toShift.add(index);
						toShift ++;
						
						int lid = coupleAff.getValue2().getId();
						
						int size = occ[lid][0];
						occ[lid][size+1] = c.getId();
						occ[lid][size+2] = -1;
						occ[lid][0] ++;
						
						Collections.swap(c.getLitterals(), 1, coupleAff.getValue1());
						sat.setCouplePtr(c.getId(), c.get(0), c.get(1));
						
						//Utils.shiftAll(occ[nl.getId()], toShift);
						//toShift = 0;
					}
				//shift ici
					
				}
				//Utils.shiftAll(occ[nl.getId()], toShift);
				//toShift = 0;
			}
			Utils.shiftAll(occ[nl.getId()], toShift);
			toShift = 0;
			//occ[nl.getId()][0] -= toShift;
			affect(sat, l);
			indexLitteral ++;
			l = L[indexLitteral];
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
	public static void restore(SAT sat, Litteral l) {
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
	 * @param shift
	 */
	public static void restoreAll(SAT sat, ArrayList<Litteral> L, int [] shift) {
		long begin = System.currentTimeMillis();
		
		for (Litteral l : L) {
			restore(sat, l);
		}
		long end = System.currentTimeMillis();
		long time = end - begin;
		restoreTime += time;
	}
	
	public static void restoreAll(SAT sat, Litteral [] L, int [] shift) {
		long begin = System.currentTimeMillis();
		
		for (Litteral l : L) {
			if (l == null) break;
			restore(sat, l);
		}
		
		long end = System.currentTimeMillis();
		long time = end - begin;
		restoreTime += time;
	}
	
	
	public static void propagationAll(SAT sat, Litteral [] L1, Litteral [] L2, int [] shift) {
		boolean result1 = propagation(sat, L1, 2, 1, true);
		restoreAll(sat, X, shift);
		boolean result2 = propagation(sat, L2, 2, 2, true);
		
		int [] occurences = new int [decisionLevel];
		
		restoreAll(sat, Y, shift);
		
		result.clear();
		
		if (result1 && !result2) {
			PA = X.clone();
			iPA = X.length;
			
			ArrayList<Cause> causesConflict = getCauses(conflict, G2);
			
			int i = 0;
			while(PA[i] != null) {
				Litteral l = PA[i];
				clearArray(occurences);
				ArrayList<Cause> causes = getCauses(l, G1);
				
				for (Cause cause : causesConflict) {
					if (occurences[cause.getLevel()-1] == 0) {
						reason.get(l.getId()).add(cause);
						occurences[cause.getLevel()-1] = 1;
					}
				}
				
				for (Cause cause : causes) {
					if (occurences[cause.getLevel()-1] == 0) {
						reason.get(l.getId()).add(cause);
						occurences[cause.getLevel()-1] = 1;
					}
				}
				
				//reason.get(l.getId()).addAll(causesConflict);
				//reason.get(l.getId()).addAll(causes);
				i++;
			}
			
			clearArray(L1);
			clearArray(L2);
			clearGraph(G1);
			clearGraph(G2);
		}
		else if (!result1 && result2) {
			PA = Y.clone();
			iPA = Y.length;
			
			ArrayList<Cause> causesConflict = getCauses(conflict, G1);
			
			int i = 0;
			while(PA[i] != null) {
				Litteral l = PA[i];
				clearArray(occurences);
				ArrayList<Cause> causes = getCauses(l, G2);
				
				for (Cause cause : causesConflict) {
					if (occurences[cause.getLevel()-1] == 0) {
						reason.get(l.getId()).add(cause);
						occurences[cause.getLevel()-1] = 1;
					}
				}
				
				for (Cause cause : causes) {
					if (occurences[cause.getLevel()-1] == 0) {
						reason.get(l.getId()).add(cause);
						occurences[cause.getLevel()-1] = 1;
					}
				}
				
				//reason.get(l.getId()).addAll(causesConflict);
				//reason.get(l.getId()).addAll(causes);
				i++;
			}
			
			clearArray(L1);
			clearArray(L2);
			clearGraph(G1);
			clearGraph(G2);
		}
		else if (!result1 && !result2) {
			PA = null;
			iPA = 0;
			
			ArrayList<Cause> causesConflict = new ArrayList<Cause>();
			clearArray(occurences);
			causesConflict.addAll(getCauses(conflict, G1, occurences));
			causesConflict.addAll(getCauses(conflict, G1, occurences));
			
			reason.get(conflict.getId()).addAll(causesConflict);
			
			clearArray(L1);
			clearArray(L2);
			clearGraph(G1);
			clearGraph(G2);
		}
		else {
			PA = LP.clone();
			iPA = LP.length;
			
			int i = 0;
			while(PA[i] != null) {
				Litteral l = PA[i];
				
				ArrayList<Cause> causes = new ArrayList<Cause>();
				clearArray(occurences);
				causes.addAll(getCauses(l, G1, occurences));
				causes.addAll(getCauses(l, G2, occurences));
				
				reason.get(l.getId()).addAll(causes);
				
				i++;
			}
			
			clearArray(L1);
			clearArray(L2);
			clearGraph(G1);
			clearGraph(G2);
		}
		
	}

	public static int findIndex(int [] shift, int id) {
		
		int idLitteral;
		idLitteral = id >> 1;
		
		for (int i = 0 ; i < shift.length ; i++) {
			if (i < shift.length - 1) {
				if (shift[i] <= idLitteral && idLitteral < shift[i+1]) return i;
			} else {
				return i;
			}
		}
		return -1;
	}
	
	public static boolean backtrack(SAT sat, ArrayList<Integer> CP, ArrayList<Integer> CC, int [] shift) {

		long begin = System.currentTimeMillis();
		
		nodeState = false;
		
		if (flagWriteTree)
			displayPC(CC);
		
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
			restore(sat, l);
			P[iP-1] = null;
			iP --;
			int index = findIndex(shift, l.getId());
			if (l.getId() % 2 == 1)
				domainsSizes[index] ++;
		}
		
		for (int i = 0 ; i < nbChoices ; i++) {
			Litteral l = C[iC-1];
			int lid = getIndex(l.getId());
			//sat.getChoises()[lid] = 0;
			toPropage[iTP] = negation(sat, l);
			iTP ++;
			C[iC-1] = null;
			iC --;
		}
		
		boolean r = propagation(sat, toPropage, 0, 1, false);
		clearTP();
		result.clear();
		
		for (int i = 0 ; i < iX ; i++) {
			explicitsPropagations[idClause][iEP[idClause]] = X[i];
			iEP[idClause] ++;
		}
		
		clearX();
	
		if (!r) {
			while (!r) {
				restoreAll(sat, explicitsPropagations[idClause], shift);
				
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
					restore(sat, l);
					P[iP-1] = null;
					iP --;
					int index = findIndex(shift, l.getId());
					
					if (l.getId() % 2 == 1)
						domainsSizes[index] ++;
				}
				
				for (int i = 0 ; i < nbChoices ; i++) {
					Litteral l = C[iC-1];
					//int lid = getIndex(l.getId());
					//sat.getChoises()[lid] = 0;
					toPropage[iTP] = negation(sat, l);
					iTP ++;
					C[iC-1] = null;
					iC --;
				}
				
				r = propagation(sat, toPropage, 0, 1, false);
				clearTP();
				result.clear();
				
				for (int i = 0 ; i < iX ; i++) {
					explicitsPropagations[idClause][iEP[idClause]] = X[i];
					iEP[idClause] ++;
				}
				
				clearX();
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
	
	public static boolean modelExists(BinCSP csp, SAT sat) {
		//ATTENTION MODIFICATION
		/*for (int i = 0 ; i < sat.getNbVariables() ; i++) {
			if (sat.getLitteralState(i) == 0 && sat.getChoice(i) == 0)
				return false;
		}
		return true;*/
		for (int i = 0 ; i < variablesStates.length ; i++) {
			if (variablesStates[i] == 0) return false;
		}
		return true;
	}
	
	public static void displayPC(ArrayList<Integer> CC) {
		int index = 0;
		for (Integer i : CC) {
			if (i == 2) {
				try {
					w.write(C[index] + " != " + C[index+1] + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				index += 2;
			} 
			if (i == 1) {
				try {
					w.write(C[index] + " = 1" + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				index += 1;
			}
		}
	}
	
	/**
	 * Initialize occurences matrix
	 * @param sat
	 */
	public static void initializeOcc(SAT sat) {
		
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
	
	public static void displayAllSolutions(SAT sat, BinCSP csp, int [] shift) {
		int nbSolutions = 1;
		for (Litteral [] L : solutions) {
			if (flagDisplay) 
				System.out.println("# Solution " + nbSolutions + " : ");
			for (Litteral l : L) {
				int id = 0, begin = 0, indexVariable = 0;
				for (int i = 0 ; i < shift.length ; i++) {
					id = l.getId() / 2;
					if (i < shift.length - 1) {
						if (shift[i] <= id && shift[i+1] > id) {
							begin = shift[i];
							indexVariable = i;
							break;
						}
					} else {
						begin = shift[i];
					}
				}
				int indexValue = id - begin;
				String name = csp.getVariables().get(indexVariable).getName();
				String value = csp.getVariables().get(indexVariable).getDomain().get(indexValue);
				if (flagDisplay) 
					System.out.println("# " + name + " = " + value);
			}
			if (flagDisplay)
				System.out.println("##");
			nbSolutions ++;
		}
	}
	
	public static int findColor(int id, BinCSP csp) {
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
	
	
	
	public static int breakSymmetries(BinCSP csp, SAT sat, int [] shift) {
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
			Litteral x = negation(sat, sat.getLitterals().get(indexX));
			L1[iL1] = x;
			iL1 ++;
		}
		
		boolean r = propagation(sat, L1, 0, 1, false);
		clearArray(L1);
		result.clear();
		V--;
		
		if (!r) {
			System.out.println("UNSAT");
			return -1;
		}
		
		for (int i = 0 ; i < iX ; i++) {
			Litteral x = X[i];
			int indexX = x.getId();
			if (indexX % 2 != 0) { //if a negation is propagated
				int color = findColor(indexX - 1, csp);
				state[color] --;
			}
			SP[iSP] = x;
			iSP ++;
		}
				
		state[symetricsColors.get(0)] = 0; //(ou décrémenter, à voir)
		
		countSP.add(iX);
		
		clearX();
		clearY();
		clearLP();
		
		return 1;
	}
	
	public static void initialize(SAT sat, BinCSP csp) {
		X  = new Litteral[sat.getNbVariables() * 2];
		Y  = new Litteral[sat.getNbVariables() * 2];
		LP = new Litteral[sat.getNbVariables() * 2];
		P = new Litteral[sat.getNbVariables() * 2];
		C = new Litteral[sat.getNbVariables()];
		
		toPropage = new Litteral [sat.getNbVariables() * 2];
		affectations = new int [csp.getNbVariables()];
		
		explicitsPropagations = new Litteral [csp.getNbVariables()][sat.getNbVariables() * 2];
		
		iEP = new int [csp.getNbVariables()];
		
		result = new ResultPropagation(sat.getNbVariables() * 2);
		
		occ = new int [sat.getNbVariables() * 2][sat.getMaxOccurences() + 2];
		
		conflict  = new Litteral(sat.getNbVariables() * 2, -1);
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
	
	public static void initializeSymmetriesVariables(BinCSP csp, SAT sat) {
		V = csp.getNbVariables();
		state = new int[csp.getVariables().get(0).getDomain().size()];
		
		for (int j = 0 ; j < state.length ; j++) {
			state[j] = V;
		}
		
		SP = new Litteral[sat.getNbVariables()*2];
		countSP = new ArrayList<Integer>();
	}
	
	public static void initializeLearningVariables(SAT sat) {
		reason = new ArrayList<ArrayList<Cause>>();
		G1 = new ArrayList<ArrayList<Cause>>();
		G2 = new ArrayList<ArrayList<Cause>>();
		
		for (int j = 0 ; j <= sat.getNbVariables() * 2 ; j++) {
			reason.add(new ArrayList<Cause>());
			G1.add(new ArrayList<Cause>());
			G2.add(new ArrayList<Cause>());
		}
		
		decisionLevel = 1;
		P1 = new int[sat.getNbVariables() * 2];
		P2 = new int[sat.getNbVariables() * 2];	
	}
	
	/**
	 * Solve the csp
	 * @param csp
	 */
	public static void solve(BinCSP csp) {

		long begin = System.currentTimeMillis();
		
		SAT sat;
		
		if (flagSupport)
			sat = BinCSPConverter.supportEncoding(csp);
		else 
			sat = BinCSPConverter.directEncoding(csp);
			
		initialize(sat, csp);
		initializeOcc(sat);
		
		variablesStates = new int[csp.getNbVariables()];
		domainsSizes = new int [csp.getNbVariables()];
		int [] shift = new int[csp.getNbVariables() + 1];
		
		shift [0] = 0;
		int i = 1;
		int sum = 0;
		
		int maxDomain = 0;
		for (Variable v : csp.getVariables()) {
			domainsSizes[v.getIndex()] = v.getDomain().size();
			int size = v.getDomain().size();
			sum += size;
			shift[i] = sum;
			if (size > maxDomain)
				maxDomain = size;
			i ++;
		}

		initializeSymmetriesVariables(csp, sat);
		int resultSymmetries = 0;
		initializeLearningVariables(sat);
		
		
		while (true) {		
			switch (action) {
				case HEURISTIC : 
					idClause = domHeuristic();
					//idClause = degHeuristic(csp);
					/**/
					if (flagSymetries && !flagNoMoreSymmetries) {
						resultSymmetries = breakSymmetries(csp, sat, shift);
						if (resultSymmetries == -1) break;
					}
					/**/
					couple = selectCouple(sat, idClause);
					break;
					
				case SAME_VARIABLE :
					couple = selectCouple(sat, idClause);
					break;
			}
		
			if (resultSymmetries == -1) {
				System.out.println("UNSATISFIABLE");
				System.out.println("nb_nodes : " + nbNodes);
				long end = System.currentTimeMillis();
				solveTime = end - begin;
				break;
			}
			
			//nbNodes ++;	
			
			Litteral x = couple.getV1();
			Litteral y = couple.getV2();
			Litteral nx, ny;
			
			if (x != null && y != null) {
				
				if (action != Action.SAME_VARIABLE) {
					affectations[iA] = idClause;
					iA ++;
				}
				
				nx = negation(sat, x);
				ny = negation(sat, y);
				
				C[iC] = x;
				iC ++;
				C[iC] = y;
				iC ++;
				CC.add(2);
				
				int xid = getIndex(x.getId());
				int yid = getIndex(y.getId());
				
				//sat.getChoises()[xid] = 1;
				//sat.getChoises()[yid] = 1;
				
				Litteral [] L1 = new Litteral [sat.getNbVariables() * 2];
				L1[0] = x;
				L1[1] = ny;
				Litteral [] L2 = new Litteral [sat.getNbVariables() * 2];
				L2[0] = nx;
				L2[1] = y;
				
				propagationAll(sat, L1, L2, shift);
				
				decisionLevel++;
				
				clearX(); 
				clearY(); 
				clearLP();
				
				if (PA == null) {
					CP.add(0);
					if (!backtrack(sat, CP, CC, shift)) {
						if (solutions.size() == 0)
							System.out.println("# UNSATISFIABLE");
						else {
							System.out.println("# SATISFIABLE");
							System.out.println("# " + solutions.size() + " solutions found");
							displayAllSolutions(sat, csp, shift);
						}
						System.out.println("nb_nodes : " + nbNodes);
						long end = System.currentTimeMillis();
						solveTime = end - begin;
						break;
					}
				} else {
					action = Action.HEURISTIC;
					propagation(sat, PA, 0, 1, false);
					clearPA();
					
					for (int index = 0 ; index < iX ; index++) {
						P[iP] = X[index];
						iP ++;
					}
					
					CP.add(iX);
					
					variablesStates[idClause] = 1;
					nbVariablesSat ++; 
						
					for (Litteral l : X) {
						if (l == null) break;
						int index = findIndex(shift, l.getId());
						if (l.getId() % 2 == 1)
							domainsSizes[index] --;
					}
					
					sat.setNbLitteralsSat(sat.getNbLitteralsSat() + iX);
					
					clearX();
					
					if (modelExists(csp,sat)) {
						//displayPC(CC);
						if (flagAllSolutions) {
							deductMultipleSolution(sat, csp, CC);
							if (!backtrack(sat, CP, CC, shift)) {
								if (solutions.size() == 0)
									System.out.println("# UNSATISFIABLE");
								else {
									System.out.println("# SATISFIABLE");
									System.out.println("# " + solutions.size() + " solutions found");
									displayAllSolutions(sat, csp, shift);
								}
								System.out.println("nb_nodes : " + nbNodes);
								long end = System.currentTimeMillis();
								solveTime = end - begin;
								break;
							}
						} else {
							//deductModel(sat);
							//TODO : changer
							deductMultipleSolution(sat, csp, CC);
							//displaySolution(csp, sat);
							displayAllSolutions(sat, csp, shift);
							System.out.println("nb_nodes : " + nbNodes);
							long end = System.currentTimeMillis();
							solveTime = end - begin;
							break;
						}
					}
				}
					
			} else if (x != null && y == null) {
				
				if (action != Action.SAME_VARIABLE) {
					affectations[iA] = idClause;
					iA ++;
				}
				
				//nbNodes ++;
				
				nx = negation(sat, x);
				
				C[iC] = x;
				iC ++;
				CC.add(1);
				
				int xid = getIndex(x.getId());
				//sat.getChoises()[xid] = 1;
			
				Litteral [] L1 = new Litteral[sat.getNbVariables() * 2];
				L1[0] = x;
				
				boolean r = propagation(sat, L1, 0, 1, true); 
				
				//TODO: mettre à jour le graphe d'implication (ici un seul sous graphe donc simple ...)
				decisionLevel ++;
				
				clearArray(L1);
				
				result.clear();
				
				if (!r) {
					CP.add(0);
					restoreAll(sat, X, shift);
					
					clearX();
					clearY();
					clearLP();
					
					if (!backtrack(sat, CP, CC, shift)) {
						if (solutions.size() == 0)
							System.out.println("# UNSATISFIABLE");
						else {
							System.out.println("# " + solutions.size() + " solutions found");
							displayAllSolutions(sat, csp, shift);
						}
						System.out.println("nb_nodes : " + nbNodes);
						long end = System.currentTimeMillis();
						solveTime = end - begin;
						break;
					}
				} else {
					action = Action.HEURISTIC;
					
					for (int index = 0 ; index < iX ; index ++) {
						P[iP] = X[index];
						iP ++;
					}
					CP.add(iX);
					
					variablesStates[idClause] = 1;
					nbVariablesSat ++;
					
					for (Litteral l : X) {
						if (l == null) break;
						int index = findIndex(shift, l.getId());
						if (l.getId() % 2 == 1) 
							domainsSizes[index] --;
					}
					
					sat.setNbLitteralsSat(sat.getNbLitteralsSat() + iX);
					
					clearX();
					clearY();
					clearLP();
					
					if (modelExists(csp,sat)) {
						if (flagAllSolutions) {
							deductMultipleSolution(sat, csp, CC);
							if (!backtrack(sat, CP, CC, shift)) {
								if (solutions.size() == 0)
									System.out.println("# UNSATISFIABLE");
								else {
									System.out.println("# SATISFIABLE");
									System.out.println("# " + solutions.size() + " solutions found");
									displayAllSolutions(sat, csp, shift);
								}
								System.out.println("nb_nodes : " + nbNodes);
								long end = System.currentTimeMillis();
								solveTime = end - begin;
								break;
							}
						} else {
							//deductModel(sat);
							//TODO MODIF
							deductMultipleSolution(sat, csp, CC);
							//displaySolution(csp, sat);
							displayAllSolutions(sat, csp, shift);
							System.out.println("nb_nodes : " + nbNodes);
							long end = System.currentTimeMillis();
							solveTime = end - begin;
							break;
						}
					}
				}
			} else if (x == null && y == null) {
				
				if (!backtrack(sat, CP, CC, shift)) {
					if (solutions.size() == 0)
						System.out.println("# UNSATISFIABLE");
					else {
						System.out.println("# " + solutions.size() + " solutions found");
						displayAllSolutions(sat, csp, shift);
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
	
	public static void clearVariables() {
		
		//displayTime();
		
		System.out.println("#########");
		
		//flagSupport = true;
		ic = 0;
		iA = 0;
		nbNodes = 0;
		clearX();
		clearY();
		
		if (c != null) {
			for (int i = 0 ; i < iC ; i++) {
				c[iC] = null;
			}
		}
		
		solutions.clear();
		
		iC = 0;
		CC.clear();
		CP.clear();
		
		for (int i = 0 ; i < iP ; i++) {
			P[i] = null;
		}
		iP = 0;
		
		resetTimer();
	}
	
	public static void main(String [] args) {
		flagAllSolutions = true;
		flagDomHeuristic = false;
		flagDegHeuristic = true;
		flagSupport = false;
		flagDisplay = true;
		
		BinCSP csp = Generator.generatePigeons(9,8);
		solve(csp);
		
		displayTime();
	
	}
}
