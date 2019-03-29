package generator;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import bincsp.BinCSP;
import conversion.BinCSPConverter;
import sat.SAT;
import utils.GenericCouple;

public class InstanceGenerator {

	public static void generateRandomGraphsInstances() {
		
		int n = 25;
		int d = 11;
		java.text.DecimalFormat df = new java.text.DecimalFormat("0.###");
		for (double i = 0.05 ; i < 1 ; i += 0.05) {
			double dens = i;
			GenericCouple<BinCSP> couple = Generator.generateRandomGraphDS(n, d, dens);
			SAT satDirect = BinCSPConverter.directEncoding(couple.getV1());
			SAT satSupport = BinCSPConverter.supportEncoding(couple.getV2());
			
			String outputFileName = "instances/random_graphs/random_graph_" + n + "_" + d + "_" + df.format(dens);
			satDirect.exportToCNFFile(outputFileName + "_direct.cnf");
			satSupport.exportToCNFFile(outputFileName + "_support.cnf");
			
			System.out.println(outputFileName + "gen"); 
		}
	}
	
	public static void generateRandomCSP() {
		for (int n = 30 ; n <= 50 ; n += 5) {
			for (int d = 5 ; d <= 12 ; d += 2) {
				for (int i = 1 ; i < 10 ; i += 2) {
					double density = (double)i/10;
					for (int j = 1 ; j < 10 ; j += 2) {
						double k = (double) j/10;
						int hardness = (int) ((d*d) * k);
						GenericCouple<BinCSP> couple = Generator.generateRandomProblem(n, d, density, hardness);
						SAT satDirect = BinCSPConverter.directEncoding(couple.getV1());
						SAT satSupport = BinCSPConverter.supportEncoding(couple.getV2());
						
						String outputFileName = "instances/random_csp/random_csp" + n + "_" + d + "_" + density + "_" + k;
						satDirect.exportToCNFFile(outputFileName + "_direct.cnf");
						satSupport.exportToCNFFile(outputFileName + "_support.cnf");
						
						System.out.println(outputFileName + " generated");
					}
				}
			}
		}
	}
	
	public static void main(String [] args) {
		generateRandomGraphsInstances();
	}
}
