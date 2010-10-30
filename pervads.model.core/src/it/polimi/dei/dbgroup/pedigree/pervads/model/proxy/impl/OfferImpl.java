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

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class OfferImpl extends PervADsModelEntityImpl implements Offer {
	private PervAD pervad;

	public OfferImpl(PervADsModelProxy proxy, Resource offerIndividual,
			PervAD pervad) {
		super(proxy, offerIndividual);
		this.pervad = pervad;
	}

	@Override
	public List<String> getAttachedImages() {
		return ModelUtils.listStringProperties(getOfferIndividual(), PervADsModel.hasAttachedImage);
	}

	@Override
	public List<String> getAttachedMedia() {
		return ModelUtils.listStringProperties(getOfferIndividual(), PervADsModel.hasAttachedMedia);
	}

	@Override
	public List<String> getAttachedVideos() {
		return ModelUtils.listStringProperties(getOfferIndividual(), PervADsModel.hasAttachedVideo);
	}

	@Override
	public List<String> getDetailCoupons() {
		return ModelUtils.listStringProperties(getOfferIndividual(), PervADsModel.hasDetailCoupon);
	}

	@Override
	public List<? extends OfferedItem> getItems() {
		List<OfferedItem> items = new ArrayList<OfferedItem>();
		for(ExtendedIterator<RDFNode> it = getProxy().getModel().listObjectsOfProperty(getOfferIndividual(), PervADsModel.offers); it.hasNext();) {
			RDFNode node = it.next();
			if(node.isURIResource()) {
				Resource itemIndividual = (Resource) node;
				OfferedItem item;
				if(itemIndividual.hasProperty(RDF.type, PervADsModel.Good)) item = new GoodImpl(getProxy(), itemIndividual, this);
				else if(itemIndividual.hasProperty(RDF.type, PervADsModel.Service)) item = new ServiceImpl(getProxy(), itemIndividual, this);
				else item = new OfferedItemImpl(getProxy(), itemIndividual, this);
				items.add(item);
			}
		}
		
		return items;
	}

	@Override
	public List<String> getListCoupons() {
		return ModelUtils.listStringProperties(getOfferIndividual(), PervADsModel.hasListCoupon);
	}

	@Override
	public Resource getOfferIndividual() {
		return getResource();
	}

	@Override
	public PervAD getPervAD() {
		return pervad;
	}

	@Override
	public Date getValidFrom() {
		return ModelUtils.getDateProperty(getOfferIndividual(), PervADsModel.validFrom);
	}

	@Override
	public Date getValidUntil() {
		return ModelUtils.getDateProperty(getOfferIndividual(), PervADsModel.validUntil);
	}

}
