package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

public class Parameter extends Node {
	private String type;

	public Parameter(String name) {
		super(name);
	}
	
	public Parameter() {
		super();
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return getName() + " (" + type + ")";
	}
	
	
}
