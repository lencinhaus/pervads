package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

public interface ContextEntity {
	public String getURI();

	public String getName(String lang);
	
	public String getName();

	public String getDescription(String lang);
	
	public String getDescription();
	
	public ContextModelProxy getProxy();
}
