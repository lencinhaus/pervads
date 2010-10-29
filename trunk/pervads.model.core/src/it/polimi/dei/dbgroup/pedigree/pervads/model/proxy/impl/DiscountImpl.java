package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Discount;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervAD;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import com.hp.hpl.jena.ontology.Individual;

public class DiscountImpl extends OfferImpl implements Discount {

	public DiscountImpl(PervADsModelProxy proxy, Individual offerIndividual,
			PervAD pervad) {
		super(proxy, offerIndividual, pervad);
	}

	@Override
	public float getDiscount() {
		return ModelUtils.parseFloatLiteral(getOfferIndividual().getPropertyValue(PervADsModel.discount));
	}

}
