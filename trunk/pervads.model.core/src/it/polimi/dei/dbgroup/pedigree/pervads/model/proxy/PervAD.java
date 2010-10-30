package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Context;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public interface PervAD extends PervADsModelEntity {
	public Resource getPervADIndividual();

	public Context getContext();

	public List<? extends Organization> listAdvertisers();

	public List<? extends Offer> listOffers();

	public List<String> listTags();
}
