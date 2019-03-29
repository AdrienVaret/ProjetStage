package latex_parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CompleteGraphParser implements Parser{

	public void header(BufferedWriter writer) throws IOException {
		writer.write("\\begin{center} \n");
		writer.write("\t\\begin{tabular}{ c | c | c | c | c | c} \n");
		writer.write("\t\t\\hline \n");
		writer.write("\t\tn & d & DE symmetries & DE w.o symmetries & SE symmetries & SE w.o symmetries \\\\ \\hline \n"); 
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
				
				String tSymDE = splittedLine[2];
				String nbNodesSymDE = splittedLine[3];
				
				String tNSymDE = splittedLine[4];
				String nbNodeNSymDE = splittedLine[5];
				
				String tSymSE = splittedLine[6];
				String nbNodesSymSE = splittedLine[7];
				
				String tNSymSE = splittedLine[8];
				String nbNodeNSymSE = splittedLine[9];
					
				writer.write("\t\t" + n + " & " + d + " & (" + tSymDE + ", " + nbNodesSymDE + ") & (" + tNSymDE + ", " + nbNodeNSymDE + ") & (" 
				            + tSymSE + ", " + nbNodesSymSE + ") & (" + tNSymSE + ", " + nbNodeNSymSE + ") \\\\ \\hline \n" );
				
				lineNumber ++;
				
				if (lineNumber % 50 == 0) {
					footer(writer);
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
		CompleteGraphParser p = new CompleteGraphParser();
		p.parse("result.txt", "output");
	}
}
