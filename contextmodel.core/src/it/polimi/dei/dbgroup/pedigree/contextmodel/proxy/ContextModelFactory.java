package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl.AssignmentDefinitionImpl;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl.ContextInstanceProxyImpl;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl.ContextModelProxyImpl;

import com.hp.hpl.jena.rdf.model.Model;

public class ContextModelFactory {
	public static final String CONTEXT_MODEL_ALT_URI = "owl/context-model.owl";

	private ContextModelFactory() {
	}

	public static ContextModelProxy createProxy(Model model) {
		return new ContextModelProxyImpl(model);
	}

	public static ContextInstanceProxy createInstanceProxy(
			ContextModelProxy contextModel, Model model) {
		return new ContextInstanceProxyImpl(contextModel, model);
	}

	public static AssignmentDefinition createAssignmentDefinition(Value value,
			Dimension dimension) {
		if (value == null)
			throw new IllegalArgumentException("value cannot be null");
		if (dimension == null)
			dimension = value.getParentDimension();
		return new AssignmentDefinitionImpl(dimension, value);
	}

	public static AssignmentDefinition createAssignmentDefinition(Value value) {
		if (value == null)
			throw new IllegalArgumentException("value cannot be null");
		return createAssignmentDefinition(value, value.getParentDimension());
	}
}
