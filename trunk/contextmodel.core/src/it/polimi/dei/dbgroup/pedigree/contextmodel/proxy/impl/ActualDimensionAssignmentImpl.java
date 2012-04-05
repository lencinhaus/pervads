package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualDimension;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualDimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.Value;

import java.util.Collection;

public class ActualDimensionAssignmentImpl extends
		DimensionAssignmentImpl<ActualParameterAssignment> implements
		ActualDimensionAssignment {
	private ActualDimension actualDimension;

	public ActualDimensionAssignmentImpl(Value value,
			ActualDimension actualDimension,
			Collection<? extends ActualParameterAssignment> parameterAssignments) {
		super(value);
		this.actualDimension = actualDimension;
		for (ActualParameterAssignment assignment : parameterAssignments) {
			this.parameterAssignments.put(assignment.getParameter().getURI(),
					assignment);
		}
	}

	@Override
	public ActualDimension getActualDimension() {
		return actualDimension;
	}

}
