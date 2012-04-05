package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualParameter;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;

import com.hp.hpl.jena.rdf.model.Resource;

public class ActualParameterImpl extends ContextInstanceEntityImpl implements
		ActualParameter {

	protected ActualParameterImpl(ContextInstanceProxy proxy,
			Resource actualParameterIndividual) {
		super(proxy, actualParameterIndividual);
	}

	@Override
	public Resource getActualParameterIndividual() {
		return getResource();
	}
}
