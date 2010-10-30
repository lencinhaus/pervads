package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public interface Organization extends PervADsModelEntity {
	public Resource getOrganizationIndividual();

	public PervAD getPervAD();

	public String getName();

	public String getAddress();

	public String getCity();

	public int getZipCode();

	public String getZone();

	public List<String> getEmails();

	public List<String> getWebsites();
}
