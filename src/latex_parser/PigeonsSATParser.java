package latex_parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PigeonsSATParser implements Parser{

	public void header(BufferedWriter writer) throws IOException {
		writer.write("\\begin{center} \n");
		writer.write("\t\\begin{tabular}{ c | c | c | c } \n");
		writer.write("\t\t\\hline \n");
		writer.write("\t\tn & d & DE & SE \\\\ \\hline \n"); 
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
				
				String tDE = splittedLine[2];
				String nbNodesDE = splittedLine[3];
				
				String tSE = splittedLine[4];
				String nbNodesSE = splittedLine[5];
				
				writer.write("\t\t" + n + " & " + d + " & (" + tDE + ", " + nbNodesDE + ") & (" + tSE + ", " + nbNodesSE + ")" + " \\\\ \\hline \n" );
				
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
		PigeonsSATParser p = new PigeonsSATParser();
		p.parse("result.txt", "output");
	}
}
