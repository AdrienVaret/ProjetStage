package utils;

import java.util.ArrayList;
import java.util.Arrays;

import bincsp.BinCSP;
import bincsp.Constraint;
import bincsp.Couple;
import bincsp.Domain;
import bincsp.Relation;
import bincsp.Variable;
import bincsp.Relation.TypeRelation;

public class CreateCSP {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static BinCSP createBinCSP_1() {
		ArrayList<String> vdx1 = new ArrayList(Arrays.asList(new String [] {"1", "3", "11"}));
		ArrayList<String> vdx2 = new ArrayList(Arrays.asList(new String [] {"2", "1", "9", "13"}));
		ArrayList<String> vdx3 = new ArrayList(Arrays.asList(new String [] {"16", "17", "18"}));
		ArrayList<String> vdx4 = new ArrayList(Arrays.asList(new String [] {"113"}));
		
		Domain Dx1 = new Domain("D1", vdx1);
		Domain Dx2 = new Domain("D2", vdx2);
		Domain Dx3 = new Domain("D3", vdx3);
		Domain Dx4 = new Domain("D4", vdx4);
		
		ArrayList<Domain> domains = new ArrayList<Domain>(Arrays.asList(new Domain [] {
				Dx1, Dx2, Dx3, Dx4
		}));
		
		Variable x1 = new Variable("X", Dx1, 0);
		Variable x2 = new Variable("Y", Dx2, 1);
		Variable x3 = new Variable("Z", Dx3, 2);
		Variable x4 = new Variable("A", Dx4, 3);
		
		ArrayList<Variable> variables = new ArrayList<Variable>(Arrays.asList(new Variable [] {
				x1, x2, x3, x4
		}));
		
		Relation RCx1x2 = new Relation(TypeRelation.R_CONFLICTS,
				                       new ArrayList<Couple>(Arrays.asList(new Couple [] {
				                    		   new Couple("1", "2"),
				                    		   new Couple("3", "13")
				                       })));
		
		Relation RCx2x4 = new Relation(TypeRelation.R_CONFLICTS,
                new ArrayList<Couple>(Arrays.asList(new Couple [] {
             		   new Couple("9", "113")
                })));
		
		Relation RCx3x4 = new Relation(TypeRelation.R_CONFLICTS,
                new ArrayList<Couple>(Arrays.asList(new Couple [] {
             		   new Couple("16", "113")
                })));
		
		ArrayList<Relation> relations = new ArrayList<Relation>(Arrays.asList(new Relation [] {
				RCx1x2, RCx2x4, RCx3x4
		}));
		
		Constraint Cx1x2 = new Constraint(x1, x2, RCx1x2);
		Constraint Cx2x4 = new Constraint(x2, x4, RCx2x4);
		Constraint Cx3x4 = new Constraint(x3, x4, RCx3x4);
	
		ArrayList<Constraint> constraints = new ArrayList<Constraint>(Arrays.asList(new Constraint[] {
				Cx1x2, Cx2x4, Cx3x4
		}));
		
		BinCSP csp = new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
				                variables, domains, constraints, relations);
		
		return csp;
	}
	
	public static BinCSP createBinCSP3COL_3() {
		
		ArrayList<Domain> domains = new ArrayList<Domain>(Arrays.asList(new Domain [] {
				new Domain("D0", new ArrayList<String>(Arrays.asList(new String [] {
						"0", "1", "2"	
					}))),
				new Domain("D0", new ArrayList<String>(Arrays.asList(new String [] {
						"0", "1", "2"	
					}))),
				new Domain("D0", new ArrayList<String>(Arrays.asList(new String [] {
						"0", "1", "2"	
					})))
		}));
		
		ArrayList<Variable> variables = new ArrayList<Variable>(Arrays.asList(new Variable[] {
				new Variable("X", domains.get(0), 0),
				new Variable("Y", domains.get(1), 1),
				new Variable("Z", domains.get(2), 2)
		}));
		
		ArrayList<Couple> couples = new ArrayList<Couple>(Arrays.asList(new Couple[] {
			new Couple("0", "0"), 
			new Couple("1", "1"),
			new Couple("2", "2")
		}));
		
		ArrayList<Relation> relations = new ArrayList<Relation>(Arrays.asList(new Relation[] {
				new Relation(TypeRelation.R_CONFLICTS, couples),
				new Relation(TypeRelation.R_CONFLICTS, couples),
				new Relation(TypeRelation.R_CONFLICTS, couples)
		}));
		
		ArrayList<Constraint> constraints = new ArrayList<Constraint>(Arrays.asList(new Constraint[] {
			new Constraint(variables.get(0), variables.get(1), relations.get(0)),
			new Constraint(variables.get(1), variables.get(2), relations.get(1)),
			new Constraint(variables.get(2), variables.get(0), relations.get(2))
		}));
		
		return new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(), 
				          variables, domains, constraints, relations);
	}
	
	public static BinCSP createBinCSP3COLSquare() {
		
		ArrayList<Domain> domains = new ArrayList<Domain>(Arrays.asList(new Domain [] {
			new Domain("D0", new ArrayList<String>(Arrays.asList(new String[] {"0", "1", "2"}))),
			new Domain("D1", new ArrayList<String>(Arrays.asList(new String[] {"3", "4", "5"}))),
			new Domain("D2", new ArrayList<String>(Arrays.asList(new String[] {"6", "7", "8"}))),
			new Domain("D3", new ArrayList<String>(Arrays.asList(new String[] {"9", "10", "11"})))
		}));
		
		ArrayList<Variable> variables = new ArrayList<Variable>(Arrays.asList(new Variable[] {
			new Variable("W", domains.get(0), 0),
			new Variable("X", domains.get(1), 1),
			new Variable("Y", domains.get(2), 2),
			new Variable("Z", domains.get(3), 3)
		}));
		
		ArrayList<Relation> relations = new ArrayList<Relation>(Arrays.asList(new Relation [] {
			new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
					new Couple("0", "3"), new Couple("1", "4"), new Couple("2", "5")
			}))),
			new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
					new Couple("3", "6"), new Couple("4", "7"), new Couple("5", "8")
			}))),
			new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
					new Couple("6", "9"), new Couple("7", "10"), new Couple("8", "11")
			}))),
			new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
					new Couple("9", "0"), new Couple("10", "1"), new Couple("11", "2")
			}))),
			new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
					new Couple("0", "6"), new Couple("1", "7"), new Couple("2", "8")
			})))
		}));
		
		ArrayList<Constraint> constraints = new ArrayList<Constraint>(Arrays.asList(new Constraint[] {
			new Constraint(variables.get(0), variables.get(1), relations.get(0)),
			new Constraint(variables.get(1), variables.get(2), relations.get(1)),
			new Constraint(variables.get(2), variables.get(3), relations.get(2)),
			new Constraint(variables.get(3), variables.get(0), relations.get(3)),
			new Constraint(variables.get(0), variables.get(2), relations.get(4))
		}));
		
		return new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
				          variables, domains, constraints, relations);
	}
	
	
	public static BinCSP createUNSAT3COLCSP() {
		
		ArrayList<Domain> domains = new ArrayList<Domain>(Arrays.asList(new Domain [] {
			new Domain("D0", new ArrayList<String>(Arrays.asList(new String[] {"0", "1", "2"}))),
			new Domain("D1", new ArrayList<String>(Arrays.asList(new String[] {"3", "4", "5"}))),
			new Domain("D2", new ArrayList<String>(Arrays.asList(new String[] {"6", "7", "8"}))),
			new Domain("D3", new ArrayList<String>(Arrays.asList(new String[] {"9", "10", "11"})))
		}));
		
		ArrayList<Variable> variables = new ArrayList<Variable>(Arrays.asList(new Variable[] {
			new Variable("W", domains.get(0), 0),
			new Variable("X", domains.get(1), 1),
			new Variable("Y", domains.get(2), 2),
			new Variable("Z", domains.get(3), 3)
		}));
		
		ArrayList<Relation> relations = new ArrayList<Relation>(Arrays.asList(new Relation [] {
			new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
					new Couple("0", "3"), new Couple("1", "4"), new Couple("2", "5")
			}))),
			new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
					new Couple("3", "6"), new Couple("4", "7"), new Couple("5", "8")
			}))),
			new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
					new Couple("6", "9"), new Couple("7", "10"), new Couple("8", "11")
			}))),
			new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
					new Couple("9", "0"), new Couple("10", "1"), new Couple("11", "2")
			}))),
			new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
					new Couple("0", "6"), new Couple("1", "7"), new Couple("2", "8")
			}))),
			new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
					new Couple("3", "9"), new Couple("4", "10"), new Couple("5", "11")
			})))
		}));
		
		ArrayList<Constraint> constraints = new ArrayList<Constraint>(Arrays.asList(new Constraint[] {
			new Constraint(variables.get(0), variables.get(1), relations.get(0)),
			new Constraint(variables.get(1), variables.get(2), relations.get(1)),
			new Constraint(variables.get(2), variables.get(3), relations.get(2)),
			new Constraint(variables.get(3), variables.get(0), relations.get(3)),
			new Constraint(variables.get(0), variables.get(2), relations.get(4)),
			new Constraint(variables.get(1), variables.get(3), relations.get(5))
		}));
		
		return new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
		          variables, domains, constraints, relations);
	}
	
	public static BinCSP createUNSATProblem() {
		
		ArrayList<Domain> domains = new ArrayList<Domain>(Arrays.asList(new Domain[] {
				new Domain("D0", new ArrayList<String>( Arrays.asList(new String [] {"1", "2", "3"}))),
				new Domain("D1", new ArrayList<String>( Arrays.asList(new String [] {"4", "5", "6"}))),
				new Domain("D2", new ArrayList<String>( Arrays.asList(new String [] {"7", "8", "9"}))),
		}));
		
		ArrayList<Variable> variables = new ArrayList<Variable>(Arrays.asList(new Variable[] {
			new Variable ("X", domains.get(0), 0),
			new Variable ("Y", domains.get(1), 1),
			new Variable ("Z", domains.get(2), 2)
		}));
		
		Relation rxy = new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
			new Couple("1", "4"), new Couple("1", "5"), new Couple("1", "6"),
			new Couple("2", "4"), new Couple("2", "5"), new Couple("2", "6"),
			new Couple("3", "4"), new Couple("3", "5"), new Couple("3", "6")
		})));
		
		Relation rxz = new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("1", "7"), new Couple("1", "8"), new Couple("1", "9"),
				new Couple("2", "7"), new Couple("2", "8"), new Couple("2", "9"),
				new Couple("3", "7"), new Couple("3", "8"), new Couple("3", "9")
			})));
		
		Relation ryz = new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("4", "7"), new Couple("4", "8"), new Couple("4", "9"),
				new Couple("5", "7"), new Couple("5", "8"), new Couple("5", "9"),
				new Couple("6", "7"), new Couple("6", "8"), new Couple("6", "9")
			})));
		
		ArrayList<Relation> relations = new ArrayList<Relation>();
		relations.add(rxy);
		relations.add(rxz);
		relations.add(ryz);
		
		ArrayList<Constraint> constraints = new ArrayList<Constraint>(Arrays.asList(new Constraint [] {
				new Constraint(variables.get(0), variables.get(1), rxy),
				new Constraint(variables.get(0), variables.get(2), rxz),
				new Constraint(variables.get(1), variables.get(2), ryz)
		}));
		
		return new BinCSP(3, 3, 3, 3, variables, domains, constraints, relations);
	}
}
