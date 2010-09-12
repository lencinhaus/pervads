package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import java.util.ArrayList;
import java.util.List;

public class Category extends Node {
	private List<Category> subCategories = new ArrayList<Category>();
	private List<Parameter> parameters = new ArrayList<Parameter>();

	public Category() {
		super();
	}
	
	public Category(String name) {
		super(name);
	}
	
	public List<Category> getSubCategories() {
		return subCategories;
	}
	
	public List<Parameter> getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, 0);
		return sb.toString();
	}
	
	protected void toString(StringBuilder sb, int indent) {
		for(int i=0; i < indent; i++) {
			sb.append("\t");
		}
		sb.append(super.toString());
		sb.append("\n");
		for(Category category : subCategories) {
			category.toString(sb, indent + 1);
		}
	}
}
