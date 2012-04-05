package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualParameter;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ActualParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ValueParameter;

public class ActualParameterAssignmentImpl extends ParameterAssignmentImpl
		implements ActualParameterAssignment {
	private ActualParameter actualParameter;

	public ActualParameterAssignmentImpl(ValueParameter parameter,
			Object value, ActualParameter actualParameter) {
		super(parameter, value);
		this.actualParameter = actualParameter;
	}

	@Override
	public ActualParameter getActualParameter() {
		return actualParameter;
	}

}
