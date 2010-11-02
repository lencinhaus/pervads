package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import java.util.ArrayList;
import java.util.List;

public class Category extends Node {
	private Category parent;
	private List<Category> subCategories = new ArrayList<Category>();
	private List<Parameter> parameters = new ArrayList<Parameter>();

	public Category(String name) {
		super(name);
	}
	public Category() {
		super();
	}

	public List<Category> getSubCategories() {
		return subCategories;
	}

	public Category getParent() {
		return parent;
	}

	public void setParent(Category parent) {
		this.parent = parent;
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
		for (int i = 0; i < indent; i++) {
			sb.append("\t");
		}
		sb.append(super.toString());
		sb.append("\n");
		if (parameters.size() > 0) {
			for (int i = 0; i < indent; i++) {
				sb.append("\t");
			}
			sb.append("Parameters:\n");
			for (Parameter parameter : parameters) {
				for (int i = 0; i <= indent; i++) {
					sb.append("\t");
				}
				sb.append(parameter);
				sb.append("\n");
			}
		}
		if (subCategories.size() > 0) {
			for (int i = 0; i < indent; i++) {
				sb.append("\t");
			}
			sb.append("Subcategories:\n");
			for (Category category : subCategories) {
				category.toString(sb, indent + 1);
			}
		}
	}
}
