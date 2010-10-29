package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Good;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Offer;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;

import com.hp.hpl.jena.ontology.Individual;

public class GoodImpl extends OfferedItemImpl implements Good {
	public GoodImpl(PervADsModelProxy proxy, Individual itemIndividual,
			Offer offer) {
		super(proxy, itemIndividual, offer);
	}
}
