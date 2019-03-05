package Main;

import bincsp.BinCSP;
import generator.Generator;
import parser.Parser;
import solver_sat.Solver;

public class Launcher {

	public static void displayStart() {
		System.out.println("###############################");
		System.out.println("# CSP SOLVER");
		System.out.println("# @authors");
		System.out.println("# OSTROWSKI RICHARD");
		System.out.println("# VARET ADRIEN");
		System.out.println("###############################");
	}
	
	public static void displayUsage() {
		System.out.println("# USAGE : java -jar name.jar -r benchname");
		System.out.println("#         java -jar name.jar -g nbVariables domainSize");
	}
	
	public static void main(String [] args) throws Exception {
		displayStart();
		
		if (args.length < 2) {
			System.out.println("# Invalid command");
			displayUsage();
			System.out.println("###############################");
			System.exit(1);
		}
		
		if (args[0].equals("-r")) {
			String filename = args[1];
			long begin = System.currentTimeMillis();
			Parser parser = new Parser(filename);
			BinCSP csp = parser.buildCSP();
			long end = System.currentTimeMillis();
			long parseTime = end - begin;
			begin = System.currentTimeMillis();
			Solver.solve(csp);
			end = System.currentTimeMillis();
			long solveTime = end - begin;
			System.out.println("# Parse time : " + parseTime + ", Solve time : " + solveTime + 
					"total : " + (parseTime + solveTime));
		}
		
		else if (args[0].equals("-g")) {
			int nbVariables = Integer.parseInt(args[1]);
			int domainSize  = Integer.parseInt(args[2]);
			BinCSP csp = Generator.generateUNSATCSP(nbVariables, domainSize);
			Solver.solve(csp);
		}
		
		else {
			System.out.println("# Invalid command");
			displayUsage();
		}
		
		System.out.println("###############################");
	}
}
