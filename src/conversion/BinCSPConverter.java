package conversion;

import java.util.ArrayList;
import bincsp.BinCSP;
import bincsp.Constraint;
import bincsp.Couple;
import bincsp.Domain;
import bincsp.Relation;
import bincsp.Relation.TypeRelation;
import sat.Clause;
import sat.Litteral;
import sat.SAT;
import bincsp.Variable;
import generator.Generator;

public class BinCSPConverter {

	public static BinCSP convertToConflicts(BinCSP csp) {
		ArrayList<Relation> newRelations = new ArrayList<Relation>();
		ArrayList<Constraint> newConstraints = new ArrayList<Constraint>();
		
		for (Constraint oldConstraint : csp.getConstraints()) {
			if (oldConstraint.getRelation().getTypeRelation() == TypeRelation.R_CONFLICTS) {
				newConstraints.add(oldConstraint);
			} else {
				ArrayList<Couple> newCouples = new ArrayList<Couple>();
				for (String value1 : oldConstraint.getVariable1().getDomain().getValues()) {
					for (String value2 : oldConstraint.getVariable2().getDomain().getValues()) {
						newCouples.add(new Couple(value1, value2));
					}
				}
				
				ArrayList<Couple> toRemove = new ArrayList<Couple>();
				
				for (Couple oldCouple : oldConstraint.getRelation().getCouples()) {
					for (Couple newCouple : newCouples) {
						if (oldCouple.getValue1().equals(newCouple.getValue1()) &&
							oldCouple.getValue2().equals(newCouple.getValue2())) {
							toRemove.add(newCouple);
						}
					}
				}
				
				newCouples.removeAll(toRemove);
				
				Relation newRelation = new Relation(TypeRelation.R_CONFLICTS, newCouples);
				
				newRelations.add(newRelation);
				
				newConstraints.add(new Constraint(oldConstraint.getVariable1(), 
						                          oldConstraint.getVariable2(), 
						                          newRelation));
			}
		}
		
		return new BinCSP(csp.getNbVariables(), csp.getNbDomains(), newConstraints.size(), newRelations.size(),
                csp.getVariables(), csp.getDomains(), newConstraints, newRelations);
	}
	
	public static BinCSP shiftDomains(BinCSP csp) {
		
		//init shift array and shift variables
		int n = 0;
		ArrayList<Variable> variables = new ArrayList<Variable>();
		ArrayList<Domain> domains = new ArrayList<Domain>();
		int value = 0;
		for (Variable variable : csp.getVariables()) {
			ArrayList<String> values = new ArrayList<String>();
			int [] shift = new int [variable.getDomain().size()];
			for (int i = 0 ; i < shift.length ; i++) {
				shift[i] = value - Integer.parseInt(variable.getDomain().get(i));
				values.add(Integer.toString(value));
				value ++;
			}
			variable.getDomain().setShift(shift);
			Domain domain = new Domain(variable.getDomain().getName(), values);
			domains.add(domain);
			variables.add(new Variable(variable.getName(), domain, n));
			n++;
		}
		
		//shift relations
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
		ArrayList<Relation> relations = new ArrayList<Relation>();
		for (Constraint constraint : csp.getConstraints()) {
			ArrayList<Couple> couples = new ArrayList<Couple>();
			for (Couple couple : constraint.getRelation().getCouples()) {
				int indexV1 = 0, indexV2 = 0;
				for (String v : constraint.getVariable1().getDomain().getValues()) {
					if (couple.getValue1().equals(v)) break;
					indexV1 ++;
				}
				for (String v : constraint.getVariable2().getDomain().getValues()) {
					if (couple.getValue2().equals(v)) break;
					indexV2 ++;
				}
				int shiftV1 = constraint.getVariable1().getDomain().getShift()[indexV1];
				int shiftV2 = constraint.getVariable2().getDomain().getShift()[indexV2];
			
				Couple newCouple = new Couple(Integer.toString(Integer.parseInt(couple.getValue1()) + shiftV1),
						                      Integer.toString(Integer.parseInt(couple.getValue2()) + shiftV2));
				
				couples.add(newCouple);
			}
			
			int idV1 = csp.getVariables().indexOf(constraint.getVariable1());
			int idV2 = csp.getVariables().indexOf(constraint.getVariable2());
			
			Relation newRelation = new Relation(constraint.getRelation().getTypeRelation(), couples);
			Constraint newConstraint = new Constraint(variables.get(idV1),
					                                  variables.get(idV2),
					                                  newRelation);
			
			constraints.add(newConstraint);
			relations.add(newRelation);
		}
		
		return new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
				          variables, domains, constraints, relations);
	}
	
	public static void convertToXSat(BinCSP csp) {
		BinCSP newCSP = convertToConflicts(csp);
		
		//initialize variables O(|D|)
		int [] variablesCount = new int[newCSP.getNbDomains() + 1];
		variablesCount[0] = 1;
		int index = 1;
		int sum = 1;
		for (Domain domain : newCSP.getDomains()) {
			sum += domain.size();
			variablesCount[index] = sum;
			index ++;
		}
		
		//at most/least one value O(|X|) 
		for (int i = 1 ; i < variablesCount.length ; i++) {
			int nbVariables = variablesCount[i] - variablesCount[i-1];
			int begin = variablesCount[i-1];
			int litteral = begin;
			String clause = "";
			for (int j = 0 ; j < nbVariables ; j++) {
				clause += litteral + " ";
				litteral ++;
			}
			clause += "0";
			System.out.println(clause);
		}
		
		//encoding couples
		int maxLitteral = 0;
		for (Variable variable : newCSP.getVariables()) {
			maxLitteral += variable.getDomain().size();
		}
		maxLitteral += 1;
		
		for (Constraint constraint : newCSP.getConstraints()) {
			int v1Index = Integer.parseInt(constraint.getVariable1().getName().substring(1));
			int v2Index = Integer.parseInt(constraint.getVariable2().getName().substring(1));
			
			int v1Begin = variablesCount[v1Index-1];
			int v2Begin = variablesCount[v2Index-1];
			
			for (Couple couple : constraint.getRelation().getCouples()) {
				String value1 = couple.getValue1();
				String value2 = couple.getValue2();
				
				int v1Position = 0;
				int v2Position = 0;
				
				for (String value : constraint.getVariable1().getDomain().getValues()) {
					if (value1.equals(value)) break;
					v1Position ++;
				}
				
				for (String value : constraint.getVariable2().getDomain().getValues()) {
					if (value2.equals(value)) break;
					v2Position ++;
				}
				
				int v1Variable = v1Begin + v1Position;
				int v2Variable = v2Begin + v2Position;
				
				System.out.println(v1Variable + " " + v2Variable + " " + maxLitteral + " 0");
				maxLitteral += 1;
			}
		}
	}	
	
	//state : 0 true, 1false
	public static Litteral getLitteral(ArrayList<Litteral> litterals, Variable x, int i, ArrayList<Integer> t, int state) {
		int index = x.getIndex();
		int begin = t.get(index);
		return litterals.get(begin + (2 * i) + state);
	}
	
	public static Litteral getLitteralLazy(Litteral [] litterals, 
			Variable x, int i, ArrayList<Integer> t, int state) {
		int index = x.getIndex();
		int begin = t.get(index);
		return litterals[begin + (2 * i) + state];
	}
	
	public static void breakSymmetries2(BinCSP csp) {
		
		ArrayList<String> symetricsValues = new ArrayList<String>(); 
		
		//Calculer les valeurs symétriques.
		for(String value : csp.getVariables().get(0).getDomain().getValues()) {
			boolean isSymetric = true;
			for (int i = 1 ; i < csp.getNbVariables() ; i++) {
				boolean present = false;
				Variable variable = csp.getVariables().get(i);
				
				for (String value2 : variable.getDomain().getValues()) {
					if (value.equals(value2)) {
						present = true;
						break;
					}
				}
				
				if (!present) { 
					isSymetric = false;
					break;
				}
			}
			
			if (isSymetric) {
				symetricsValues.add(value);
			}
		}
		
		int [] availables = new int [symetricsValues.size()];
		for (int i = 0 ; i < availables.length ; i++) {
			availables[i] = csp.getNbVariables();
		}
		
		int V = csp.getNbVariables();
		
		for (int i = 0 ; i < csp.getNbVariables() ; i++) {
			Variable variable = csp.getVariables().get(i);
			
			int indexFirstSymetricValue = -1;
			for (int j = 0 ; j < symetricsValues.size() ; j++) {
				String value = symetricsValues.get(j);
				for (int k = 0 ; k < variable.getDomain().size() ; k++) {
					if (value.equals(variable.getDomain().get(k)) && availables[j] == V) {
						indexFirstSymetricValue = k;
						break;
					}
				}
				if (indexFirstSymetricValue > -1) break;
			}
			
			
			if (indexFirstSymetricValue > -1) {
				
				for (int j = 0 ; j < symetricsValues.size() ; j++) {
					if (j != indexFirstSymetricValue && availables[j] == V) { // == V ?
						String valueToDelete = symetricsValues.get(j);
						for (int k = 0 ; k < variable.getDomain().size() ; k++) {
							if (variable.getDomain().get(k).equals(valueToDelete)) {
									
								availables[j] --;
								
								//Suppression des contraintes associées.
								for (Constraint constraint : csp.getConstraints()) {
									for (int l = 0 ; l < constraint.getRelation().getCouples().size() ; l++) {
										if (constraint.getVariable1().equals(variable) && 
											constraint.getRelation().getCouples().get(l).getValue1().equals(valueToDelete)) {
											
											constraint.getRelation().remove(constraint.getRelation().getCouples().get(l));
										}
										
										else if (constraint.getVariable2().equals(variable) && 
												constraint.getRelation().getCouples().get(l).getValue2().equals(valueToDelete)) {
												
												constraint.getRelation().remove(constraint.getRelation().getCouples().get(l));
										}
									}
								}
								
								//Mise à jour du domaine
								variable.getDomain().remove(k);
								
							}
						}
					} else {
						availables[j] = 0;
						//V--;
					}
				}
				
			}
			V --;
		}
		
		
	}
	
	public static void breakSymmetries(BinCSP csp) {
		int V = csp.getDomains().get(0).size();
		int [] state = new int [V];
		int i = 1;
		for (Variable variable : csp.getVariables()) {
			 for (int index = i ; index < V ; index++) {
				 String value = variable.getDomain().get(i);
				  
				 for (int j = 0 ; j < csp.getNbConstraints() ; j++) {
					Constraint constraint = csp.getConstraints().get(j);
					
					if (constraint.getVariable1().equals(variable)) {
						int l = 0;
						
						for (int k = 0 ; k < constraint.getRelation().getCouples().size() ; k++) {
							Couple couple = constraint.getRelation().getCouples().get(k);
							if (couple.getValue1().equals(value))
								constraint.getRelation().remove(l);
							 l++;
						 }
					 }
					 
					 else if (constraint.getVariable2().equals(variable)) {
							int l = 0;
							
							for (int k = 0 ; k < constraint.getRelation().getCouples().size() ; k++) {
								Couple couple = constraint.getRelation().getCouples().get(k);
								if (couple.getValue2().equals(value))
									constraint.getRelation().remove(l);
								 l++;
							 }
						 }
				 }
				 variable.getDomain().remove(i);
				 if (variable.getDomain().size() == 1) {
					 
				 }
			 }
			 i ++;
		}
	}
	
	public static SAT directEncoding(BinCSP csp) {
		
		int nbLitterals = 0;
		for (Variable v : csp.getVariables()) {
			nbLitterals += v.getDomain().size();
		}
		
		int [] occ = new int [2 * nbLitterals];
		
		BinCSP newCSP = convertToConflicts(csp);
		newCSP = shiftDomains(newCSP);
		
		Litteral [] litterals = new Litteral [nbLitterals * 2];
		ArrayList<Clause> clauses = new ArrayList<Clause>();
		int i = 0;
		int sum = 0;
		
		int nbClauses = 0;
		
		ArrayList<Integer> t = new ArrayList<Integer>();
		t.add(0);
		
		int iL = 0;
		int idVariable = 0;
		for (Variable variable : newCSP.getVariables()) {
			sum += variable.getDomain().size() * 2;
			t.add(sum);
			Clause clause = new Clause(nbClauses);
			
			for (String value : variable.getDomain().getValues()) {
				Litteral x = new Litteral(i, idVariable);
				Litteral nx = new Litteral(i+1, idVariable);
				litterals[iL] = x;
				litterals[iL+1] = nx;
				iL += 2;
				clause.addLitteral(x);
				occ[x.getId()] ++;
				i += 2;
			}
			clauses.add(clause);
			nbClauses ++;
			idVariable ++;
		}
		
		int size = clauses.size();
		for (i = 0 ; i < size ; i++) {
			Clause clause = clauses.get(i);
			for (int j = 0 ; j < clause.getLitterals().size() ; j++) {
				Litteral l1 = clause.get(j);
				for (int k = j+1 ; k < clause.getLitterals().size() ; k++) {
					Litteral l2 = clause.get(k);
					Clause c = new Clause(nbClauses);
					c.addLitteral(litterals[l1.getId()+1]);
					c.addLitteral(litterals[l2.getId()+1]);
					occ[litterals[l1.getId()+1].getId()] ++;
					occ [litterals[l2.getId()+1].getId()] ++;
					clauses.add(c);
					nbClauses ++;
				}
			}
		}
		
		for (Constraint constraint : newCSP.getConstraints()) {
			Variable x = constraint.getVariable1();
			Variable y = constraint.getVariable2();
			int xMin = Integer.parseInt(x.getDomain().get(0));
			int yMin = Integer.parseInt(y.getDomain().get(0));
			for (Couple couple : constraint.getRelation().getCouples()) {
				int xIndex = Integer.parseInt(couple.getValue1()) - xMin;
				int yIndex = Integer.parseInt(couple.getValue2()) - yMin;
				Litteral l1 = getLitteralLazy(litterals, x, xIndex, t, 1);
				Litteral l2 = getLitteralLazy(litterals, y, yIndex, t, 1);
				Clause clause = new Clause(nbClauses);
				clause.addLitteral(l1);
				clause.addLitteral(l2);
				occ[l1.getId()] ++;
				occ[l2.getId()] ++;
				clauses.add(clause);
				nbClauses ++;
			}
		}
		
		int maxOcc = 0;
		for (i = 0 ; i < occ.length ; i++) {
			if (occ[i] > maxOcc) maxOcc = occ[i];
		}
		
		return new SAT(litterals.length/2, clauses.size(), clauses, litterals, maxOcc);	
	}
	
	public static boolean containsCouple(Relation relation, Couple couple) {
		for (Couple c : relation.getCouples()) {
			if (couple.getValue1().equals(c.getValue1()) && couple.getValue2().equals(c.getValue2()))
				return true;
		}
		return false;
	}
	
	public static SAT supportEncoding(BinCSP csp) {
		
		int nbLitterals = 0;
		for (Variable v : csp.getVariables()) {
			nbLitterals += v.getDomain().size();
		}
		
		int [] occ = new int [2 * nbLitterals];

		csp = shiftDomains(csp);
		
		Litteral [] litterals = new Litteral [nbLitterals * 2];
		ArrayList<Clause> clauses = new ArrayList<Clause>();
		int i = 0;
		int sum = 0;
		
		int nbClauses = 0;
		
		ArrayList<Integer> t = new ArrayList<Integer>();
		t.add(0);
		
		int iL = 0;
		int idVariable = 0;
		for (Variable variable : csp.getVariables()) {
			sum += variable.getDomain().size();
			t.add(sum);
			Clause clause = new Clause(nbClauses);
			
			for (String value : variable.getDomain().getValues()) {
				Litteral x = new Litteral(i, idVariable);
				Litteral nx = new Litteral(i+1, idVariable);
				litterals[iL] = x;
				litterals[iL+1] = nx;
				clause.addLitteral(x);
				occ[x.getId()] ++;
				i += 2;
			}
			clauses.add(clause);
			nbClauses ++;
			idVariable ++;
		}
		
		int size = clauses.size();
		for (i = 0 ; i < size ; i++) {
			Clause clause = clauses.get(i);
			for (int j = 0 ; j < clause.getLitterals().size() ; j++) {
				Litteral l1 = clause.get(j);
				for (int k = j+1 ; k < clause.getLitterals().size() ; k++) {
					Litteral l2 = clause.get(k);
					Clause c = new Clause(nbClauses);
					c.addLitteral(litterals[l1.getId()+1]);
					c.addLitteral(litterals[l2.getId()+1]);
					occ [litterals[l1.getId()+1].getId()] ++;
					occ [litterals[l2.getId()+1].getId()] ++;
					clauses.add(c);
					nbClauses ++;
				}
			}
		}
		
		for (i = 0 ; i < csp.getNbVariables() ; i++) {
			for (int j = 0 ; j < csp.getNbVariables() ; j++) {
				Variable x = csp.getVariables().get(i);
				Variable y = csp.getVariables().get(j);
				
				int xMin = Integer.parseInt(x.getDomain().get(0));
				int yMin = Integer.parseInt(y.getDomain().get(0));
				
				for (Constraint constraint : csp.getConstraints()) {
					if ((constraint.getVariable1().equals(x) && constraint.getVariable2().equals(y))) {
						
						for (String vx : x.getDomain().getValues()) {
							Clause clause = new Clause(nbClauses);
							
							int xIndex = Integer.parseInt(vx) - xMin;
							Litteral lx = litterals[(t.get(i)+xIndex)*2];  
							Litteral nlx = litterals[lx.getId() + 1];
							
							clause.addLitteral(nlx);
							occ[nlx.getId()] ++;
							
							for (String vy : y.getDomain().getValues()) {
								int yIndex = Integer.parseInt(vy) - yMin;
								Litteral ly = litterals[(t.get(j)+yIndex)*2];
								
								for (Couple couple : constraint.getRelation().getCouples()) {
									if (couple.getValue1().equals(vx) && couple.getValue2().equals(vy)) {
										clause.addLitteral(ly);
										occ[ly.getId()] ++;
									}
								}
							}
							clauses.add(clause);
							nbClauses ++;
						}
					}
					
					if (constraint.getVariable1().equals(y) && constraint.getVariable2().equals(x)) {
						
						for (String vx : x.getDomain().getValues()) {
							Clause clause = new Clause(nbClauses);
							
							int xIndex = Integer.parseInt(vx) - xMin;
							Litteral lx = litterals[(t.get(i)+xIndex)*2];  
							Litteral nlx = litterals[lx.getId() + 1];
							
							clause.addLitteral(nlx);
							occ[nlx.getId()] ++;
							
							for (String vy : y.getDomain().getValues()) {
								int yIndex = Integer.parseInt(vy) - yMin;
								Litteral ly = litterals[(t.get(j)+yIndex)*2];
								
								for (Couple couple : constraint.getRelation().getCouples()) {
									if (couple.getValue2().equals(vx) && couple.getValue1().equals(vy)) {
										clause.addLitteral(ly);
										occ[ly.getId()] ++;
									}
								}
							}
							clauses.add(clause);
							nbClauses ++;
						}
					}
					
				}
			}
		}
		
		int maxOcc = 0;
		for (i = 0 ; i < occ.length ; i++) {
			if (occ[i] > maxOcc) maxOcc = occ[i];
		}
		
		return new SAT(litterals.length/2, clauses.size(), clauses, litterals, maxOcc);	
	}
	
	public static void main(String [] args) {
		BinCSP csp = Generator.generateRandomCSPSupport(3,3);
		System.out.println(csp.toString());
		SAT sat = BinCSPConverter.supportEncoding(csp);
		System.out.println("#########");
		System.out.println(sat.toString());
	}
}
