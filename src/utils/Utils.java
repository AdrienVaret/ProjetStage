package utils;

import java.util.ArrayList;
import java.util.Arrays;

import sat.Litteral;
import sat.SAT;

public class Utils {
	
	public static void clearArray(Object [] t) {
		for (int i = 0 ; i < t.length ; i++) {
			if (t[i] == null) break;
			t[i] = null;
		}
	}
	
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
	
	public static void shiftAll(int [] t, int n) {
		//t [0] -= n;
		for (int i = 0 ; i < n ; i++) {
			for (int j = 1 ; j < t.length ; j++) {
				if (t[j] == -1) {
					//shiftToEnd(t,j);
					swap(t, j, t[0]);
					t[0] --;
					break;
				}
			}
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
		int [] t = {5, 2, 3, -1, 2, -1, -1, 2};
		ArrayList<Integer> ts = new ArrayList<Integer>();
		ts.add(3);
		ts.add(5);
		ts.add(6);
		shiftAll(t, 3);
		for (int i : t) {
			System.out.print(i + " ");
		}
	}
}
