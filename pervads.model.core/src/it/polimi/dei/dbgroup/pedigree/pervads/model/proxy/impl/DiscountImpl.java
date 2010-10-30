package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Discount;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervAD;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import com.hp.hpl.jena.rdf.model.Resource;

public class DiscountImpl extends OfferImpl implements Discount {

	public DiscountImpl(PervADsModelProxy proxy, Resource offerIndividual,
			PervAD pervad) {
		super(proxy, offerIndividual, pervad);
	}

	@Override
	public float getDiscount() {
		return ModelUtils.getTypedProperty(getOfferIndividual(), PervADsModel.discount, Float.class);
	}

}
