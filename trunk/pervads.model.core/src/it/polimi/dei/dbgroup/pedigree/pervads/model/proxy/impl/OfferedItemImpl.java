package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Offer;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.OfferedItem;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import java.util.Locale;

import com.hp.hpl.jena.rdf.model.Resource;

public class OfferedItemImpl extends PervADsModelEntityImpl implements
		OfferedItem {
	private Offer offer;

	public OfferedItemImpl(PervADsModelProxy proxy, Resource itemIndividual,
			Offer offer) {
		super(proxy, itemIndividual);
		this.offer = offer;
	}

	@Override
	public Offer getOffer() {
		return offer;
	}

	@Override
	public String getDescription(String lang) {
		return ModelUtils.getStringProperty(getResource(),
				PervADsModel.itemDescription, lang);
	}

	@Override
	public String getName(String lang) {
		return ModelUtils.getStringProperty(getResource(),
				PervADsModel.itemName, lang);
	}

	@Override
	public String getDescription() {

		return getDescription(getDefaultLanguage());
	}

	@Override
	public String getName() {
		return getName(getDefaultLanguage());
	}

	@Override
	public Resource getOfferedItemIndividual() {
		return getResource();
	}

	private static String getDefaultLanguage() {
		Locale locale = Locale.getDefault();
		if (locale != null)
			return locale.getLanguage();
		return null;
	}
}
