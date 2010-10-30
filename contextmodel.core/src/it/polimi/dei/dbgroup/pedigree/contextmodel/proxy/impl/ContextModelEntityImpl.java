package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelEntity;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;

import com.hp.hpl.jena.rdf.model.Resource;

public class ContextModelEntityImpl extends NamedEntityImpl implements
		ContextModelEntity {
	private ContextModelProxy proxy;

	protected ContextModelEntityImpl(ContextModelProxy proxy,
			Resource resource) {
		super(resource);
		this.proxy = proxy;
	}

	@Override
	public ContextModelProxy getProxy() {
		return proxy;
	}

}
