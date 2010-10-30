package it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl.EntityImpl;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelEntity;
import it.polimi.dei.dbgroup.pedigree.pervads.model.proxy.PervADsModelProxy;

import com.hp.hpl.jena.rdf.model.Resource;

public class PervADsModelEntityImpl extends EntityImpl implements PervADsModelEntity {
	private PervADsModelProxy proxy;

	public PervADsModelEntityImpl(PervADsModelProxy proxy, Resource resource) {
		super(resource);
		this.proxy = proxy;
	}
	
	@Override
	public PervADsModelProxy getProxy() {
		return proxy;
	}
}
