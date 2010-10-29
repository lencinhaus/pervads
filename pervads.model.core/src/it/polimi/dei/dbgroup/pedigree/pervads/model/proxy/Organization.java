package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;

public interface Organization extends PervADsModelEntity {
	public Individual getOrganizationIndividual();
	public PervAD getPervAD();
	public String getName();
	public String getAddress();
	public String getCity();
	public int getZipCode();
	public String getZone();
	public List<String> getEmails();
	public List<String> getWebsites();
}
