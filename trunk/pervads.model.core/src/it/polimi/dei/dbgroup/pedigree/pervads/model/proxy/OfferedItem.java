package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.NamedEntity;

import com.hp.hpl.jena.rdf.model.Resource;

public interface OfferedItem extends PervADsModelEntity, NamedEntity {
	public Resource getOfferedItemIndividual();
	public Offer getOffer();
}
