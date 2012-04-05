package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Context;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextInstanceProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ContextModelProxy;
import it.polimi.dei.dbgroup.pedigree.contextmodel.util.ModelUtils;
import it.polimi.dei.dbgroup.pedigree.contextmodel.vocabulary.ContextModelVocabulary;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class ContextInstanceProxyImpl implements ContextInstanceProxy {
	private ContextModelProxy contextModelProxy;
	private Model model;

	public ContextInstanceProxyImpl(ContextModelProxy contextModelProxy,
			Model model) {
		this.contextModelProxy = contextModelProxy;
		this.model = model;
	}

	@Override
	public Model getModel() {
		return model;
	}

	@Override
	public Context getContext(String uri) {
		Context context = null;
		Resource contextIndividual = ModelUtils.getResourceIfExists(model, uri);
		if (contextIndividual != null
				&& model.contains(contextIndividual, RDF.type,
						ContextModelVocabulary.Context)) {
			context = new ContextImpl(this, contextIndividual);
		}
		return context;
	}

	@Override
	public List<? extends Context> listContexts() {
		List<Context> contexts = new ArrayList<Context>();
		ExtendedIterator<Resource> iterator = model.listResourcesWithProperty(
				RDF.type, ContextModelVocabulary.Context);
		while (iterator.hasNext()) {
			Resource contextIndividual = iterator.next();
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
