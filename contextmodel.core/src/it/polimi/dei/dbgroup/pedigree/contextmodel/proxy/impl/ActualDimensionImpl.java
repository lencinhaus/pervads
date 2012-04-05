package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualDimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;

import com.hp.hpl.jena.rdf.model.Resource;

public class ActualDimensionImpl extends ContextInstanceEntityImpl implements
		ActualDimension {

	protected ActualDimensionImpl(ContextInstanceProxy proxy,
			Resource actualDimensionIndividual) {
		super(proxy, actualDimensionIndividual);
	}

	@Override
	public Resource getActualDimensionIndividual() {
		return getResource();
	}
}
