package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl.NamedEntityImpl;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Offer;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.OfferedItem;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import com.hp.hpl.jena.rdf.model.Resource;

public class OfferedItemImpl extends NamedEntityImpl implements OfferedItem {
	private Offer offer;
	private PervADsModelProxy proxy;

	public OfferedItemImpl(PervADsModelProxy proxy, Resource itemIndividual,
			Offer offer) {
		super(itemIndividual, PervADsModel.itemName, PervADsModel.itemDescription);
		this.offer = offer;
		this.proxy = proxy;
	}

	@Override
	public PervADsModelProxy getProxy() {
		return proxy;
	}

	@Override
	public Offer getOffer() {
		return offer;
	}

	@Override
	public Resource getOfferedItemIndividual() {
		return getResource();
	}
}
