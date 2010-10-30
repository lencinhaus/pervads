package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Offer;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Service;

import com.hp.hpl.jena.rdf.model.Resource;

public class ServiceImpl extends OfferedItemImpl implements Service {
	public ServiceImpl(PervADsModelProxy proxy, Resource itemIndividual,
			Offer offer) {
		super(proxy, itemIndividual, offer);
	}
}
