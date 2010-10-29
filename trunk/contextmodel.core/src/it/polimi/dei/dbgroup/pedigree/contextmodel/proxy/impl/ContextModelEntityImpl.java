package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelEntity;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;

import com.hp.hpl.jena.ontology.OntResource;

public class ContextModelEntityImpl extends NamedEntityImpl implements
		ContextModelEntity {
	private ContextModelProxy proxy;

	protected ContextModelEntityImpl(ContextModelProxy proxy,
			OntResource resource) {
		super(resource);
		this.proxy = proxy;
	}

	@Override
	public ContextModelProxy getProxy() {
		return proxy;
	}

}
