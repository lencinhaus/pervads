package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceEntity;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;

import com.hp.hpl.jena.ontology.OntResource;

public class ContextInstanceEntityImpl extends EntityImpl implements
		ContextInstanceEntity {
	private ContextInstanceProxy proxy;

	public ContextInstanceEntityImpl(ContextInstanceProxy proxy,
			OntResource resource) {
		super(resource);
		this.proxy = proxy;
	}

	@Override
	public ContextInstanceProxy getProxy() {
		return proxy;
	}

}
