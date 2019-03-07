package generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import bincsp.BinCSP;
import bincsp.Constraint;
import bincsp.Couple;
import bincsp.Domain;
import bincsp.Relation;
import bincsp.Relation.TypeRelation;
import bincsp.Variable;


public class Generator {

	public static BinCSP generateUncompleteGraphColoration(int nbVertexs, int nbColors, double density) {
		
		ArrayList<Variable> variables = new ArrayList<Variable>();
		ArrayList<Domain> domains = new ArrayList<Domain>();
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
		ArrayList<Relation> relations = new ArrayList<Relation>();
		
		int nbMaxEdges = (nbVertexs * (nbVertexs - 1)) / 2;
		int nbEdges = (int) (density * nbMaxEdges);
		
		int [][] M = new int [nbVertexs][nbVertexs];
		
		int i = 0;
		while(i < nbEdges) {
			int x = ThreadLocalRandom.current().nextInt(1, nbVertexs);
			int y = ThreadLocalRandom.current().nextInt(1, nbVertexs);
			
			if ((x != y) && (M[x][y] == 0)) {
				M[x][y] = 1;
				M[y][x] = 1;
				i ++;
			}
		}
		
		int v = 0;
		for (i = 0 ; i < nbVertexs ; i++) {
			String name = "D" + Integer.toString(i); 
			ArrayList<String> values = new ArrayList<String>();
			for(int j = 0 ; j < nbColors ; j++) {
				values.add(Integer.toString(v));
				v++;
			}
			domains.add(new Domain(name, values));
		}
		
		for (i = 0 ; i < nbVertexs ; i++) {
			String name = "X" + i;
			variables.add(new Variable(name, domains.get(i)));
		}
		
		for (i = 0 ; i < nbVertexs ; i++) {
			for (int j = i + 1 ; j < nbVertexs ; j++) {
				if (M[i][j] == 1) {
					
					Variable x = variables.get(i);
					Variable y = variables.get(j);
					
					ArrayList<Couple> couples = new ArrayList<Couple>();
					
					for (int col = 0 ; col < nbColors ; col ++) {
						String v1 = Integer.toString((i * nbColors) + col);
						String v2 = Integer.toString((j * nbColors) + col);
						couples.add(new Couple(v1, v2));
					}
					
					Relation relation = new Relation(TypeRelation.R_CONFLICTS, couples);
					Constraint constraint = new Constraint(x, y, relation);
					
					constraints.add(constraint);
					relations.add(relation);
				}
			}
		}
		
		return new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
		          variables, domains, constraints, relations);
	}
	
	public static BinCSP generateCompleteGraphColoration(int nbVertexs, int nbColors) {
		
		ArrayList<Variable> variables = new ArrayList<Variable>();
		ArrayList<Domain> domains = new ArrayList<Domain>();
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
		ArrayList<Relation> relations = new ArrayList<Relation>();
		
		int v = 0;
		for (int i = 0 ; i < nbVertexs ; i++) {
			String name = "D" + Integer.toString(i); 
			ArrayList<String> values = new ArrayList<String>();
			for(int j = 0 ; j < nbColors ; j++) {
				values.add(Integer.toString(v));
				v++;
			}
			domains.add(new Domain(name, values));
		}
		
		for (int i = 0 ; i < nbVertexs ; i++) {
			String name = "X" + i;
			variables.add(new Variable(name, domains.get(i)));
		}
		
		int r = 0;
		for (int i = 0 ; i < nbVertexs ; i++) {
			for (int j = i+1 ; j < nbVertexs ; j++) {
				Variable x = variables.get(i);
				Variable y = variables.get(j);
				
				ArrayList<Couple> couples = new ArrayList<Couple>();
				
				for (int col = 0 ; col < nbColors ; col ++) {
					String v1 = Integer.toString((i * nbColors) + col);
					String v2 = Integer.toString((j * nbColors) + col);
					couples.add(new Couple(v1, v2));
				}
				
				Relation relation = new Relation(TypeRelation.R_CONFLICTS, couples);
				Constraint constraint = new Constraint(x, y, relation);
				
				constraints.add(constraint);
				relations.add(relation);
			}
		}
		
		return new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
		          variables, domains, constraints, relations);
	}
	
	public static BinCSP exempleGraphCol() {
		
		ArrayList<Domain> domains = new ArrayList<Domain>( Arrays.asList(new Domain [] {
				new Domain("D0", new ArrayList<String>(Arrays.asList(new String [] {"0", "1", "2", "3"}))),
				new Domain("D1", new ArrayList<String>(Arrays.asList(new String [] {"4", "5", "6", "7"}))),
				new Domain("D2", new ArrayList<String>(Arrays.asList(new String [] {"8", "9", "10", "11"}))),
				new Domain("D3", new ArrayList<String>(Arrays.asList(new String [] {"12", "13", "14", "15"}))),
				new Domain("D4", new ArrayList<String>(Arrays.asList(new String [] {"16", "17", "18", "19"})))
		}));
		
		ArrayList<Variable> variables = new ArrayList<Variable>( Arrays.asList(new Variable [] {
				new Variable("V", domains.get(0), 0),
				new Variable("W", domains.get(1), 1),
				new Variable("X", domains.get(2), 2),
				new Variable("Y", domains.get(3), 3),
				new Variable("Z", domains.get(4), 4)
		}));
		
		ArrayList<Couple> cvw = new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("0", "4"), new Couple("1", "5"), new Couple("2", "6"), new Couple("3", "7")
		}));
		
		ArrayList<Couple> cvx = new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("0", "8"), new Couple("1", "9"), new Couple("2", "10"), new Couple("3", "11")
		}));
		
		ArrayList<Couple> cwy = new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("4", "12"), new Couple("5", "13"), new Couple("6", "14"), new Couple("7", "15")
		}));
		
		ArrayList<Couple> cyz = new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("12", "16"), new Couple("13", "17"), new Couple("14", "18"), new Couple("15", "19")
		}));
		
		ArrayList<Couple> cxz = new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("8", "16"), new Couple("9", "17"), new Couple("10", "18"), new Couple("11", "19")
		}));
		
		ArrayList<Relation> relations = new ArrayList<Relation>(Arrays.asList(new Relation [] {
				new Relation (TypeRelation.R_CONFLICTS, cvw),
				new Relation (TypeRelation.R_CONFLICTS, cvx),
				new Relation (TypeRelation.R_CONFLICTS, cwy),
				new Relation (TypeRelation.R_CONFLICTS, cyz),
				new Relation (TypeRelation.R_CONFLICTS, cxz)
		}));
		
		ArrayList<Constraint> constraints = new ArrayList<Constraint>(Arrays.asList(new Constraint [] {
				new Constraint(variables.get(0), variables.get(1), relations.get(0)),
				new Constraint(variables.get(0), variables.get(2), relations.get(1)),
				new Constraint(variables.get(1), variables.get(3), relations.get(2)),
				new Constraint(variables.get(3), variables.get(4), relations.get(3)),
				new Constraint(variables.get(2), variables.get(4), relations.get(4))
			}));
		
		return new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
		          variables, domains, constraints, relations);
	}
	
	public static BinCSP colSat() {
		
		ArrayList<Domain> domain = new ArrayList<Domain>( Arrays.asList(new Domain [] {
				new Domain("D0", new ArrayList<String>(Arrays.asList(new String [] {"0", "1", "2", "3"}))),
				new Domain("D1", new ArrayList<String>(Arrays.asList(new String [] {"4", "5", "6", "7"}))),
				new Domain("D2", new ArrayList<String>(Arrays.asList(new String [] {"8", "9", "10", "11"}))),
				new Domain("D3", new ArrayList<String>(Arrays.asList(new String [] {"12", "13", "14", "15"})))
		}));
		
		ArrayList<Variable> variables = new ArrayList<Variable>( Arrays.asList(new Variable [] {
				new Variable("W", domain.get(0), 0),
				new Variable("X", domain.get(1), 1),
				new Variable("Y", domain.get(2), 2),
				new Variable("Z", domain.get(3), 3)
		}));
		
		ArrayList<Couple> cwx = new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("0", "4"), new Couple("1", "5"), new Couple("2", "6"), new Couple("3", "7")
		}));
		
		ArrayList<Couple> cwy = new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("0", "8"), new Couple("1", "9"), new Couple("2", "10"), new Couple("3", "11")
		}));
		
		ArrayList<Couple> cwz = new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("0", "12"), new Couple("1", "13"), new Couple("2", "14"), new Couple("3", "15")
		}));
		
		ArrayList<Couple> cxy = new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("4", "8"), new Couple("5", "9"), new Couple("6", "10"), new Couple("7", "11")
		}));
		
		ArrayList<Couple> cxz = new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("4", "12"), new Couple("5", "13"), new Couple("6", "14"), new Couple("7", "15")
		}));
		
		ArrayList<Couple> cyz = new ArrayList<Couple>(Arrays.asList(new Couple [] {
				new Couple("8", "12"), new Couple("9", "13"), new Couple("10", "14"), new Couple("11", "15")
		}));
		
		ArrayList<Relation> relations = new ArrayList<Relation>(Arrays.asList(new Relation [] {
			new Relation (TypeRelation.R_CONFLICTS, cwx),
			new Relation (TypeRelation.R_CONFLICTS, cwy),
			new Relation (TypeRelation.R_CONFLICTS, cwz),
			new Relation (TypeRelation.R_CONFLICTS, cxy),
			new Relation (TypeRelation.R_CONFLICTS, cxz),
			new Relation (TypeRelation.R_CONFLICTS, cyz)
			
		}));
		
		ArrayList<Constraint> constraints = new ArrayList<Constraint>(Arrays.asList(new Constraint [] {
			new Constraint(variables.get(0), variables.get(1), relations.get(0)),
			new Constraint(variables.get(0), variables.get(2), relations.get(1)),
			new Constraint(variables.get(0), variables.get(3), relations.get(2)),
			new Constraint(variables.get(1), variables.get(2), relations.get(3)),
			new Constraint(variables.get(1), variables.get(3), relations.get(4)),
			new Constraint(variables.get(2), variables.get(3), relations.get(5))
		}));
		
		return new BinCSP(variables.size(), domain.size(), constraints.size(), relations.size(),
		          variables, domain, constraints, relations);
	}
	
	public static BinCSP generateProblemWithoutConstraints(int nbVariables, int domainSize) {
		
		int v = 0;
		
		ArrayList<Domain> domains = new ArrayList<Domain>();
		for (int i = 0 ; i < nbVariables ; i++) {
			ArrayList<String> values = new ArrayList<String>();
			for (int j = 0 ; j < domainSize ; j++) {
				values.add(Integer.toString(v));
				v++;
			}
			domains.add(new Domain("D"  + i, values));
		}
		
		ArrayList<Variable> variables = new ArrayList<Variable>();
		for (int i = 0 ; i < nbVariables ; i++) {
			variables.add(new Variable("X" + i, domains.get(i), i));
		}
		
		ArrayList<Relation> relations = new ArrayList<Relation>();
		int nbRelations = (nbVariables * (nbVariables - 1)) / 2;
		for (int i = 0 ; i < nbRelations ; i++) {
			relations.add(new Relation(TypeRelation.R_CONFLICTS, new ArrayList<Couple>()));
		}
		
		int index = 0;
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
		for (int i = 0 ; i < nbVariables ; i++) {
			for (int j = i + 1 ; j < nbVariables ; j++) {
				Variable x = variables.get(i);
				Variable y = variables.get(j);
				Relation r = relations.get(index);
				constraints.add(new Constraint(x, y, r));
				index ++;
			}
		}
		
		return new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
				          variables, domains, constraints, relations);
	}
	
	public static BinCSP generatePigeons(int nbPigeons, int nbNids) {
		
		/* Generate variables and domains */
		ArrayList<Domain> domains = new ArrayList<Domain>();
		ArrayList<Variable> variables = new ArrayList<Variable>();
		
		int v = 0;
		for (int i = 0 ; i < nbPigeons ; i++) {
			ArrayList<String> values = new ArrayList<String>();
			for (int j = 0 ; j < nbNids ; j++) {
				//values.add(Integer.toString(v));
				values.add(Integer.toString(j));
				v ++;
			}
			Domain d = new Domain("D" + i, values);
			domains.add(d);
			variables.add(new Variable("X" + i, d, i));
		}
		
		/* Generate constraints and relations */
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
		ArrayList<Relation> relations = new ArrayList<Relation>();
		
		for (int i = 0 ; i < variables.size() ; i++) {
			for (int j = i+1 ; j < variables.size() ; j++) {
				Variable x = variables.get(i);
				Variable y = variables.get(j);
				
				ArrayList<Couple> couples = new ArrayList<Couple>();
				for (int k = 0 ; k < nbNids ; k++) {
					couples.add(new Couple(x.getDomain().get(k), y.getDomain().get(k)));
				}
				
				Relation r = new Relation(TypeRelation.R_CONFLICTS, couples);
				Constraint c = new Constraint(x, y, r);
				
				relations.add(r);
				constraints.add(c);
			}
		}
				
		return new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
				          variables, domains, constraints, relations);
	}
	
	public static BinCSP generateUNSATCSP(int nbVariables, int domainSize) {
		
		ArrayList<Domain> domains = new ArrayList<Domain>();
		ArrayList<Variable> variables = new ArrayList<Variable>();
		int v = 0;
		for (int i = 0 ; i < nbVariables ; i++) {
			ArrayList<String> values = new ArrayList<String>();
			for (int j = 0 ; j < domainSize ; j++) {
				values.add(Integer.toString(v));
				v++;
			}
			domains.add(new Domain("D"+ i, values));
			variables.add(new Variable("X" + i, domains.get(i), i));
		}
		
		
		ArrayList<Relation> relations = new ArrayList<Relation>();
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
		for (int i = 0 ; i < nbVariables ; i++) {
			Variable v1 = variables.get(i);
			if (i < nbVariables -1) {
				Variable v2 = variables.get(i+1);
				ArrayList<Couple> couples = new ArrayList<Couple>();
				for (int j = 0 ; j < domainSize ; j++) {
					Couple c = new Couple(v1.getDomain().get(j), v2.getDomain().get(j));
					couples.add(c);
				}
				Relation r = new Relation(TypeRelation.R_SUPPORTS, couples);	
				Constraint cons = new Constraint(v1, v2, r);
				relations.add(r);
				constraints.add(cons);
			} else {
				Variable v2 = variables.get(0);
				ArrayList<Couple> couples = new ArrayList<Couple>();
				for (int j = 0 ; j < domainSize ; j++) {
					if (j < domainSize -1) {
						Couple c = new Couple(v1.getDomain().get(j), v2.getDomain().get(j+1));
						couples.add(c);
					} else {
						Couple c = new Couple(v1.getDomain().get(j), v2.getDomain().get(0));
						couples.add(c);
					}
				}
				Relation r = new Relation(TypeRelation.R_SUPPORTS, couples);
				relations.add(r);
				Constraint c = new Constraint(v1, v2, r);
				constraints.add(c);
			}
		}
		
		return new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(), 
				          variables, domains, constraints, relations);
	}
	
	public static BinCSP generateBinCSP(int nbVariables,
										int minSizeDomain, int maxSizeDomain,
			                            int nbConstraints, int maxNbCouples,
			                            boolean export, String path) {
		
		//generate domains
		int maxValue = 0;
		ArrayList<Domain> domains = new ArrayList<Domain>();
		for (int i = 0 ; i < nbVariables ; i++) {
			int size = ThreadLocalRandom.current().nextInt(minSizeDomain, maxSizeDomain + 1);
			ArrayList<String> values = new ArrayList<String>();
			for (int j = 0 ; j < size ; j++) {
				values.add(Integer.toString(maxValue));
				maxValue += 1;
			}
			domains.add(new Domain("D" + i, values));
		}
		
		//generate variables
		ArrayList<Variable> variables = new ArrayList<Variable>();

		for (int i = 0 ; i < nbVariables ; i++) {
			variables.add(new Variable("X" + (i+1), domains.get(i), i));
		}
		
		//generate constraints and relations
		ArrayList<Relation> relations = new ArrayList<Relation>();
		ArrayList<Constraint> constraints = new ArrayList<Constraint>();
		for (int i = 0 ; i < nbConstraints ; i++) {
			
			int idV1 = ThreadLocalRandom.current().nextInt(0, nbVariables);
			int idV2 = idV1;
			while (idV1 == idV2) {
				idV2 = ThreadLocalRandom.current().nextInt(0, nbVariables);
			}
			
			Variable v1 = variables.get(idV1);
			Variable v2 = variables.get(idV2);
			
			@SuppressWarnings("unused")
			int random = ThreadLocalRandom.current().nextInt(0, 2);
			TypeRelation type = TypeRelation.R_CONFLICTS;
			
			int max = Integer.max(v1.getDomain().size(), v2.getDomain().size());
			max = Integer.min(max, maxNbCouples);
			
			int nbCouples = ThreadLocalRandom.current().nextInt(1, max+1);
			ArrayList<Couple> couples = new ArrayList<Couple>();
			for (int j = 0 ; j < nbCouples ; j++) {
				int idDomain1 = ThreadLocalRandom.current().nextInt(0, v1.getDomain().size());
				int idDomain2 = ThreadLocalRandom.current().nextInt(0, v2.getDomain().size());
				
				String value1 = v1.getDomain().get(idDomain1);
				String value2 = v2.getDomain().get(idDomain2);
				
				couples.add(new Couple(value1, value2));
			}
			
			Relation relation = new Relation(type, couples);
			relations.add(relation);
			constraints.add(new Constraint(v1, v2, relation));
		}
		
		BinCSP csp = new BinCSP(variables.size(), domains.size(), constraints.size(), relations.size(),
		                        variables, domains, constraints, relations);
		
		if (export)
			BinCSP.exportToXCSP3(csp, path);
		
		return csp;
	}
	
	public static void main(String [] args) {
		BinCSP csp = generateUncompleteGraphColoration(10, 3, 0.1);
		System.out.println(csp.toString());
	}
}
