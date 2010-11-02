package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

public class Parameter extends Node {
	private String type;
	private Category category;

	public Parameter() {
		super();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return getName() + " (" + type + ")";
	}

}
