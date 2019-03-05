package bincsp;

import java.util.ArrayList;

public class Relation {

	public enum TypeRelation {
		R_CONFLICTS, R_SUPPORTS
	}
	
	private TypeRelation typeRelation;
	private ArrayList<Couple> couples;
	
	public Relation(TypeRelation typeRelation, ArrayList<Couple> couples) {
		this.typeRelation = typeRelation;
		this.couples = couples;
	}

	public TypeRelation getTypeRelation() {
		return typeRelation;
	}

	public ArrayList<Couple> getCouples() {
		return couples;
	}
	
	public Couple get(int index) {
		return couples.get(index);
	}
	
	public int size() {
		return couples.size();
	}

	public void remove(Couple couple) {
		couples.remove(couple);
	}
	
	public void remove(int index) {
		couples.remove(index);
	}
	
	@Override
	public String toString() {
		String string = "[";
		for (Couple couple : couples) {
			string += "(" + couple.getValue1() + ", " + couple.getValue2() + "), ";
		}
		string += "]";
		return string;
	}
}
