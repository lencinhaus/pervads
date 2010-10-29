package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Context;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Offer;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Organization;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervAD;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class PervADImpl extends PervADsModelEntityImpl implements PervAD {
	private Individual pervadIndividual;
	private Context context;

	public PervADImpl(PervADsModelProxy proxy, Individual pervadIndividual,
			Context context) {
		super(proxy, pervadIndividual);
		this.pervadIndividual = pervadIndividual;
		this.context = context;
	}

	@Override
	public Individual getPervADIndividual() {
		return pervadIndividual;
	}

	@Override
	public List<? extends Organization> listAdvertisers() {
		List<Organization> organizations = new ArrayList<Organization>();
		for (NodeIterator it = pervadIndividual
				.listPropertyValues(PervADsModel.hasAdvertiser); it.hasNext();) {
			RDFNode node = it.next();
			if (node.isURIResource()) {
				Individual organizationIndividual = node.as(Individual.class);
				Organization organization = new OrganizationImpl(getProxy(),
						organizationIndividual, this);
				organizations.add(organization);
			}
		}

		return organizations;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public List<? extends Offer> listOffers() {
		List<Offer> offers = new ArrayList<Offer>();
		for (NodeIterator it = pervadIndividual
				.listPropertyValues(PervADsModel.advertises); it.hasNext();) {
			RDFNode node = it.next();
			if (node.isURIResource()) {
				Individual offerIndividual = node.as(Individual.class);
				Offer offer;
				if (offerIndividual.hasOntClass(PervADsModel.SpecialPrice))
					offer = new SpecialPriceImpl(getProxy(), offerIndividual,
							this);
				else if (offerIndividual.hasOntClass(PervADsModel.Discount))
					offer = new DiscountImpl(getProxy(), offerIndividual, this);
				else
					offer = new OfferImpl(getProxy(), offerIndividual, this);
				offers.add(offer);
			}
		}

		return offers;
	}

	@Override
	public List<String> listTags() {
		return ModelUtils.parseStringLiterals(pervadIndividual
				.listPropertyValues(PervADsModel.hasTag));
	}

}
