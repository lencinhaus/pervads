package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import java.util.ArrayList;
import java.util.List;

public class Node {
	private LocalizedAttribute name = new LocalizedAttribute();
	private LocalizedAttribute description = new LocalizedAttribute();
	private List<String> classes = new ArrayList<String>();
	private String id;
	
	public Node(String name) {
		getName().setDefaultValue(name);
	}

	public Node() {

	}

	public LocalizedAttribute getName() {
		return name;
	}

	public LocalizedAttribute getDescription() {
		return description;
	}
	
	public List<String> getClasses() {
		return classes;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, 0);
		return sb.toString();
	}
	
	protected void toString(StringBuilder sb, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append(" ");
		}
		sb.append("* ");
		sb.append(name.getDefaultValue());
		sb.append("\n");
		if(classes.size() > 0) {
			for (int i = 0; i <= indent; i++) {
				sb.append(" ");
			}
			sb.append("Classes:\n");
			for (String clazz : classes) {
				for (int i = 0; i <= indent + 1; i++) {
					sb.append(" ");
				}
				sb.append(clazz);
				sb.append("\n");
			}
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
