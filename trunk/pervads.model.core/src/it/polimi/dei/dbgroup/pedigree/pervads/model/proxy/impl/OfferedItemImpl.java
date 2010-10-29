package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Offer;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.OfferedItem;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import com.hp.hpl.jena.ontology.Individual;

public class OfferedItemImpl extends PervADsModelEntityImpl implements
		OfferedItem {
	private Individual itemIndividual;
	private Offer offer;
	
	public OfferedItemImpl(PervADsModelProxy proxy, Individual itemIndividual,
			Offer offer) {
		super(proxy, itemIndividual);
		this.itemIndividual = itemIndividual;
		this.offer = offer;
	}

	@Override
	public Offer getOffer() {
		return offer;
	}

	@Override
	public String getDescription() {
		return ModelUtils.parseStringLiteral(itemIndividual.getPropertyValue(PervADsModel.itemDescription));
	}

	@Override
	public String getName() {
		return ModelUtils.parseStringLiteral(itemIndividual.getPropertyValue(PervADsModel.itemName));
	}

	@Override
	public Individual getOfferedItemIndividual() {
		return itemIndividual;
	}

}
