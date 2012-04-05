package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.FormalDimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.FormalParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ValueParameter;

public class FormalDimensionAssignmentImpl extends
		DimensionAssignmentImpl<FormalParameterAssignment> implements
		FormalDimensionAssignment {

	public FormalDimensionAssignmentImpl(Value value) {
		super(value);
	}

	@Override
	public void addParameterAssignment(FormalParameterAssignment assignment) {
		// check if parameter is valid for value
		if (!getValue().listParameters().contains(assignment.getParameter()))
			throw new IllegalArgumentException("parameter "
					+ assignment.getParameter()
					+ " is not a valid parameter for value " + getValue());
		parameterAssignments
				.put(assignment.getParameter().getURI(), assignment);
	}

	@Override
	public void setParameterValue(ValueParameter parameter, Object value) {
		FormalParameterAssignment assignment = new FormalParameterAssignmentImpl(
				parameter, value);
		addParameterAssignment(assignment);
	}
}
