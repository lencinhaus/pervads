package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.Offer;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.OfferedItem;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervAD;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;
import it.polimi.dei.dbgroup.pedigree.pervads.model.vocabulary.PervADsModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class OfferImpl extends PervADsModelEntityImpl implements Offer {
	private Individual offerIndividual;
	private PervAD pervad;

	public OfferImpl(PervADsModelProxy proxy, Individual offerIndividual,
			PervAD pervad) {
		super(proxy, offerIndividual);
		this.offerIndividual = offerIndividual;
		this.pervad = pervad;
	}

	@Override
	public List<String> getAttachedImages() {
		return ModelUtils.parseStringLiterals(offerIndividual
				.listPropertyValues(PervADsModel.hasAttachedImage));
	}

	@Override
	public List<String> getAttachedMedia() {
		return ModelUtils.parseStringLiterals(offerIndividual
				.listPropertyValues(PervADsModel.hasAttachedMedia));
	}

	@Override
	public List<String> getAttachedVideos() {
		return ModelUtils.parseStringLiterals(offerIndividual
				.listPropertyValues(PervADsModel.hasAttachedVideo));
	}

	@Override
	public List<String> getDetailCoupons() {
		return ModelUtils.parseStringLiterals(offerIndividual
				.listPropertyValues(PervADsModel.hasDetailCoupon));
	}

	@Override
	public List<? extends OfferedItem> getItems() {
		List<OfferedItem> items = new ArrayList<OfferedItem>();
		for(NodeIterator it = offerIndividual.listPropertyValues(PervADsModel.offers); it.hasNext();) {
			RDFNode node = it.next();
			if(node.isURIResource()) {
				Individual itemIndividual = node.as(Individual.class);
				OfferedItem item;
				if(itemIndividual.hasOntClass(PervADsModel.Good)) item = new GoodImpl(getProxy(), itemIndividual, this);
				else if(offerIndividual.hasOntClass(PervADsModel.Service)) item = new ServiceImpl(getProxy(), itemIndividual, this);
				else item = new OfferedItemImpl(getProxy(), itemIndividual, this);
				items.add(item);
			}
		}
		
		return items;
	}

	@Override
	public List<String> getListCoupons() {
		return ModelUtils.parseStringLiterals(offerIndividual
				.listPropertyValues(PervADsModel.hasListCoupon));
	}

	@Override
	public Individual getOfferIndividual() {
		return offerIndividual;
	}

	@Override
	public PervAD getPervAD() {
		return pervad;
	}

	@Override
	public Date getValidFrom() {
		return ModelUtils.parseDateLiteral(offerIndividual
				.getPropertyValue(PervADsModel.validFrom));
	}

	@Override
	public Date getValidUntil() {
		return ModelUtils.parseDateLiteral(offerIndividual
				.getPropertyValue(PervADsModel.validUntil));
	}

}
