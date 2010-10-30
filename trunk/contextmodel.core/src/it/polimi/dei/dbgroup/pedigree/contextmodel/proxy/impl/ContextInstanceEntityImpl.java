package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceEntity;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;

import com.hp.hpl.jena.rdf.model.Resource;

public class ContextInstanceEntityImpl extends EntityImpl implements
		ContextInstanceEntity {
	private ContextInstanceProxy proxy;

	public ContextInstanceEntityImpl(ContextInstanceProxy proxy,
			Resource resource) {
		super(resource);
		this.proxy = proxy;
	}

	@Override
	public ContextInstanceProxy getProxy() {
		return proxy;
	}

}
