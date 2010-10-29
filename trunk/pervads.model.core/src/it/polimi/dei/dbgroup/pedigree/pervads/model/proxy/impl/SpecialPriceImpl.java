package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervAD;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.SpecialPrice;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import com.hp.hpl.jena.ontology.Individual;

public class SpecialPriceImpl extends OfferImpl implements SpecialPrice {

	public SpecialPriceImpl(PervADsModelProxy proxy,
			Individual offerIndividual, PervAD pervad) {
		super(proxy, offerIndividual, pervad);
	}

	@Override
	public String getCurrency() {
		return ModelUtils.parseStringLiteral(getOfferIndividual()
				.getPropertyValue(PervADsModel.currency));
	}

	@Override
	public float getPrice() {
		return ModelUtils.parseFloatLiteral(getOfferIndividual()
				.getPropertyValue(PervADsModel.price));
	}

}
