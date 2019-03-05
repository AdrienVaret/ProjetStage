package utils;

import java.util.ArrayList;

import sat.Litteral;
import sat.SAT;

public class Utils {
	
	public static Double integerToDouble(Integer i) {
		return new Double(i);
	}
	
	public static void swap(int [] t, int i, int j) {
		int k = t[i];
		t[i] = j;
		t[j] = k;
	}
	
	public static boolean isInt(String str)  
	{  
	  try  
	  {  
	    @SuppressWarnings("unused")
		int i = Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	public static ArrayList<Litteral> intersection(SAT sat, ArrayList<Litteral> L1, ArrayList<Litteral> L2){
		int [] t = new int [sat.getNbVariables() * 2];
		ArrayList<Litteral> L = new ArrayList<Litteral>();
		for (Litteral l : L1) {
			t[l.getId()] += 1;
		}
		for (Litteral l : L2) {
			t[l.getId()] += 1;
			if (t[l.getId()] >= 2) {
				L.add(l);
			}
		}
		return L;
	}
}
