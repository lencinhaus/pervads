package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy;

import com.hp.hpl.jena.rdf.model.Resource;

public interface OfferedItem extends NamedPervADsModelEntity {
	public Resource getOfferedItemIndividual();
	public Offer getOffer();
}
