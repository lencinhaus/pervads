package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Dimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ValueParameter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class DimensionAssignmentImpl<P extends ParameterAssignment> implements
		DimensionAssignment<P> {
	private Value value;
	protected HashMap<String, P> parameterAssignments = new HashMap<String, P>();

	public DimensionAssignmentImpl(Value value) {
		if (value == null)
			throw new NullPointerException("value cannot be null");
		this.value = value;
	}

	@Override
	public Value getValue() {
		return value;
	}

	@Override
	public Dimension getDimension() {
		return value.getParentDimension();
	}

	@Override
	public Collection<? extends P> listParameterAssignments() {
		return Collections
				.unmodifiableCollection(parameterAssignments.values());
	}

	@Override
	public Object getParameterValue(ValueParameter parameter) {
		return getParameterValue(parameter.getURI());
	}

	@Override
	public Object getParameterValue(String formalParameterIndividualUri) {
		ParameterAssignment assignment = parameterAssignments
				.get(formalParameterIndividualUri);
		if (assignment != null)
			return assignment.getValue();
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DimensionAssignment<?>) {
			DimensionAssignment<? extends ParameterAssignment> d = (DimensionAssignment<?>) obj;
			if (!value.equals(d.getValue())
					|| parameterAssignments.size() != d
							.listParameterAssignments().size())
				return false;
			for (String parameter : parameterAssignments.keySet()) {
				Object dValue = d.getParameterValue(parameter);
				if (dValue == null
						|| !getParameterValue(parameter).equals(dValue))
					return false;
			}
			return true;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return value.getURI();
	}

}
