package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.AssignmentDefinition;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;

public class AssignmentDefinitionImpl implements AssignmentDefinition {
	private Dimension dimension;
	private Value value;
	
	public AssignmentDefinitionImpl(Dimension dimension, Value value) {
		this.dimension = dimension;
		this.value = value;
	}

	@Override
	public Dimension getDimension() {
		return dimension;
	}

	@Override
	public Value getValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AssignmentDefinition) {
			if(obj == this) return true;
			AssignmentDefinition definition = (AssignmentDefinition) obj;
			return dimension.equals(definition.getDimension()) && value.equals(definition.getValue());
		}
		return super.equals(obj);
	}

	
}
