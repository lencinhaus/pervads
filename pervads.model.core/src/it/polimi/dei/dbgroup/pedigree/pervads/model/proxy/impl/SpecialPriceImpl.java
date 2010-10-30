package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervAD;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.SpecialPrice;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import com.hp.hpl.jena.rdf.model.Resource;

public class SpecialPriceImpl extends OfferImpl implements SpecialPrice {

	public SpecialPriceImpl(PervADsModelProxy proxy, Resource offerIndividual,
			PervAD pervad) {
		super(proxy, offerIndividual, pervad);
	}

	@Override
	public String getCurrency() {
		return ModelUtils.getStringProperty(getOfferIndividual(),
				PervADsModel.currency);
	}

	@Override
	public float getPrice() {
		return ModelUtils.getTypedProperty(getOfferIndividual(),
				PervADsModel.price, Float.class);
	}

}
