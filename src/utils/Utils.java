package utils;

import java.util.ArrayList;

import sat.Litteral;
import sat.SAT;

public class Utils {
	
	public static void shiftToEnd(int [] t, int i) {
		int index = i;
		while (index + 1 < t.length) {
			swap(t, index, index+1);
			index ++;
		}
	}
	
	public static void shiftAll(int [] t, ArrayList<Integer> toShift) {
		for (Integer i : toShift) {
			t[0] --;
			shiftToEnd(t, i);
		}
	}
	
	public static Double integerToDouble(Integer i) {
		return new Double(i);
	}
	
	public static void swap(int [] t, int i, int j) {
		int k = t[i];
		t[i] = t[j];
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
	
	public static void main(String [] args) {
		int [] t = {1,-1,3,4,5};
		shiftToEnd(t, 1);
		for (int i : t) {
			System.out.println(i);
		}
	}
}
