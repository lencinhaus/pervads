package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl.ContextModelProxyImpl;

import com.hp.hpl.jena.ontology.OntModel;

public class ContextModelFactory {
	public static final String CONTEXT_MODEL_ALT_URI = "owl/context-model.owl";
	
	private ContextModelFactory() {
	}

	public static ContextModelProxy createProxy(OntModel model) {
		return new ContextModelProxyImpl(model);
	}
}
