package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;


public interface NamedEntity extends Entity {
	public String getName(String lang);
	
	public String getName();

	public String getDescription(String lang);
	
	public String getDescription();
}
