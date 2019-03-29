package latex_parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RandomGraphParser implements Parser{

	public void header(BufferedWriter writer) throws IOException {
		writer.write("\\begin{center} \n");
		writer.write("\t\\begin{tabular}{| c | c | c | c | c | c | c | c | c |} \n");
		writer.write("\t\t\\hline \n");
		writer.write("\t\t & & & \\multicolumn{3}{|c|}{DE} & \\multicolumn{3}{|c|}{SE} \\\\ \\hline \n");
		writer.write("\t\t n & d & dens & nous & FC & minisat & nous & FC & minisat \\\\ \\hline \n");
	}
	
	public void footer(BufferedWriter writer) throws IOException {
		writer.write("\t\t\\hline \n");
		writer.write("\t\\end{tabular} \n");
		writer.write("\\end{center}");
	}
	
int lineNumber = 1;
	
	@Override
	public void parse(String inputFileName, String outputFileName) {
		try {
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(new File(inputFileName)));
			String line = null;
				
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
			
			header(writer);
			
			while((line = reader.readLine()) != null) {
				String [] splittedLine = line.split(" ");
				
				String n = splittedLine[0];
				String d = splittedLine[1];
				String density = splittedLine[2];
				
				String nousDE = splittedLine[3];
				String noeudNousDE = splittedLine[4];
				String FCDE = splittedLine[5];
				String noeudFCDE = splittedLine[6];
				String minisatDE = splittedLine[7];
				String noeudMinisatDE = splittedLine[8];
				
				String nousSE = splittedLine[9];
				String noeudNousSE = splittedLine[10];
				String FCSE = splittedLine[11];
				String noeudFCSE = splittedLine[12];
				String minisatSE = splittedLine[13];
				String noeudMinisatSE = splittedLine[14];
					
				writer.write("\t\t" + n + " & " + d + " & " + density + " & (" + nousDE + ", " + noeudNousDE + ") & (" + FCDE + ", " + noeudFCDE + ") & (" 
				            + minisatDE + ", " + noeudMinisatDE + ") & (" + nousSE + ", " + noeudNousSE + ") & (" + FCSE + ", " + noeudFCSE + ") & ("
						+ minisatSE + ", " + noeudMinisatSE +") \\\\ \\hline \n" );
				
				lineNumber ++;
				
				if (lineNumber % 30 == 0) {
					footer(writer);
					writer.write("\newpage");
					header(writer);
				}
				
			}
			
			footer(writer);
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Erreur à la ligne " + lineNumber);
		}

	}
	
	public static void main(String[] args) {
		RandomGraphParser p = new RandomGraphParser();
		p.parse("result.txt", "output");
	}
}
