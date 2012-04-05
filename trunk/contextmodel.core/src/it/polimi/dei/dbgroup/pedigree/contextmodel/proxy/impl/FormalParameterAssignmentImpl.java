package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.impl;

import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.FormalParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ValueParameter;

public class FormalParameterAssignmentImpl extends ParameterAssignmentImpl
		implements FormalParameterAssignment {
	
	public FormalParameterAssignmentImpl(ValueParameter parameter, Object value) {
		super(parameter, value);
	}

	@Override
	public void setParameter(ValueParameter parameter) {
		this.parameter = parameter;
	}

	@Override
	public void setValue(Object value) {
		checkValue(value);
		this.value = value;
	}

}
