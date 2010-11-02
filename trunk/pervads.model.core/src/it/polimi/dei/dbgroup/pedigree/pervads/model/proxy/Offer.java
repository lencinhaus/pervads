package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy;

import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public interface Offer extends PervADsModelEntity {
	public Resource getOfferIndividual();

	public PervAD getPervAD();

	public List<? extends OfferedItem> getItems();

	public Date getValidFrom();

	public Date getValidUntil();

	public List<String> getAttachedMedia();

	public List<String> getAttachedImages();

	public List<String> getAttachedVideos();
	
	public String getListCoupon(String lang);
	
	public String getListCoupon();

	public String getDetailCoupon(String lang);
	
	public String getDetailCoupon();
}
