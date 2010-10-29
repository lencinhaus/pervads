package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Offer;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Service;

import com.hp.hpl.jena.ontology.Individual;

public class ServiceImpl extends OfferedItemImpl implements Service {
	public ServiceImpl(PervADsModelProxy proxy, Individual itemIndividual,
			Offer offer) {
		super(proxy, itemIndividual, offer);
	}
}
