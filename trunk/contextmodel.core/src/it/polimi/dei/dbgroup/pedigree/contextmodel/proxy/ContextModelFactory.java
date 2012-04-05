package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl.ContextInstanceProxyImpl;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl.ContextModelProxyImpl;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl.FormalDimensionAssignmentImpl;

import com.hp.hpl.jena.rdf.model.Model;

public class ContextModelFactory {
	public static final String CONTEXT_MODEL_VOCABULARY_ALT_URI = "owl/context-model-vocabulary.owl";

	private ContextModelFactory() {
	}

	public static ContextModelProxy createProxy(Model model) {
		return new ContextModelProxyImpl(model);
	}

	public static ContextInstanceProxy createInstanceProxy(
			ContextModelProxy contextModel, Model model) {
		return new ContextInstanceProxyImpl(contextModel, model);
	}

	public static FormalDimensionAssignment createDimensionAssignment(
			Value value) {
		if (value == null)
			throw new IllegalArgumentException("value cannot be null");
		return new FormalDimensionAssignmentImpl(value);
	}
}
