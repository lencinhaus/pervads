package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

public class Node {
	private LocalizedAttribute name = new LocalizedAttribute();
	private LocalizedAttribute description = new LocalizedAttribute();
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

	@Override
	public String toString() {
		return name.getDefaultValue();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
