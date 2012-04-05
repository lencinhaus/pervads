package it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized;

public interface ScalarValue {
	public static enum Type {
		EXACT,
		INTERVAL
	}
	
	public Type getType();
	
	public double getStart();
	
	public double getEnd();
	
	public double getScalar();
}
