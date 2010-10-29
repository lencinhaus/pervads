package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy;

import com.hp.hpl.jena.ontology.Individual;

public interface OfferedItem extends PervADsModelEntity {
	public Individual getOfferedItemIndividual();
	public Offer getOffer();
	public String getName();
	public String getDescription();
}
