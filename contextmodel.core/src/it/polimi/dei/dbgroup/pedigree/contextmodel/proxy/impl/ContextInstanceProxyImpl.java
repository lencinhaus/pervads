package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Context;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextModel;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class ContextInstanceProxyImpl implements ContextInstanceProxy {
	private ContextModelProxy contextModelProxy;
	private OntModel model;

	public ContextInstanceProxyImpl(ContextModelProxy contextModelProxy,
			OntModel model) {
		this.contextModelProxy = contextModelProxy;
		this.model = model;
	}

	@Override
	public OntModel getModel() {
		return model;
	}

	@Override
	public Context getContext(String uri) {
		Context context = null;
		Individual contextIndividual = getModel().getIndividual(uri);
		if(contextIndividual != null && contextIndividual.hasOntClass(ContextModel.Context)) {
			context = new ContextImpl(this, contextIndividual);
		}
		return context;
	}

	@Override
	public List<? extends Context> listContexts() {
		List<Context> contexts = new ArrayList<Context>();
		ExtendedIterator<Individual> iterator = getModel().listIndividuals(ContextModel.Context);
		while(iterator.hasNext()) {
			Individual contextIndividual = iterator.next();
			Context context = new ContextImpl(this, contextIndividual);
			contexts.add(context);
		}
		
		return contexts;
	}

	@Override
	public ContextModelProxy getContextModel() {
		return contextModelProxy;
	}

}
