package latex_parser;

import bincsp.BinCSP;
import conversion.BinCSPConverter;
import generator.Generator;
import sat.SAT;
import utils.GenericCouple;

public class ExportSATProblem {

	public static void exportPigeons(int n, int d, String outputFileName) {
		GenericCouple<BinCSP> couple = Generator.generatePigeonDirectSupport(n, d);
		SAT satDirect = BinCSPConverter.directEncoding(couple.getV1());
		SAT satSupport = BinCSPConverter.supportEncoding(couple.getV2());
		
		String outputDirectName = outputFileName + "_direct.cnf";
		String outputSupportName = outputFileName + "_support.cnf";
		
		satDirect.exportToCNFFile(outputDirectName);
		satSupport.exportToCNFFile(outputSupportName);
	}
	
	public static void main(String [] args) {
		for (int i = 7 ; i < 14 ; i++) {
			exportPigeons(i, i-1, "pigeons/pigeons_" + Integer.toString(i));
		}
	}
}
