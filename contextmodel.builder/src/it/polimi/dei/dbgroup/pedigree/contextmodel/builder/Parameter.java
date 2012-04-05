package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import com.hp.hpl.jena.datatypes.RDFDatatype;

public class Parameter extends Node {
	private RDFDatatype type;
	private Category category;

	public Parameter() {
		super();
	}

	public RDFDatatype getType() {
		return type;
	}

	public void setType(RDFDatatype type) {
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
		return getName() + " (" + type.getURI() + ")";
	}

}
